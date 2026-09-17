import {setGlobalOptions} from "firebase-functions";
import {onSchedule} from "firebase-functions/v2/scheduler";
import {onRequest} from "firebase-functions/v2/https";
import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger";

// ---------------------------------------------------------------------------
// Init
// ---------------------------------------------------------------------------
admin.initializeApp();
setGlobalOptions({maxInstances: 10, region: "asia-southeast1"});

const db = admin.firestore();
const messaging = admin.messaging();

// ---------------------------------------------------------------------------
// Helper: get all admin FCM tokens
// ---------------------------------------------------------------------------
async function getAdminFcmTokens(): Promise<string[]> {
  try {
    const snap = await db.collection("fcm_tokens").get();
    const tokens: string[] = [];
    snap.docs.forEach((doc) => {
      const token = doc.data().token as string | undefined;
      if (token) tokens.push(token);
    });
    return tokens;
  } catch (e) {
    logger.error("Gagal mengambil FCM tokens", e);
    return [];
  }
}

// ---------------------------------------------------------------------------
// Helper: send FCM multicast
// ---------------------------------------------------------------------------
async function sendFcmNotification(
  tokens: string[],
  title: string,
  body: string,
  data?: Record<string, string>
): Promise<void> {
  if (tokens.length === 0) {
    logger.warn("Tidak ada FCM token terdaftar, skip kirim notifikasi.");
    return;
  }

  // FCM sendEachForMulticast (max 500 tokens per batch)
  const chunks: string[][] = [];
  for (let i = 0; i < tokens.length; i += 500) {
    chunks.push(tokens.slice(i, i + 500));
  }

  for (const chunk of chunks) {
    const message: admin.messaging.MulticastMessage = {
      tokens: chunk,
      notification: {title, body},
      android: {
        notification: {
          channelId: "primaraga_gym_channel",
          priority: "high",
          sound: "default",
          icon: "logogym",
          color: "#32A060",
          defaultSound: true,
        },
      },
      data: {
        title,
        body,
        ...(data ?? {}),
      },
    };
    try {
      const result = await messaging.sendEachForMulticast(message);
      logger.info(
        `FCM sent: ${result.successCount} success, ${result.failureCount} failed`
      );
    } catch (e) {
      logger.error("FCM send error", e);
    }
  }
}

