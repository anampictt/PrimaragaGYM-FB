package com.pws.primaragagym.screens.admin.member

import androidx.compose.ui.graphics.Color

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
    val planPrice: String = "Rp 350.000"
)

enum class MemberStatus(val displayName: String) {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    SUSPENDED("Suspended")
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
    val isActive: Boolean
)

enum class PlanType(val displayName: String) {
    DAILY("Harian"),
    MONTHLY("Bulanan"),
    YEARLY("Tahunan")
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
