package com.pws.primaragagym.screens.admin.member

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

// ============================================================================
// COLORS - Match design system
// ============================================================================
object MemberColors {
    val BackgroundColor = Color(0xFFF5F7FA)
    val CardBackground = Color.White
    val TextPrimary = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF6B6B6B)
    val TextMuted = Color(0xFF9E9E9E)
    val GreenAccent = Color(0xFF32A060)
    val GreenLight = Color(0xFFE8F5E9)
    val DividerColor = Color(0xFFE8E8E8)
    val HeaderBackgroundColor = Color(0xFFE8F5E9)

    // Status colors
    val StatusActive = Color(0xFF4CAF50)
    val StatusExpiring = Color(0xFFFF9800)
    val StatusExpired = Color(0xFFF44336)
    val StatusSuspended = Color(0xFF9E9E9E)
}

// ============================================================================
// MEMBER MODEL
// ============================================================================
data class MemberUiModel(
    val id: String,
    val memberCode: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val planName: String,
    val status: MemberStatus,
    val startDate: String,
    val expiredDate: String,
    val avatarInitial: String,
    val planPrice: String = "Rp 350.000",
    val createdAt: Date? = null,
    val dateOfBirth: String = ""
)

enum class MemberStatus(val displayName: String) {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    SUSPENDED("Suspended")
}

fun parseExpiredDate(expiredDateStr: String?, createdAt: Date? = null): Date? {
    if (expiredDateStr.isNullOrBlank() || expiredDateStr.trim() == "-") return null

    val cleanStr = expiredDateStr.trim()
    val jakartaTz = TimeZone.getTimeZone("Asia/Jakarta")

    // 1. Format yang menyertakan jam dan menit
    val formatsWithTime = listOf(
        SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")),
        SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")),
        SimpleDateFormat("d MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")),
        SimpleDateFormat("d MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")),
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")),
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")),
        SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("id", "ID")),
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID")),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()),
        SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale.ENGLISH),
        SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale.ENGLISH),
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH)
    ).onEach { it.timeZone = jakartaTz }

    for (sdf in formatsWithTime) {
        try {
            val d = sdf.parse(cleanStr)
            if (d != null) {
                // Jika ada createdAt dan tanggal expired adalah 1 hari setelah waktu pendaftaran (paket 1 hari/24 jam),
                // pastikan jam expired minimal sama dengan jam pendaftaran agar genap 24 jam dari awal waktu daftar
                if (createdAt != null) {
                    val createdCal = Calendar.getInstance(jakartaTz).apply { time = createdAt }
                    val dCal = Calendar.getInstance(jakartaTz).apply { time = d }
                    val diffDays = (dCal.get(Calendar.DAY_OF_YEAR) - createdCal.get(Calendar.DAY_OF_YEAR)) +
                            (dCal.get(Calendar.YEAR) - createdCal.get(Calendar.YEAR)) * 365
                    if (diffDays == 1) {
                        val createdHour = createdCal.get(Calendar.HOUR_OF_DAY)
                        val createdMin = createdCal.get(Calendar.MINUTE)
                        val dHour = dCal.get(Calendar.HOUR_OF_DAY)
                        val dMin = dCal.get(Calendar.MINUTE)
                        if (dHour < createdHour || (dHour == createdHour && dMin < createdMin)) {
                            dCal.set(Calendar.HOUR_OF_DAY, createdHour)
                            dCal.set(Calendar.MINUTE, createdMin)
                            dCal.set(Calendar.SECOND, createdCal.get(Calendar.SECOND))
                            return dCal.time
                        }
                    }
                }
                return d
            }
        } catch (_: Exception) {}
    }

    // 2. Format tanggal saja (tanpa jam)
    val formatsDateOnly = listOf(
        SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("d MMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
    ).onEach { it.timeZone = jakartaTz }

    for (sdf in formatsDateOnly) {
        try {
            val d = sdf.parse(cleanStr)
            if (d != null) {
                val cal = Calendar.getInstance(jakartaTz).apply {
                    time = d
                    if (createdAt != null) {
                        val createdCal = Calendar.getInstance(jakartaTz).apply { time = createdAt }
                        set(Calendar.HOUR_OF_DAY, createdCal.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, createdCal.get(Calendar.MINUTE))
                        set(Calendar.SECOND, createdCal.get(Calendar.SECOND))
                        set(Calendar.MILLISECOND, 0)
                    } else {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                }
                return cal.time
            }
        } catch (_: Exception) {}
    }

    return null
}

fun resolveMemberStatus(rawStatus: String?, expiredDateStr: String?, createdAt: Date? = null): MemberStatus {
    val upper = rawStatus?.uppercase()?.trim() ?: ""
    if (upper == "SUSPENDED" || upper == "BANNED" || upper == "INACTIVE") {
        return MemberStatus.SUSPENDED
    }

    if (expiredDateStr.isNullOrBlank() || expiredDateStr.trim() == "-") {
        return if (upper == "EXPIRED") MemberStatus.EXPIRED else MemberStatus.ACTIVE
    }

    val parsedDate = parseExpiredDate(expiredDateStr, createdAt)

    if (parsedDate == null) {
        return when (upper) {
            "EXPIRED" -> MemberStatus.EXPIRED
            "EXPIRING_SOON" -> MemberStatus.EXPIRING_SOON
            "SUSPENDED" -> MemberStatus.SUSPENDED
            else -> MemberStatus.ACTIVE
        }
    }

    val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).time

    // Begitu waktu sekarang mencapai atau melewati jam expired persis -> Otomatis EXPIRED
    if (now.time >= parsedDate.time) {
        return MemberStatus.EXPIRED
    }

    val diffMillis = parsedDate.time - now.time
    // Jika sisa waktu <= 7 hari (atau paket 1 hari yang tersisa <= 24 jam) -> EXPIRING_SOON
    return if (diffMillis <= 7L * 24 * 60 * 60 * 1000L) {
        MemberStatus.EXPIRING_SOON
    } else {
        MemberStatus.ACTIVE
    }
}

