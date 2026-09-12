package com.pws.primaragagym.data.repository

import com.pws.primaragagym.data.datasource.*
import com.pws.primaragagym.domain.model.*
import com.pws.primaragagym.domain.repository.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

class MemberRepositoryImpl : MemberRepository {
    private val dataSource = FirebaseMemberDataSource()

    override suspend fun getMembers(branchId: String?, status: String?, limit: Int, lastDocumentId: String?): Result<List<FirestoreMember>> =
        dataSource.getMembers(branchId, status, limit, lastDocumentId)

    override suspend fun getMemberById(memberId: String): Result<FirestoreMember> =
        dataSource.getMemberById(memberId)

    override suspend fun getMemberByCode(memberCode: String): Result<FirestoreMember> =
        dataSource.getMemberByCode(memberCode)

    override suspend fun searchMembers(query: String, branchId: String?, limit: Int): Result<List<FirestoreMember>> =
        dataSource.searchMembers(query, branchId, limit)

    override suspend fun createMember(member: FirestoreMember): Result<String> =
        dataSource.createMember(member)

    override suspend fun updateMember(member: FirestoreMember): Result<Unit> =
        dataSource.updateMember(member)

    override suspend fun deleteMember(memberId: String): Result<Unit> =
        dataSource.deleteMember(memberId)

    override suspend fun getActiveMembersCount(branchId: String): Result<Int> =
        dataSource.getActiveMembersCount(branchId)

    override suspend fun getNewMembersCount(branchId: String, date: Date): Result<Int> =
        dataSource.getNewMembersCount(branchId, date)

    override suspend fun getMembersWithExpiringMembership(branchId: String, daysAhead: Int): Result<List<FirestoreMember>> =
        dataSource.getMembersWithExpiringMembership(branchId, daysAhead)
}

class MembershipPlanRepositoryImpl : MembershipPlanRepository {
    private val dataSource = FirebaseMembershipPlanDataSource()

    override suspend fun getMembershipPlans(isActive: Boolean?, limit: Int): Result<List<FirestoreMembershipPlan>> =
        dataSource.getMembershipPlans(isActive, limit)

    override suspend fun getMembershipPlanById(planId: String): Result<FirestoreMembershipPlan> =
        dataSource.getMembershipPlanById(planId)

    override suspend fun createMembershipPlan(plan: FirestoreMembershipPlan): Result<String> =
        dataSource.createMembershipPlan(plan)

    override suspend fun updateMembershipPlan(plan: FirestoreMembershipPlan): Result<Unit> =
        dataSource.updateMembershipPlan(plan)

    override suspend fun deleteMembershipPlan(planId: String): Result<Unit> =
        dataSource.deleteMembershipPlan(planId)
}

class MembershipRepositoryImpl : MembershipRepository {
    private val dataSource = FirebaseMembershipDataSource()

    override suspend fun getMembershipsByMemberId(memberId: String): Result<List<FirestoreMembership>> =
        dataSource.getMembershipsByMemberId(memberId)

    override suspend fun getActiveMembership(memberId: String): Result<FirestoreMembership?> =
        dataSource.getActiveMembership(memberId)

    override suspend fun getMembershipById(membershipId: String): Result<FirestoreMembership> =
        dataSource.getMembershipById(membershipId)

    override suspend fun createMembership(
        memberId: String,
        memberName: String,
        planId: String,
        planName: String,
        branchId: String,
        durationType: String,
        durationValue: Int,
        price: Long
    ): Result<String> = dataSource.createMembership(memberId, memberName, planId, planName, branchId, durationType, durationValue, price)

    override suspend fun renewMembership(
        currentMembershipId: String,
        memberId: String,
        memberName: String,
        planId: String,
        planName: String,
        branchId: String,
        durationType: String,
        durationValue: Int,
        price: Long
    ): Result<String> = dataSource.renewMembership(currentMembershipId, memberId, memberName, planId, planName, branchId, durationType, durationValue, price)

