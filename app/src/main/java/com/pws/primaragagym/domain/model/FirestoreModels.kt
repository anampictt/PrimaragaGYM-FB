package com.pws.primaragagym.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FirestoreUser(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUrl: String? = null,
    val roleId: String = "",
    val role: String = "",
    val branchId: String? = null,
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    @ServerTimestamp
    val lastLoginAt: Date? = null
) {
    val displayName: String
        get() = name.ifBlank { fullName.ifBlank { "User" } }

    val resolvedRole: String
        get() = role.ifBlank { roleId.ifBlank { "ADMIN" } }
}

data class FirestoreRole(
    @DocumentId
    val roleId: String = "",
    val name: String = "",
    val description: String = "",
    val permissions: Map<String, Boolean> = emptyMap(),
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun hasPermission(key: String): Boolean {
        if (name.equals("Super Admin", ignoreCase = true) || name.equals("SUPER_ADMIN", ignoreCase = true)) {
            return true
        }
        return permissions[key] ?: false
    }
}

data class FirestoreBranch(
    @DocumentId
    val branchId: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    val id: String get() = branchId
}

data class FirestoreMember(
    @DocumentId
    val memberId: String = "",
    val memberCode: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val photoUrl: String? = null,
    val gender: String = "", // MALE, FEMALE
    val dateOfBirth: String = "",
    val branchId: String = "",
    val status: String = "ACTIVE", // ACTIVE, EXPIRED, SUSPENDED, INACTIVE
    val activeMembershipId: String? = null,
    val planId: String = "",
    val planName: String = "",
    val planPrice: Long = 0,
    val planType: String = "",
    val duration: String = "",
    val startDate: String = "",
    val expiredDate: String = "",
    val paymentMethod: String = "",
    val paymentProofUrl: String? = null,
    @ServerTimestamp
    val joinedAt: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    val id: String get() = memberId
    val name: String get() = fullName
    val phone: String get() = phoneNumber
    val proofUrl: String? get() = paymentProofUrl
}

data class FirestoreMembershipPlan(
    @DocumentId
    val planId: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "MONTHLY", // DAILY, MONTHLY, YEARLY
    val durationType: String = "MONTH", // DAY, MONTH, YEAR
    val durationValue: Int = 1,
    val duration: String = "",
    val price: Long = 0,
    val maxMembers: Int? = null,
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    val id: String get() = planId
}

data class FirestoreMembership(
    @DocumentId
    val membershipId: String = "",
    val memberId: String = "",
    val memberName: String = "", // Snapshot for historical data
    val planId: String = "",
    val planName: String = "", // Snapshot for historical data
    val branchId: String = "",
    @ServerTimestamp
    val startDate: Date? = null,
    @ServerTimestamp
    val endDate: Date? = null,
    val status: String = "ACTIVE", // ACTIVE, EXPIRED, SUSPENDED
    val price: Long = 0, // Snapshot for historical data
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class FirestoreCheckin(
    @DocumentId
    val checkinId: String = "",
    val memberId: String = "",
    val memberName: String = "", // Snapshot
    val memberCode: String = "", // Snapshot
    val membershipId: String = "",
    val branchId: String = "",
    @ServerTimestamp
    val checkInAt: Date? = null,
    @ServerTimestamp
    val checkOutAt: Date? = null,
    val status: String = "CHECKED_IN", // CHECKED_IN, CHECKED_OUT
    @ServerTimestamp
    val createdAt: Date? = null
)

data class FirestorePayment(
    @DocumentId
    val paymentId: String = "",
    val invoiceNumber: String = "",
    val memberId: String = "",
    val memberName: String = "", // Snapshot
    val membershipId: String? = null,
    val branchId: String = "",
    val amount: Long = 0,
    val paymentMethod: String = "CASH", // CASH, TRANSFER, QRIS
    val paymentType: String = "NEW_MEMBERSHIP", // NEW_MEMBERSHIP, RENEWAL, UPGRADE, DOWNGRADE, OTHER
    val planName: String = "",
    val proofUrl: String? = null,
    val status: String = "PAID", // PAID, PENDING, CANCELLED
    @ServerTimestamp
    val paidAt: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class FirestoreNotification(
    @DocumentId
    val notificationId: String = "",
    val userId: String? = null,
    val memberId: String? = null,
    val branchId: String? = null,
    val type: String = "SYSTEM", // MEMBERSHIP_EXPIRING, MEMBER_INACTIVE, MEMBER_BIRTHDAY, SYSTEM
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)

data class FirestoreDailyReport(
    @DocumentId
    val reportId: String = "",
    val date: String = "", // Format: yyyy-MM-dd
    val branchId: String = "",
    val totalActiveMembers: Int = 0,
    val newMembers: Int = 0,
    val totalCheckins: Int = 0,
    val totalRevenue: Long = 0,
    val cashRevenue: Long = 0,
    val transferRevenue: Long = 0,
    val qrisRevenue: Long = 0,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class FirestoreMonthlyReport(
    @DocumentId
    val reportId: String = "",
    val month: String = "", // Format: yyyy-MM
    val branchId: String = "",
    val totalNewMembers: Int = 0,
    val totalTransactions: Int = 0,
    val totalRevenue: Long = 0,
    val cashRevenue: Long = 0,
    val transferRevenue: Long = 0,
    val qrisRevenue: Long = 0,
    @ServerTimestamp
    val updatedAt: Date? = null
)