fun formatExpiredDateDisplay(expiredDateStr: String?, createdAt: Date? = null): String {
    if (expiredDateStr.isNullOrBlank() || expiredDateStr.trim() == "-") return "-"
    val parsed = parseExpiredDate(expiredDateStr, createdAt) ?: return expiredDateStr
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
        timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    }
    return sdf.format(parsed)
}

// ============================================================================
// MEMBERSHIP MODEL
// ============================================================================
data class MembershipUiModel(
    val id: String,
    val memberId: String,
    val memberName: String,
    val planName: String,
    val startDate: String,
    val endDate: String,
    val status: MemberStatus,
    val price: String
)

// ============================================================================
// MEMBERSHIP PLAN MODEL
// ============================================================================
data class MembershipPlanUiModel(
    val id: String,
    val name: String,
    val type: PlanType,
    val price: String,
    val duration: String,
    val maxMembers: Int,
    val isActive: Boolean,
    val createdAt: Date? = null
)

enum class PlanType(val displayName: String) {
    DAILY("Harian"),
    MONTHLY("Bulanan"),
    YEARLY("Tahunan"),
    CUSTOM("Custom")
}

// ============================================================================
// TRANSACTION MODEL
// ============================================================================
data class TransactionUiModel(
    val id: String,
    val type: TransactionType,
    val description: String,
    val date: String,
    val amount: String,
    val status: TransactionStatus
)

enum class TransactionType(val displayName: String) {
    PERPANJANGAN("Perpanjang Membership"),
    UPGRADE("Upgrade Membership"),
    DOWNGRADE("Downgrade Membership"),
    REGISTRASI("Registrasi Member")
}

enum class TransactionStatus(val displayName: String) {
    PAID("Paid"),
    PENDING("Pending"),
    CANCELLED("Cancelled")
}

// ============================================================================
// DUMMY DATA - MEMBERS
// ============================================================================
val dummyMembers = listOf(
    MemberUiModel(
        id = "1",
        memberCode = "MBR-001",
        name = "John Smith",
        phone = "08123456789",
        email = "john@email.com",
        address = "Jl. Contoh No. 10, Jakarta",
        planName = "Premium Monthly",
        status = MemberStatus.ACTIVE,
        startDate = "01 September 2026",
        expiredDate = "30 September 2026",
        avatarInitial = "JS"
    ),
    MemberUiModel(
        id = "2",
        memberCode = "MBR-002",
        name = "Sarah Connor",
        phone = "08123456780",
        email = "sarah@email.com",
        address = "Jl. Sudirman No. 5, Jakarta",
        planName = "VIP Yearly",
        status = MemberStatus.ACTIVE,
        startDate = "01 Maret 2026",
        expiredDate = "28 Februari 2027",
        avatarInitial = "SC"
    ),
    MemberUiModel(
        id = "3",
        memberCode = "MBR-003",
        name = "Michael Chen",
        phone = "08123456781",
        email = "michael@email.com",
        address = "Jl. Gatot Subroto No. 20, Jakarta",
        planName = "Basic Monthly",
        status = MemberStatus.EXPIRING_SOON,
        startDate = "01 Agustus 2026",
        expiredDate = "05 September 2026",
        avatarInitial = "MC"
    ),
    MemberUiModel(
        id = "4",
        memberCode = "MBR-004",
        name = "Lisa Anderson",
        phone = "08123456782",
        email = "lisa@email.com",
        address = "Jl. Thamrin No. 15, Jakarta",
        planName = "Premium Monthly",
        status = MemberStatus.EXPIRED,
        startDate = "01 Juni 2026",
        expiredDate = "30 Juni 2026",
        avatarInitial = "LA"
    ),
    MemberUiModel(
        id = "5",
        memberCode = "MBR-005",
        name = "David Wilson",
        phone = "08123456783",
        email = "david@email.com",
        address = "Jl. Merdeka No. 8, Jakarta",
        planName = "Standard Monthly",
        status = MemberStatus.SUSPENDED,
        startDate = "01 Januari 2026",
        expiredDate = "31 Januari 2026",
        avatarInitial = "DW"
    ),
    MemberUiModel(
        id = "6",
        memberCode = "MBR-006",
        name = "Emma Davis",
        phone = "08123456784",
        email = "emma@email.com",
        address = "Jl. Asia Afrika No. 12, Bandung",
        planName = "VIP Monthly",
        status = MemberStatus.ACTIVE,
        startDate = "01 September 2026",
        expiredDate = "30 September 2026",
        avatarInitial = "ED"
    )
)

