package com.pws.primaragagym.domain.model

import com.pws.primaragagym.screens.admin.member.MemberStatus
import com.pws.primaragagym.screens.admin.member.parseExpiredDate
import com.pws.primaragagym.screens.admin.member.resolveMemberStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Data class untuk item member yang akan berulang tahun dalam waktu dekat.
 */
data class BirthdayMemberItem(
    val member: FirestoreMember,
    val daysRemaining: Int, // 0 = Hari ini, 1 = Besok, dst.
    val ageThisYear: Int?,
    val formattedBirthday: String // Contoh: "25 Sep"
)

/**
 * Data class untuk member aktif yang belum pernah check-in sama sekali.
 */
data class ActiveNeverCheckinMemberItem(
    val member: FirestoreMember,
    val daysSinceJoined: Long,
    val formattedJoinDate: String
)

/**
 * Data class untuk member yang sudah lama tidak aktif (membership kadaluarsa/belum diperpanjang).
 */
data class InactiveMemberItem(
    val member: FirestoreMember,
    val daysSinceExpired: Long,
    val formattedExpiredDisplay: String,
    val formattedExpiredTimeAgo: String
)

enum class MemberInsightType(val title: String) {
    BIRTHDAY("Member Akan Ulang Tahun"),
    NEVER_CHECKIN("Member Aktif Belum Check-in"),
    INACTIVE("Member Belum Perpanjang Membership")
}

/**
 * Helper parser tanggal lahir dengan dukungan beragam format tanggal input.
 */
fun parseDateOfBirth(dobStr: String?): Pair<Calendar, Int?>? {
    if (dobStr.isNullOrBlank() || dobStr.trim() == "-") return null
    var clean = dobStr.trim()
    if (clean.contains(",")) clean = clean.substringBefore(",").trim()
    if (clean.contains("T")) clean = clean.substringBefore("T").trim()

    val patterns = listOf(
        "yyyy-MM-dd",
        "dd MMMM yyyy",
        "d MMMM yyyy",
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "yyyy/MM/dd"
    )

    val locales = listOf(Locale("id", "ID"), Locale.ENGLISH, Locale.getDefault())
    for (pattern in patterns) {
        for (locale in locales) {
            try {
                val sdf = SimpleDateFormat(pattern, locale).apply { isLenient = false }
                val date = sdf.parse(clean)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    val birthYear = cal.get(Calendar.YEAR)
                    return Pair(cal, birthYear)
                }
            } catch (_: Exception) {}
        }
    }
    return null
}

/**
 * Menghitung daftar member yang akan berulang tahun dalam rentang [maxDaysAhead] hari (default 7 hari).
 */