// ---------------------------------------------------------------------------
// Helper: save notification to Firestore
// ---------------------------------------------------------------------------
async function saveNotification(
  branchId: string | null,
  memberId: string | null,
  type: string,
  title: string,
  message: string
): Promise<void> {
  try {
    await db.collection("notifications").add({
      branchId: branchId ?? null,
      memberId: memberId ?? null,
      userId: null,
      type,
      title,
      message,
      isRead: false,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } catch (e) {
    logger.error("Gagal menyimpan notifikasi ke Firestore", e);
  }
}

// ---------------------------------------------------------------------------
// 1. RUNNER: CHECK MEMBER BIRTHDAYS
// ---------------------------------------------------------------------------
export async function runMemberBirthdaysCheck(
  targetDateStr?: string
): Promise<{ checked: number; birthdayCount: number }> {
  logger.info("=== runMemberBirthdaysCheck START ===");

  const now = new Date();
  let todayMmDd = "";

  if (targetDateStr) {
    todayMmDd = targetDateStr.length >= 10 ? targetDateStr.substring(5, 10) : targetDateStr;
  } else {
    const month = String(now.getUTCMonth() + 1).padStart(2, "0");
    const day = String(now.getUTCDate()).padStart(2, "0");
    todayMmDd = `${month}-${day}`;
  }

  logger.info(`Mengecek ulang tahun untuk tanggal (MM-dd): ${todayMmDd}`);

  let birthdayCount = 0;
  let checked = 0;

  try {
    const membersSnap = await db
      .collection("members")
      .where("status", "==", "ACTIVE")
      .get();

    checked = membersSnap.size;
    const tokens = await getAdminFcmTokens();

    for (const doc of membersSnap.docs) {
      const member = doc.data();
      const dateOfBirth: string = member.dateOfBirth ?? "";

      let memberMmDd = "";
      if (dateOfBirth.length >= 10) {
        memberMmDd = dateOfBirth.substring(5, 10);
      } else if (dateOfBirth.length === 5) {
        memberMmDd = dateOfBirth;
      }

      if (memberMmDd === todayMmDd) {
        birthdayCount++;
        const memberName: string = member.fullName ?? "Member";
        const branchId: string = member.branchId ?? null;
        const memberId: string = doc.id;

        const title = "🎂 Ulang Tahun Member";
        const body = `${memberName} berulang tahun hari ini! Kirimkan ucapan selamat.`;

        await saveNotification(branchId, memberId, "MEMBER_BIRTHDAY", title, body);
        await sendFcmNotification(tokens, title, body, {
          type: "MEMBER_BIRTHDAY",
          memberId,
          memberName,
        });

        logger.info(`Birthday notification sent for: ${memberName}`);
      }
    }

    logger.info(`=== runMemberBirthdaysCheck DONE: ${birthdayCount} members ===`);
  } catch (e) {
    logger.error("Error runMemberBirthdaysCheck", e);
  }

  return {checked, birthdayCount};
}

// ---------------------------------------------------------------------------
// 2. RUNNER: CHECK INACTIVE MEMBERS (>= 14 days without check-in)
// ---------------------------------------------------------------------------
export async function runInactiveMembersCheck(
  ignoreDuplicate = false
): Promise<{ checked: number; inactiveCount: number }> {
  logger.info("=== runInactiveMembersCheck START ===");

  const now = new Date();
  const INACTIVE_DAYS = 14;
  const cutoffDate = new Date(now.getTime() - INACTIVE_DAYS * 24 * 60 * 60 * 1000);

  let inactiveCount = 0;
  let checked = 0;

  try {
    const membersSnap = await db
      .collection("members")
      .where("status", "==", "ACTIVE")
      .get();

    checked = membersSnap.size;
    const tokens = await getAdminFcmTokens();

    for (const doc of membersSnap.docs) {
      const member = doc.data();
      const memberId = doc.id;
      const memberName: string = member.fullName ?? "Member";
      const branchId: string = member.branchId ?? null;

      const lastCheckinSnap = await db
        .collection("checkins")
        .where("memberId", "==", memberId)
        .orderBy("checkInAt", "desc")
        .limit(1)
        .get();

      let isInactive = false;
      let lastCheckinLabel = "Belum pernah check-in";

      if (lastCheckinSnap.empty) {
        const joinedAt = member.joinedAt as admin.firestore.Timestamp | null;
        if (!joinedAt || joinedAt.toDate() < cutoffDate) {
          isInactive = true;
        }
      } else {
        const lastDoc = lastCheckinSnap.docs[0].data();
        const checkInAt = lastDoc.checkInAt as admin.firestore.Timestamp | null;
        if (checkInAt) {
          const lastDate = checkInAt.toDate();
          if (lastDate < cutoffDate) {
            isInactive = true;
            lastCheckinLabel = `${lastDate.getDate()}/${lastDate.getMonth() + 1}/${lastDate.getFullYear()}`;
          }
        }
      }

      if (!isInactive) continue;

      if (!ignoreDuplicate) {
        const todayStart = new Date(now);
        todayStart.setUTCHours(0, 0, 0, 0);

        const existingSnap = await db
          .collection("notifications")
          .where("memberId", "==", memberId)
          .where("type", "==", "MEMBER_INACTIVE")
          .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(todayStart))
          .limit(1)
          .get();

        if (!existingSnap.empty) {
          logger.info(`Skip duplicate inactive notif for: ${memberName}`);
          continue;
        }
      }

      inactiveCount++;

      const title = "⚠️ Member Tidak Aktif";
      const body = `${memberName} tidak check-in selama ${INACTIVE_DAYS}+ hari. Terakhir: ${lastCheckinLabel}.`;

      await saveNotification(branchId, memberId, "MEMBER_INACTIVE", title, body);
      await sendFcmNotification(tokens, title, body, {
        type: "MEMBER_INACTIVE",
        memberId,
        memberName,
        lastCheckin: lastCheckinLabel,
      });

      logger.info(`Inactive notification sent for: ${memberName}`);
    }

    logger.info(`=== runInactiveMembersCheck DONE: ${inactiveCount} inactive ===`);
  } catch (e) {
    logger.error("Error runInactiveMembersCheck", e);
  }

  return {checked, inactiveCount};
}

// ---------------------------------------------------------------------------
// 3. RUNNER: CHECK EXPIRING MEMBERSHIPS (in 7 days or 3 days)
// ---------------------------------------------------------------------------
export async function runExpiringMembershipsCheck(
  ignoreDuplicate = false
): Promise<{ checked: number; expiringCount: number }> {
  logger.info("=== runExpiringMembershipsCheck START ===");

  const now = new Date();
  now.setUTCHours(0, 0, 0, 0);

  const ALERT_DAYS = [7, 3];
  let expiringCount = 0;
  let checked = 0;

  try {
    const membersSnap = await db
      .collection("members")
      .where("status", "==", "ACTIVE")
      .get();

    checked = membersSnap.size;
    const tokens = await getAdminFcmTokens();

    for (const doc of membersSnap.docs) {
      const member = doc.data();
      const memberId = doc.id;
      const memberName: string = member.fullName ?? "Member";
      const branchId: string = member.branchId ?? null;
      const expiredDateStr: string = member.expiredDate ?? "";
      const planName: string = member.planName ?? "Membership";

      if (!expiredDateStr) continue;

      const expiredDate = new Date(expiredDateStr);
      expiredDate.setUTCHours(0, 0, 0, 0);

      const diffMs = expiredDate.getTime() - now.getTime();
      const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));

      if (!ALERT_DAYS.includes(diffDays)) continue;

      if (!ignoreDuplicate) {
        const todayStart = new Date(now);
        todayStart.setUTCHours(0, 0, 0, 0);

        const existingSnap = await db
          .collection("notifications")
          .where("memberId", "==", memberId)
          .where("type", "==", "MEMBERSHIP_EXPIRING")
          .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(todayStart))
          .limit(1)
          .get();

        if (!existingSnap.empty) {
          logger.info(`Skip duplicate expiring notif for: ${memberName}`);
          continue;
        }
      }

      expiringCount++;

      const title = "🔔 Membership Hampir Expired";
      const body = `${memberName} — ${planName} akan berakhir dalam ${diffDays} hari (${expiredDateStr}).`;

      await saveNotification(branchId, memberId, "MEMBERSHIP_EXPIRING", title, body);
      await sendFcmNotification(tokens, title, body, {
        type: "MEMBERSHIP_EXPIRING",
        memberId,
        memberName,
        expiredDate: expiredDateStr,
        daysLeft: String(diffDays),
      });

      logger.info(
        `Expiring notification sent for: ${memberName} (${diffDays} days left)`
      );
    }

    logger.info(`=== runExpiringMembershipsCheck DONE: ${expiringCount} expiring ===`);
  } catch (e) {
    logger.error("Error runExpiringMembershipsCheck", e);
  }

  return {checked, expiringCount};
}