// ============================================================================
// DUMMY DATA - MEMBERSHIP PLANS
// ============================================================================
val dummyMembershipPlans = listOf(
    MembershipPlanUiModel(
        id = "1",
        name = "Basic Daily",
        type = PlanType.DAILY,
        price = "Rp 25.000",
        duration = "1 Hari",
        maxMembers = 50,
        isActive = true
    ),
    MembershipPlanUiModel(
        id = "2",
        name = "Standard Daily",
        type = PlanType.DAILY,
        price = "Rp 35.000",
        duration = "1 Hari",
        maxMembers = 30,
        isActive = true
    ),
    MembershipPlanUiModel(
        id = "3",
        name = "Basic Monthly",
        type = PlanType.MONTHLY,
        price = "Rp 200.000",
        duration = "30 Hari",
        maxMembers = 100,
        isActive = true
    ),
    MembershipPlanUiModel(
        id = "4",
        name = "Standard Monthly",
        type = PlanType.MONTHLY,
        price = "Rp 250.000",
        duration = "30 Hari",
        maxMembers = 80,
        isActive = true
    ),
    MembershipPlanUiModel(
        id = "5",
        name = "Premium Monthly",
        type = PlanType.MONTHLY,
        price = "Rp 350.000",
        duration = "30 Hari",
        maxMembers = 50,
        isActive = true
    ),
    MembershipPlanUiModel(
        id = "6",
        name = "VIP Monthly",
        type = PlanType.MONTHLY,
        price = "Rp 500.000",
        duration = "30 Hari",
        maxMembers = 20,
        isActive = true
    ),
    MembershipPlanUiModel(
        id = "7",
        name = "Premium Yearly",
        type = PlanType.YEARLY,
        price = "Rp 3.000.000",
        duration = "365 Hari",
        maxMembers = 30,
        isActive = true
    ),
    MembershipPlanUiModel(
        id = "8",
        name = "VIP Yearly",
        type = PlanType.YEARLY,
        price = "Rp 5.000.000",
        duration = "365 Hari",
        maxMembers = 10,
        isActive = true
    )
)

// ============================================================================
// DUMMY DATA - TRANSACTIONS
// ============================================================================
val dummyTransactions = listOf(
    TransactionUiModel(
        id = "1",
        type = TransactionType.PERPANJANGAN,
        description = "Premium Monthly",
        date = "04 September 2026",
        amount = "Rp 350.000",
        status = TransactionStatus.PAID
    ),
    TransactionUiModel(
        id = "2",
        type = TransactionType.UPGRADE,
        description = "Basic → Premium",
        date = "01 Agustus 2026",
        amount = "Rp 150.000",
        status = TransactionStatus.PAID
    ),
    TransactionUiModel(
        id = "3",
        type = TransactionType.REGISTRASI,
        description = "Standard Monthly",
        date = "15 Juli 2026",
        amount = "Rp 250.000",
        status = TransactionStatus.PAID
    ),
    TransactionUiModel(
        id = "4",
        type = TransactionType.PERPANJANGAN,
        description = "VIP Monthly",
        date = "20 Juni 2026",
        amount = "Rp 500.000",
        status = TransactionStatus.PENDING
    ),
    TransactionUiModel(
        id = "5",
        type = TransactionType.DOWNGRADE,
        description = "Premium → Basic",
        date = "10 Juni 2026",
        amount = "Rp 100.000",
        status = TransactionStatus.CANCELLED
    )
)

// ============================================================================
// WHATSAPP HELPER
// ============================================================================
fun openWhatsApp(context: Context, rawPhone: String, memberName: String = "") {
    val digits = rawPhone.filter { it.isDigit() }
    if (digits.isBlank()) {
        Toast.makeText(context, "Nomor telepon member belum tersedia", Toast.LENGTH_SHORT).show()
        return
    }

    val formattedPhone = when {
        digits.startsWith("0") -> "62" + digits.substring(1)
        digits.startsWith("62") -> digits
        else -> digits
    }

    try {
        val greeting = if (memberName.isNotBlank()) "Halo Kak $memberName, kami dari Primaraga Gym" else "Halo, kami dari Primaraga Gym"
        val encodedMessage = URLEncoder.encode(greeting, "UTF-8")
        val uri = Uri.parse("https://wa.me/$formattedPhone?text=$encodedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