fun calculateUpcomingBirthdays(
    members: List<FirestoreMember>,
    maxDaysAhead: Int = 7
): List<BirthdayMemberItem> {
    val jakartaTz = TimeZone.getTimeZone("Asia/Jakarta")
    val todayCal = Calendar.getInstance(jakartaTz).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayYear = todayCal.get(Calendar.YEAR)

    val birthdayDisplaySdf = SimpleDateFormat("d MMM", Locale("id", "ID")).apply {
        timeZone = jakartaTz
    }

    return members.mapNotNull { member ->
        val parsed = parseDateOfBirth(member.dateOfBirth) ?: return@mapNotNull null
        val (birthCal, birthYear) = parsed
        val birthMonth = birthCal.get(Calendar.MONTH)
        val birthDay = birthCal.get(Calendar.DAY_OF_MONTH)

        val nextBdayCal = Calendar.getInstance(jakartaTz).apply {
            set(Calendar.YEAR, todayYear)
            set(Calendar.MONTH, birthMonth)
            set(Calendar.DAY_OF_MONTH, birthDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (nextBdayCal.before(todayCal)) {
            nextBdayCal.add(Calendar.YEAR, 1)
        }

        val diffMillis = nextBdayCal.timeInMillis - todayCal.timeInMillis
        val daysRemaining = (diffMillis / (24 * 60 * 60 * 1000L)).toInt()

        if (daysRemaining in 0..maxDaysAhead) {
            val age = if (birthYear != null && birthYear > 1900) {
                nextBdayCal.get(Calendar.YEAR) - birthYear
            } else null

            BirthdayMemberItem(
                member = member,
                daysRemaining = daysRemaining,
                ageThisYear = age,
                formattedBirthday = birthdayDisplaySdf.format(nextBdayCal.time)
            )
        } else {
            null
        }
    }.sortedBy { it.daysRemaining }
}

/**
 * Menghitung member aktif (ACTIVE / EXPIRING_SOON) yang belum pernah melakukan check-in.
 */
fun calculateActiveNeverCheckin(
    members: List<FirestoreMember>,
    checkedInMemberIds: Set<String>,
    checkedInMemberCodes: Set<String>
): List<ActiveNeverCheckinMemberItem> {
    val jakartaTz = TimeZone.getTimeZone("Asia/Jakarta")
    val now = Calendar.getInstance(jakartaTz).time
    val displaySdf = SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).apply {
        timeZone = jakartaTz
    }

    return members.filter { member ->
        val resolved = resolveMemberStatus(member.status, member.expiredDate, member.createdAt)
        val isActive = (resolved == MemberStatus.ACTIVE || resolved == MemberStatus.EXPIRING_SOON)

        val id = member.memberId.trim()
        val code = member.memberCode.trim()
        val hasCheckedIn = (id.isNotBlank() && id in checkedInMemberIds) ||
                (code.isNotBlank() && code in checkedInMemberCodes)

        isActive && !hasCheckedIn
    }.map { member ->
        val joinDate = member.createdAt ?: member.joinedAt ?: parseFlexibleDate(member.startDate)
        val daysSinceJoined = if (joinDate != null) {
            ((now.time - joinDate.time) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0L)
        } else 0L

        ActiveNeverCheckinMemberItem(
            member = member,
            daysSinceJoined = daysSinceJoined,
            formattedJoinDate = if (joinDate != null) displaySdf.format(joinDate) else member.startDate.ifBlank { "-" }
        )
    }.sortedByDescending { it.daysSinceJoined }
}

/**
 * Menghitung member yang sudah lama tidak aktif (pernah aktif tapi kadaluarsa dan belum perpanjang).
 */
fun calculateLongInactiveMembers(
    members: List<FirestoreMember>
): List<InactiveMemberItem> {
    val jakartaTz = TimeZone.getTimeZone("Asia/Jakarta")
    val now = Calendar.getInstance(jakartaTz).time
    val displaySdf = SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).apply {
        timeZone = jakartaTz
    }

    return members.filter { member ->
        val resolved = resolveMemberStatus(member.status, member.expiredDate, member.createdAt)
        val rawStatus = member.status.uppercase().trim()
        val isExpiredOrInactive = resolved == MemberStatus.EXPIRED ||
                resolved == MemberStatus.SUSPENDED ||
                rawStatus == "EXPIRED" ||
                rawStatus == "INACTIVE" ||
                rawStatus == "SUSPENDED"

        isExpiredOrInactive
    }.map { member ->
        val expDate = parseExpiredDate(member.expiredDate, member.createdAt)
        val daysSinceExpired = if (expDate != null) {
            ((now.time - expDate.time) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0L)
        } else {
            // Jika tanggal expired tidak tertera, gunakan selisih dari createdAt jika ada
            val created = member.createdAt ?: member.joinedAt
            if (created != null) {
                ((now.time - created.time) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0L)
            } else 0L
        }

        val formattedExpiredDisplay = if (expDate != null) {
            displaySdf.format(expDate)
        } else {
            member.expiredDate.ifBlank { "-" }
        }

        InactiveMemberItem(
            member = member,
            daysSinceExpired = daysSinceExpired,
            formattedExpiredDisplay = formattedExpiredDisplay,
            formattedExpiredTimeAgo = formatDaysAgo(daysSinceExpired)
        )
    }.sortedByDescending { it.daysSinceExpired }
}

/**
 * Format selisih hari menjadi teks relatif bahasa Indonesia yang ramah pembaca.
 */
fun formatDaysAgo(days: Long): String {
    return when {
        days <= 0 -> "Hari ini"
        days == 1L -> "Kemarin"
        days < 7L -> "$days hari lalu"
        days < 30L -> "${days / 7} minggu lalu"
        days < 365L -> "${days / 30} bulan lalu"
        else -> "${days / 365} tahun lalu"
    }
}

/**
 * Helper format teks countdown ulang tahun.
 */
fun formatBirthdayCountdown(daysRemaining: Int): String {
    return when (daysRemaining) {
        0 -> "Hari ini! 🎂"
        1 -> "Besok"
        else -> "$daysRemaining hari lagi"
    }
}

private fun parseFlexibleDate(dateStr: String?): Date? {
    if (dateStr.isNullOrBlank() || dateStr.trim() == "-") return null
    val clean = dateStr.trim()
    val jakartaTz = TimeZone.getTimeZone("Asia/Jakarta")
    val formats = listOf(
        "dd MMMM yyyy, HH:mm 'WIB'",
        "dd MMM yyyy, HH:mm 'WIB'",
        "dd MMMM yyyy",
        "dd MMM yyyy",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy"
    )
    for (pattern in formats) {
        for (locale in listOf(Locale("id", "ID"), Locale.ENGLISH, Locale.getDefault())) {
            try {
                val sdf = SimpleDateFormat(pattern, locale).apply {
                    timeZone = jakartaTz
                    isLenient = false
                }
                val d = sdf.parse(clean)
                if (d != null) return d
            } catch (_: Exception) {}
        }
    }
    return null
}
