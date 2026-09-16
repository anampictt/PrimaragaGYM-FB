package com.pws.primaragagym.domain.repository

import com.pws.primaragagym.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    suspend fun getMembers(branchId: String? = null, status: String? = null, limit: Int = 25, lastDocumentId: String? = null): Result<List<FirestoreMember>>
    suspend fun getMemberById(memberId: String): Result<FirestoreMember>
    suspend fun getMemberByCode(memberCode: String): Result<FirestoreMember>
    suspend fun searchMembers(query: String, branchId: String? = null, limit: Int = 25): Result<List<FirestoreMember>>
    suspend fun createMember(member: FirestoreMember): Result<String>
    suspend fun updateMember(member: FirestoreMember): Result<Unit>
    suspend fun updateMemberStatus(memberId: String, status: String): Result<Unit>
    suspend fun deleteMember(memberId: String): Result<Unit>
    suspend fun getActiveMembersCount(branchId: String): Result<Int>
    suspend fun getNewMembersCount(branchId: String, date: java.util.Date): Result<Int>
    suspend fun getMembersWithExpiringMembership(branchId: String, daysAhead: Int = 7): Result<List<FirestoreMember>>
    suspend fun generateNextMemberCode(): Result<String>
}

interface MembershipPlanRepository {
    fun observeMembershipPlans(isActive: Boolean? = null): Flow<List<FirestoreMembershipPlan>>
    suspend fun getMembershipPlans(isActive: Boolean? = true, limit: Int = 50): Result<List<FirestoreMembershipPlan>>
    suspend fun getMembershipPlanById(planId: String): Result<FirestoreMembershipPlan>
    suspend fun createMembershipPlan(plan: FirestoreMembershipPlan): Result<String>
    suspend fun updateMembershipPlan(plan: FirestoreMembershipPlan): Result<Unit>
    suspend fun deleteMembershipPlan(planId: String): Result<Unit>
}

interface MembershipRepository {
    suspend fun getMembershipsByMemberId(memberId: String): Result<List<FirestoreMembership>>
    suspend fun getActiveMembership(memberId: String): Result<FirestoreMembership?>
    suspend fun getMembershipById(membershipId: String): Result<FirestoreMembership>
    suspend fun createMembership(
        memberId: String,
        memberName: String,
        planId: String,
        planName: String,
        branchId: String,
        durationType: String,
        durationValue: Int,
        price: Long
    ): Result<String>
    suspend fun renewMembership(
        currentMembershipId: String,
        memberId: String,
        memberName: String,
        planId: String,
        planName: String,
        branchId: String,
        durationType: String,
        durationValue: Int,
        price: Long
    ): Result<String>
    suspend fun suspendMembership(membershipId: String, memberId: String): Result<Unit>
    suspend fun getExpiringMemberships(branchId: String, daysAhead: Int = 7): Result<List<FirestoreMembership>>
}

interface CheckinRepository {
    suspend fun checkIn(memberId: String, memberName: String, memberCode: String, membershipId: String, branchId: String): Result<String>
    suspend fun checkOut(memberId: String): Result<String>
    suspend fun getTodayCheckins(branchId: String): Result<List<FirestoreCheckin>>
    suspend fun getTodayCheckinsCount(branchId: String): Result<Int>
    suspend fun getWeeklyCheckins(branchId: String): Result<Map<String, Int>>
    suspend fun getRecentActivities(branchId: String, limit: Int = 10): Result<List<FirestoreCheckin>>
    suspend fun isMemberCheckedIn(memberId: String): Result<Boolean>
    suspend fun getMemberCheckinHistory(memberId: String, limit: Int = 50): Result<List<FirestoreCheckin>>
    suspend fun getActiveCheckinForMember(memberId: String): Result<FirestoreCheckin?>
    suspend fun getAllCheckins(branchId: String, limit: Int = 100): Result<List<FirestoreCheckin>>
}