    override suspend fun suspendMembership(membershipId: String, memberId: String): Result<Unit> =
        dataSource.suspendMembership(membershipId, memberId)

    override suspend fun getExpiringMemberships(branchId: String, daysAhead: Int): Result<List<FirestoreMembership>> =
        dataSource.getExpiringMemberships(branchId, daysAhead)
}

class CheckinRepositoryImpl : CheckinRepository {
    private val dataSource = FirebaseCheckinDataSource()

    override suspend fun checkIn(memberId: String, memberName: String, memberCode: String, membershipId: String, branchId: String): Result<String> =
        dataSource.checkIn(memberId, memberName, memberCode, membershipId, branchId)

    override suspend fun checkOut(memberId: String): Result<String> =
        dataSource.checkOut(memberId)

    override suspend fun getTodayCheckins(branchId: String): Result<List<FirestoreCheckin>> =
        dataSource.getTodayCheckins(branchId)

    override suspend fun getTodayCheckinsCount(branchId: String): Result<Int> =
        dataSource.getTodayCheckinsCount(branchId)

    override suspend fun getWeeklyCheckins(branchId: String): Result<Map<String, Int>> =
        dataSource.getWeeklyCheckins(branchId)

    override suspend fun getRecentActivities(branchId: String, limit: Int): Result<List<FirestoreCheckin>> =
        dataSource.getRecentActivities(branchId, limit)

    override suspend fun isMemberCheckedIn(memberId: String): Result<Boolean> =
        dataSource.isMemberCheckedIn(memberId)

    override suspend fun getMemberCheckinHistory(memberId: String, limit: Int): Result<List<FirestoreCheckin>> =
        dataSource.getMemberCheckinHistory(memberId, limit)
}

class PaymentRepositoryImpl : PaymentRepository {
    private val dataSource = FirebasePaymentDataSource()

    override suspend fun createPayment(
        memberId: String,
        memberName: String,
        membershipId: String?,
        branchId: String,
        amount: Long,
        paymentMethod: String,
        paymentType: String,
        planName: String
    ): Result<String> = dataSource.createPayment(memberId, memberName, membershipId, branchId, amount, paymentMethod, paymentType, planName)

    override suspend fun getPaymentsByBranch(branchId: String, startDate: Date?, endDate: Date?, limit: Int, lastDocumentId: String?): Result<List<FirestorePayment>> =
        dataSource.getPaymentsByBranch(branchId, startDate, endDate, limit, lastDocumentId)

    override suspend fun getPaymentsByMember(memberId: String): Result<List<FirestorePayment>> =
        dataSource.getPaymentsByMember(memberId)

    override suspend fun getTodayRevenue(branchId: String): Result<Long> =
        dataSource.getTodayRevenue(branchId)

    override suspend fun getTodayRevenueByMethod(branchId: String): Result<Map<String, Long>> =
        dataSource.getTodayRevenueByMethod(branchId)

    override suspend fun getPaymentById(paymentId: String): Result<FirestorePayment> =
        dataSource.getPaymentById(paymentId)

    override suspend fun cancelPayment(paymentId: String): Result<Unit> =
        dataSource.cancelPayment(paymentId)
}

class BranchRepositoryImpl : BranchRepository {
    private val dataSource = FirebaseBranchDataSource()

    override suspend fun getBranches(isActive: Boolean?): Result<List<FirestoreBranch>> =
        dataSource.getBranches(isActive)

    override suspend fun getBranchById(branchId: String): Result<FirestoreBranch> =
        dataSource.getBranchById(branchId)

    override suspend fun createBranch(branch: FirestoreBranch): Result<String> =
        dataSource.createBranch(branch)

    override suspend fun updateBranch(branch: FirestoreBranch): Result<Unit> =
        dataSource.updateBranch(branch)

    override suspend fun deleteBranch(branchId: String): Result<Unit> =
        dataSource.deleteBranch(branchId)