// ---------------------------------------------------------------------------
// Scheduled Cloud Functions
// ---------------------------------------------------------------------------
export const checkMemberBirthdays = onSchedule(
  {
    schedule: "0 0 * * *", // 07:00 WIB = 00:00 UTC
    timeZone: "UTC",
  },
  async () => {
    await runMemberBirthdaysCheck();
  }
);

export const checkInactiveMembers = onSchedule(
  {
    schedule: "0 1 * * *", // 08:00 WIB = 01:00 UTC
    timeZone: "UTC",
  },
  async () => {
    await runInactiveMembersCheck();
  }
);

export const checkExpiringMemberships = onSchedule(
  {
    schedule: "0 2 * * *", // 09:00 WIB = 02:00 UTC
    timeZone: "UTC",
  },
  async () => {
    await runExpiringMembershipsCheck();
  }
);

// ---------------------------------------------------------------------------
// HTTP Endpoints (Manual Trigger & Push Testing)
// ---------------------------------------------------------------------------
export const manualCheckNotifications = onRequest(async (req, res) => {
  const type = (req.query.type as string) || "all";
  const date = req.query.date as string | undefined;
  const ignoreDuplicate = req.query.ignoreDuplicate === "true";

  try {
    const result: Record<string, unknown> = {};

    if (type === "all" || type === "birthday") {
      result.birthday = await runMemberBirthdaysCheck(date);
    }
    if (type === "all" || type === "inactive") {
      result.inactive = await runInactiveMembersCheck(ignoreDuplicate);
    }
    if (type === "all" || type === "expiring") {
      result.expiring = await runExpiringMembershipsCheck(ignoreDuplicate);
    }

    res.json({
      success: true,
      message: "Check executed successfully",
      type,
      result,
    });
  } catch (error: unknown) {
    logger.error("Error in manualCheckNotifications", error);
    const message = error instanceof Error ? error.message : "Unknown error";
    res.status(500).json({
      success: false,
      error: message,
    });
  }
});

export const testPushNotification = onRequest(async (req, res) => {
  const title = (req.query.title as string) || "🔔 Test Notifikasi FCM";
  const body = (req.query.body as string) || "Ini adalah notifikasi uji coba dari Primaraga Gym!";

  try {
    const tokens = await getAdminFcmTokens();
    if (tokens.length === 0) {
      res.status(400).json({
        success: false,
        message: "Tidak ada token admin di collection fcm_tokens. Buka aplikasi dan login terlebih dahulu.",
      });
      return;
    }

    await sendFcmNotification(tokens, title, body, {
      type: "TEST",
      timestamp: String(Date.now()),
    });

    res.json({
      success: true,
      message: `Push notification berhasil dikirim ke ${tokens.length} token admin.`,
      title,
      body,
    });
  } catch (error: unknown) {
    logger.error("Error in testPushNotification", error);
    const message = error instanceof Error ? error.message : "Unknown error";
    res.status(500).json({
      success: false,
      error: message,
    });
  }
});