interface PaymentRepository {
    suspend fun createPayment(
        memberId: String,
        memberName: String,
        membershipId: String?,
        branchId: String,
        amount: Long,
        paymentMethod: String,
        paymentType: String,
        planName: String = "",
        proofUrl: String? = null,
        transactionType: String = "INCOME",
        category: String = "",
        notes: String = "",
        transactionDate: java.util.Date = java.util.Date()
    ): Result<String>
    suspend fun getPaymentsByBranch(branchId: String, startDate: java.util.Date? = null, endDate: java.util.Date? = null, limit: Int = 50, lastDocumentId: String? = null): Result<List<FirestorePayment>>
    suspend fun getPaymentsByMember(memberId: String): Result<List<FirestorePayment>>
    suspend fun getTodayRevenue(branchId: String): Result<Long>
    suspend fun getTodayExpense(branchId: String): Result<Long>
    suspend fun getTodayRevenueByMethod(branchId: String): Result<Map<String, Long>>
    suspend fun getPaymentById(paymentId: String): Result<FirestorePayment>
    suspend fun cancelPayment(paymentId: String): Result<Unit>
}

interface BranchRepository {
    fun observeBranches(isActive: Boolean? = null): Flow<List<FirestoreBranch>>
    suspend fun getBranches(isActive: Boolean? = true): Result<List<FirestoreBranch>>
    suspend fun getBranchById(branchId: String): Result<FirestoreBranch>
    suspend fun createBranch(branch: FirestoreBranch): Result<String>
    suspend fun updateBranch(branch: FirestoreBranch): Result<Unit>
    suspend fun deleteBranch(branchId: String): Result<Unit>
    suspend fun getActiveBranchesCount(): Result<Int>
}

interface RoleRepository {
    fun observeRoles(): Flow<List<FirestoreRole>>
    suspend fun getRoles(isActive: Boolean? = true): Result<List<FirestoreRole>>
    suspend fun getRoleById(roleId: String): Result<FirestoreRole>
    suspend fun getRoleByName(roleName: String): Result<FirestoreRole>
    suspend fun createRole(role: FirestoreRole): Result<String>
    suspend fun updateRole(role: FirestoreRole): Result<Unit>
    suspend fun updateRolePermissions(roleId: String, permissions: Map<String, Boolean>): Result<Unit>
    suspend fun deleteRole(roleId: String): Result<Unit>
}

interface UserAdminRepository {
    fun observeUsers(): Flow<List<FirestoreUser>>
    suspend fun getUsers(roleId: String? = null, branchId: String? = null, isActive: Boolean? = null, limit: Int = 100, lastDocumentId: String? = null): Result<List<FirestoreUser>>
    suspend fun getUserById(userId: String): Result<FirestoreUser>
    suspend fun createUser(user: FirestoreUser): Result<String>
    suspend fun updateUser(user: FirestoreUser): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun deactivateUser(userId: String): Result<Unit>
    suspend fun getUsersCount(branchId: String? = null): Result<Int>
    suspend fun searchUsers(query: String, limit: Int = 25): Result<List<FirestoreUser>>
}

interface NotificationRepository {
    suspend fun getNotifications(userId: String? = null, branchId: String? = null, isRead: Boolean? = null, limit: Int = 50): Result<List<FirestoreNotification>>
    suspend fun getUnreadCount(userId: String? = null, branchId: String? = null): Result<Int>
    suspend fun createNotification(userId: String? = null, memberId: String? = null, branchId: String? = null, type: String, title: String, message: String): Result<String>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(userId: String? = null, branchId: String? = null): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
}

interface ReportRepository {
    suspend fun getDailyReport(branchId: String, date: java.util.Date): Result<FirestoreDailyReport?>
    suspend fun getMonthlyReport(branchId: String, year: Int, month: Int): Result<FirestoreMonthlyReport?>
    suspend fun refreshDailyReport(branchId: String, date: java.util.Date): Result<FirestoreDailyReport>
}