    override suspend fun getActiveBranchesCount(): Result<Int> =
        dataSource.getActiveBranchesCount()
}

class RoleRepositoryImpl : RoleRepository {
    private val dataSource = FirebaseRoleDataSource()

    override suspend fun getRoles(isActive: Boolean?): Result<List<FirestoreRole>> =
        dataSource.getRoles(isActive)

    override suspend fun getRoleById(roleId: String): Result<FirestoreRole> =
        dataSource.getRoleById(roleId)

    override suspend fun getRoleByName(roleName: String): Result<FirestoreRole> =
        dataSource.getRoleByName(roleName)

    override suspend fun createRole(role: FirestoreRole): Result<String> =
        dataSource.createRole(role)

    override suspend fun updateRole(role: FirestoreRole): Result<Unit> =
        dataSource.updateRole(role)

    override suspend fun deleteRole(roleId: String): Result<Unit> =
        dataSource.deleteRole(roleId)
}

class UserAdminRepositoryImpl : UserAdminRepository {
    private val dataSource = FirebaseUserAdminDataSource()

    override fun observeUsers(): Flow<List<FirestoreUser>> =
        dataSource.observeUsers()

    override suspend fun getUsers(roleId: String?, branchId: String?, isActive: Boolean?, limit: Int, lastDocumentId: String?): Result<List<FirestoreUser>> =
        dataSource.getUsers(roleId, branchId, isActive, limit, lastDocumentId)

    override suspend fun getUserById(userId: String): Result<FirestoreUser> =
        dataSource.getUserById(userId)

    override suspend fun createUser(user: FirestoreUser): Result<String> =
        dataSource.createUser(user)

    override suspend fun updateUser(user: FirestoreUser): Result<Unit> =
        dataSource.updateUser(user)

    override suspend fun deleteUser(userId: String): Result<Unit> =
        dataSource.deleteUser(userId)

    override suspend fun deactivateUser(userId: String): Result<Unit> =
        dataSource.deactivateUser(userId)

    override suspend fun getUsersCount(branchId: String?): Result<Int> =
        dataSource.getUsersCount(branchId)

    override suspend fun searchUsers(query: String, limit: Int): Result<List<FirestoreUser>> =
        dataSource.searchUsers(query, limit)
}

class NotificationRepositoryImpl : NotificationRepository {
    private val dataSource = FirebaseNotificationDataSource()

    override suspend fun getNotifications(userId: String?, branchId: String?, isRead: Boolean?, limit: Int): Result<List<FirestoreNotification>> =
        dataSource.getNotifications(userId, branchId, isRead, limit)

    override suspend fun getUnreadCount(userId: String?, branchId: String?): Result<Int> =
        dataSource.getUnreadCount(userId, branchId)

    override suspend fun createNotification(userId: String?, memberId: String?, branchId: String?, type: String, title: String, message: String): Result<String> =
        dataSource.createNotification(userId, memberId, branchId, type, title, message)

    override suspend fun markAsRead(notificationId: String): Result<Unit> =
        dataSource.markAsRead(notificationId)

    override suspend fun markAllAsRead(userId: String?, branchId: String?): Result<Unit> =
        dataSource.markAllAsRead(userId, branchId)

    override suspend fun deleteNotification(notificationId: String): Result<Unit> =
        dataSource.deleteNotification(notificationId)
}

class ReportRepositoryImpl : ReportRepository {
    private val dataSource = FirebaseReportDataSource()

    override suspend fun getDailyReport(branchId: String, date: Date): Result<FirestoreDailyReport?> =
        dataSource.getDailyReport(branchId, date)

    override suspend fun getMonthlyReport(branchId: String, year: Int, month: Int): Result<FirestoreMonthlyReport?> =
        dataSource.getMonthlyReport(branchId, year, month)

    override suspend fun refreshDailyReport(branchId: String, date: Date): Result<FirestoreDailyReport> =
        dataSource.refreshDailyReport(branchId, date)
}
