package com.pws.primaragagym.screens.admin.checkin

enum class MembershipStatus(val label: String) {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    SUSPENDED("Suspended")
}

enum class CheckinStatus(val label: String) {
    NOT_CHECKED_IN("Belum Check-in"),
    CHECKED_IN("Sedang Berada di Gym"),
    CHECKED_OUT("Sudah Check-out")
}

data class MemberMock(
    val memberId: String,
    val name: String,
    val membershipPlan: String,
    val membershipStatus: MembershipStatus,
    val checkinStatus: CheckinStatus = CheckinStatus.NOT_CHECKED_IN,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val expiredDate: String = "30 September 2026",
    val joinDate: String = "01 September 2026"
)

enum class ActivityType {
    CHECK_IN, CHECK_OUT
}

data class CheckinActivityMock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val memberId: String,
    val name: String,
    val time: String,
    val type: ActivityType
)
