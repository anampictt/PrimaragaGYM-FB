package com.pws.primaragagym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.data.repository.*
import com.pws.primaragagym.domain.model.*
import com.pws.primaragagym.domain.repository.*
import com.pws.primaragagym.screens.admin.checkin.ActivityType
import com.pws.primaragagym.screens.admin.checkin.CheckinActivityMock
import com.pws.primaragagym.screens.admin.member.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// ADMIN DASHBOARD VIEWMODEL
// ============================================================================
data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val totalActiveMembers: Int = 0,
    val todayRevenue: String = "Rp 0",
    val newMembersToday: Int = 0,
    val todayCheckins: Int = 0,
    val weeklyCheckins: Map<String, Int> = emptyMap(),
    val unreadNotifications: Int = 0,
    val error: String? = null,
    val branchId: String = ""
)

class AdminDashboardViewModel : ViewModel() {

    private val memberRepository: MemberRepository = MemberRepositoryImpl()
    private val checkinRepository: CheckinRepository = CheckinRepositoryImpl()
    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, branchId = branchId, error = null) }

            try {
                val activeMembers = memberRepository.getActiveMembersCount(branchId)
                val newMembers = memberRepository.getNewMembersCount(branchId, Date())
                val todayRevenue = paymentRepository.getTodayRevenue(branchId)
                val todayCheckins = checkinRepository.getTodayCheckinsCount(branchId)
                val weeklyCheckins = checkinRepository.getWeeklyCheckins(branchId)
                val unreadNotifications = notificationRepository.getUnreadCount(null, branchId)

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        totalActiveMembers = activeMembers.getOrDefault(0),
                        newMembersToday = newMembers.getOrDefault(0),
                        todayRevenue = todayRevenue.getOrDefault(0L).formatCurrency(),
                        todayCheckins = todayCheckins.getOrDefault(0),
                        weeklyCheckins = weeklyCheckins.getOrDefault(emptyMap()),
                        unreadNotifications = unreadNotifications.getOrDefault(0)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun refreshDashboard() {
        val branchId = _uiState.value.branchId
        if (branchId.isNotEmpty()) {
            loadDashboard(branchId)
        }
    }
}

private fun Long.formatCurrency(): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        format.format(this)
    } catch (e: Exception) { "Rp 0" }
}

// ============================================================================
// MEMBER MANAGEMENT VIEWMODEL
// ============================================================================
data class MemberListUiState(
    val isLoading: Boolean = true,
    val members: List<MemberUiModel> = emptyList(),
    val filteredMembers: List<MemberUiModel> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: MemberStatus = MemberStatus.ACTIVE,
    val error: String? = null,
    val branchId: String = "",
    val hasMore: Boolean = true,
    val lastDocumentId: String? = null
)

class MemberListViewModel : ViewModel() {

    private val memberRepository: MemberRepository = MemberRepositoryImpl()
    private val membershipRepository: MembershipRepository = MembershipRepositoryImpl()

    private val _uiState = MutableStateFlow(MemberListUiState())
    val uiState: StateFlow<MemberListUiState> = _uiState.asStateFlow()

    fun loadMembers(branchId: String, reset: Boolean = false) {
        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(isLoading = true, members = emptyList(), lastDocumentId = null, hasMore = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val lastDocId = if (reset) null else _uiState.value.lastDocumentId
                val result = memberRepository.getMembers(branchId, limit = 25, lastDocumentId = lastDocId)

                result.onSuccess { firestoreMembers ->
                    val memberUiModels = firestoreMembers.map { member ->
                        val membership = membershipRepository.getActiveMembership(member.memberId).getOrNull()
                        member.toUiModel().copy(
                            planName = membership?.planName ?: "",
                            expiredDate = membership?.endDate?.formatDate() ?: "",
                            planPrice = membership?.price?.formatCurrency() ?: ""
                        )
                    }

                    _uiState.update { state ->
                        val newList = if (reset) memberUiModels else state.members + memberUiModels
                        val hasMore = memberUiModels.size >= 25
                        val newLastDocId = if (memberUiModels.isNotEmpty()) memberUiModels.last().id else null
                        state.copy(
                            isLoading = false,
                            members = newList,
                            filteredMembers = applyFilters(newList, state.searchQuery, state.selectedFilter),
                            lastDocumentId = newLastDocId,
                            hasMore = hasMore,
                            branchId = branchId
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchMembers(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredMembers = applyFilters(state.members, query, state.selectedFilter)
            )
        }
    }

    fun filterByStatus(status: MemberStatus) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = status,
                filteredMembers = applyFilters(state.members, state.searchQuery, status)
            )
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoading && state.hasMore && state.branchId.isNotEmpty()) {
            loadMembers(state.branchId, reset = false)
        }
    }

    private fun applyFilters(members: List<MemberUiModel>, query: String, status: MemberStatus): List<MemberUiModel> {
        return members.filter { member ->
            val matchesSearch = query.isBlank() ||
                    member.name.contains(query, ignoreCase = true) ||
                    member.memberCode.contains(query, ignoreCase = true) ||
                    member.phone.contains(query, ignoreCase = true)

            val matchesFilter = when (status) {
                MemberStatus.ACTIVE -> member.status == MemberStatus.ACTIVE
                MemberStatus.EXPIRING_SOON -> member.status == MemberStatus.EXPIRING_SOON
                MemberStatus.EXPIRED -> member.status == MemberStatus.EXPIRED
                MemberStatus.SUSPENDED -> member.status == MemberStatus.SUSPENDED
            }

            matchesSearch && matchesFilter
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// ============================================================================
// MEMBERSHIP PLAN VIEWMODEL
// ============================================================================
data class PlanListUiState(
    val isLoading: Boolean = true,
    val plans: List<MembershipPlanUiModel> = emptyList(),
    val rawPlans: List<FirestoreMembershipPlan> = emptyList(),
    val error: String? = null
)

class PlanListViewModel : ViewModel() {

    private val membershipPlanRepository: MembershipPlanRepository = MembershipPlanRepositoryImpl()

    private val _uiState = MutableStateFlow(PlanListUiState())
    val uiState: StateFlow<PlanListUiState> = _uiState.asStateFlow()

    init {
        observePlans()
    }

    fun observePlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            membershipPlanRepository.observeMembershipPlans(isActive = null)
                .catch { e ->
                    android.util.Log.e("PlanListVM", "observePlans error: ${e.message}", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { plans ->
                    android.util.Log.d("PlanListVM", "observePlans collected ${plans.size} plans")
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            plans = plans.map { it.toUiModel() },
                            rawPlans = plans,
                            error = null
                        )
                    }
                }
        }
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                membershipPlanRepository.getMembershipPlans(isActive = null)
                    .onSuccess { plans ->
                        android.util.Log.d("PlanListVM", "loadPlans loaded ${plans.size} plans")
                        _uiState.update { it.copy(
                            isLoading = false,
                            plans = plans.map { p -> p.toUiModel() },
                            rawPlans = plans,
                            error = null
                        )}
                    }
                    .onFailure { e ->
                        android.util.Log.e("PlanListVM", "loadPlans error: ${e.message}", e)
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
            } catch (e: Exception) {
                android.util.Log.e("PlanListVM", "loadPlans exception: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    suspend fun getPlanById(planId: String): Result<FirestoreMembershipPlan> {
        return membershipPlanRepository.getMembershipPlanById(planId)
    }

    fun createPlan(plan: FirestoreMembershipPlan, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            membershipPlanRepository.createMembershipPlan(plan)
                .onSuccess {
                    loadPlans()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun updatePlan(plan: FirestoreMembershipPlan, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            membershipPlanRepository.updateMembershipPlan(plan)
                .onSuccess {
                    loadPlans()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun deletePlan(planId: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            membershipPlanRepository.deleteMembershipPlan(planId)
                .onSuccess {
                    loadPlans()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }
}

// ============================================================================
// CHECKIN VIEWMODEL (FIRESTORE)
// ============================================================================
data class CheckinUiState(
    val isLoading: Boolean = true,
    val activities: List<CheckinActivityMock> = emptyList(),
    val todayCheckins: List<FirestoreCheckin> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val branchId: String = "",
    val memberToProcess: FirestoreMember? = null,
    val memberMembership: FirestoreMembership? = null
)

class CheckinFirestoreViewModel : ViewModel() {

    private val memberRepository: MemberRepository = MemberRepositoryImpl()
    private val membershipRepository: MembershipRepository = MembershipRepositoryImpl()
    private val checkinRepository: CheckinRepository = CheckinRepositoryImpl()

    private val _uiState = MutableStateFlow(CheckinUiState())
    val uiState: StateFlow<CheckinUiState> = _uiState.asStateFlow()

    fun loadTodayActivities(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, branchId = branchId) }

            try {
                val checkins = checkinRepository.getTodayCheckins(branchId)
                val recentActivities = checkinRepository.getRecentActivities(branchId, 20)

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        todayCheckins = checkins.getOrDefault(emptyList()),
                        activities = recentActivities.getOrDefault(emptyList()).map { it.toActivityMock() }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchMemberByCode(memberCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, memberToProcess = null, memberMembership = null) }

            try {
                val memberResult = memberRepository.getMemberByCode(memberCode)

                memberResult.onSuccess { member ->
                    val membership = membershipRepository.getActiveMembership(member.memberId).getOrNull()
                    val now = Date()
                    val isMembershipValid = membership != null &&
                            membership.status == "ACTIVE" &&
                            (membership.endDate?.after(now) == true)

                    val isCheckedIn = checkinRepository.isMemberCheckedIn(member.memberId).getOrDefault(false)

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            memberToProcess = member,
                            memberMembership = if (isMembershipValid) membership else null
                        )
                    }

                    when {
                        member.status != "ACTIVE" -> {
                            _uiState.update { it.copy(error = "Member tidak aktif (status: ${member.status})") }
                        }
                        !isMembershipValid -> {
                            _uiState.update { it.copy(error = "Membership sudah expire atau tidak valid") }
                        }
                        isCheckedIn -> {
                            _uiState.update { it.copy(error = "Member sudah check-in hari ini") }
                        }
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun performCheckin() {
        val member = _uiState.value.memberToProcess ?: return
        val membership = _uiState.value.memberMembership ?: return
        val branchId = _uiState.value.branchId.ifEmpty { member.branchId }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            checkinRepository.checkIn(
                memberId = member.memberId,
                memberName = member.fullName,
                memberCode = member.memberCode,
                membershipId = membership.membershipId,
                branchId = branchId
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Check-in berhasil untuk ${member.fullName}",
                        memberToProcess = null,
                        memberMembership = null
                    )
                }
                loadTodayActivities(branchId)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun performCheckout(memberId: String, memberName: String) {
        val branchId = _uiState.value.branchId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            checkinRepository.checkOut(memberId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Check-out berhasil untuk $memberName",
                            memberToProcess = null,
                            memberMembership = null
                        )
                    }
                    if (branchId.isNotEmpty()) {
                        loadTodayActivities(branchId)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun clearMember() {
        _uiState.update { it.copy(memberToProcess = null, memberMembership = null, error = null) }
    }
}

// ============================================================================
// SUPER ADMIN DASHBOARD VIEWMODEL
// ============================================================================
data class SuperAdminDashboardUiState(
    val isLoading: Boolean = true,
    val totalUsers: Int = 0,
    val totalBranches: Int = 0,
    val totalMembers: Int = 0,
    val unreadNotifications: Int = 0,
    val error: String? = null
)

class SuperAdminDashboardViewModel : ViewModel() {

    private val userAdminRepository: UserAdminRepository = UserAdminRepositoryImpl()
    private val branchRepository: BranchRepository = BranchRepositoryImpl()
    private val memberRepository: MemberRepository = MemberRepositoryImpl()
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()

    private val _uiState = MutableStateFlow(SuperAdminDashboardUiState())
    val uiState: StateFlow<SuperAdminDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val totalUsers = userAdminRepository.getUsersCount()
                val totalBranches = branchRepository.getActiveBranchesCount()
                val branches = branchRepository.getBranches()

                var totalMembers = 0
                branches.getOrNull()?.forEach { branch ->
                    totalMembers += memberRepository.getActiveMembersCount(branch.branchId).getOrDefault(0)
                }

                val unreadNotifications = notificationRepository.getUnreadCount()

                _uiState.update { it.copy(
                    isLoading = false,
                    totalUsers = totalUsers.getOrDefault(0),
                    totalBranches = totalBranches.getOrDefault(0),
                    totalMembers = totalMembers,
                    unreadNotifications = unreadNotifications.getOrDefault(0)
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

// ============================================================================
// BRANCH MANAGEMENT VIEWMODEL
// ============================================================================
data class BranchListUiState(
    val isLoading: Boolean = true,
    val branches: List<FirestoreBranch> = emptyList(),
    val filteredBranches: List<FirestoreBranch> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

class BranchListViewModel : ViewModel() {

    private val branchRepository: BranchRepository = BranchRepositoryImpl()

    private val _uiState = MutableStateFlow(BranchListUiState())
    val uiState: StateFlow<BranchListUiState> = _uiState.asStateFlow()

    init {
        observeBranches()
    }

    fun observeBranches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            branchRepository.observeBranches(isActive = null)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { branches ->
                    _uiState.update { state ->
                        val filtered = if (state.searchQuery.isBlank()) {
                            branches
                        } else {
                            branches.filter {
                                it.name.contains(state.searchQuery, ignoreCase = true) ||
                                it.address.contains(state.searchQuery, ignoreCase = true)
                            }
                        }
                        state.copy(
                            isLoading = false,
                            branches = branches,
                            filteredBranches = filtered,
                            error = null
                        )
                    }
                }
        }
    }

    fun loadBranches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                branchRepository.getBranches(isActive = null)
                    .onSuccess { branches ->
                        _uiState.update { state ->
                            val filtered = if (state.searchQuery.isBlank()) {
                                branches
                            } else {
                                branches.filter {
                                    it.name.contains(state.searchQuery, ignoreCase = true) ||
                                    it.address.contains(state.searchQuery, ignoreCase = true)
                                }
                            }
                            state.copy(
                                isLoading = false,
                                branches = branches,
                                filteredBranches = filtered
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchBranches(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.branches
            } else {
                state.branches.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredBranches = filtered)
        }
    }

    suspend fun getBranchById(branchId: String): Result<FirestoreBranch> {
        return branchRepository.getBranchById(branchId)
    }

    fun createBranch(branch: FirestoreBranch, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            branchRepository.createBranch(branch)
                .onSuccess {
                    loadBranches()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun updateBranch(branch: FirestoreBranch, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            branchRepository.updateBranch(branch)
                .onSuccess {
                    loadBranches()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun deleteBranch(branchId: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            branchRepository.deleteBranch(branchId)
                .onSuccess {
                    loadBranches()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }
}

// ============================================================================
// ROLE MANAGEMENT VIEWMODEL
// ============================================================================
data class RoleListUiState(
    val isLoading: Boolean = true,
    val roles: List<FirestoreRole> = emptyList(),
    val error: String? = null
)

class RoleListViewModel : ViewModel() {

    private val roleRepository: RoleRepository = RoleRepositoryImpl()

    private val _uiState = MutableStateFlow(RoleListUiState())
    val uiState: StateFlow<RoleListUiState> = _uiState.asStateFlow()

    init {
        observeRoles()
    }

    private fun observeRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            roleRepository.observeRoles()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { roles ->
                    _uiState.update { it.copy(isLoading = false, roles = roles, error = null) }
                }
        }
    }

    fun loadRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                roleRepository.getRoles()
                    .onSuccess { roles ->
                        _uiState.update { it.copy(isLoading = false, roles = roles) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createRole(role: FirestoreRole, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            roleRepository.createRole(role)
                .onSuccess {
                    loadRoles()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun updateRole(role: FirestoreRole, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            roleRepository.updateRole(role)
                .onSuccess {
                    loadRoles()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun updateRolePermissions(roleId: String, permissions: Map<String, Boolean>, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            roleRepository.updateRolePermissions(roleId, permissions)
                .onSuccess {
                    loadRoles()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun deleteRole(roleId: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            roleRepository.deleteRole(roleId)
                .onSuccess {
                    loadRoles()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun getRoleById(roleId: String): FirestoreRole? {
        return _uiState.value.roles.find { it.roleId == roleId }
    }
}

// ============================================================================
// USER MANAGEMENT VIEWMODEL
// ============================================================================
data class UserListUiState(
    val isLoading: Boolean = true,
    val users: List<FirestoreUser> = emptyList(),
    val filteredUsers: List<FirestoreUser> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

class UserListViewModel : ViewModel() {

    private val userAdminRepository: UserAdminRepository = UserAdminRepositoryImpl()

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userAdminRepository.observeUsers()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { users ->
                    _uiState.update { state ->
                        val filtered = if (state.searchQuery.isBlank()) {
                            users
                        } else {
                            users.filter {
                                it.displayName.contains(state.searchQuery, ignoreCase = true) ||
                                it.email.contains(state.searchQuery, ignoreCase = true) ||
                                it.resolvedRole.contains(state.searchQuery, ignoreCase = true) ||
                                it.address.contains(state.searchQuery, ignoreCase = true)
                            }
                        }
                        state.copy(isLoading = false, users = users, filteredUsers = filtered, error = null)
                    }
                }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                userAdminRepository.getUsers()
                    .onSuccess { users ->
                        _uiState.update { state ->
                            val filtered = if (state.searchQuery.isBlank()) {
                                users
                            } else {
                                users.filter {
                                    it.displayName.contains(state.searchQuery, ignoreCase = true) ||
                                    it.email.contains(state.searchQuery, ignoreCase = true) ||
                                    it.resolvedRole.contains(state.searchQuery, ignoreCase = true) ||
                                    it.address.contains(state.searchQuery, ignoreCase = true)
                                }
                            }
                            state.copy(isLoading = false, users = users, filteredUsers = filtered)
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchUsers(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.users
            } else {
                state.users.filter {
                    it.displayName.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true) ||
                    it.resolvedRole.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredUsers = filtered)
        }
    }

    fun createUser(user: FirestoreUser, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userAdminRepository.createUser(user)
                .onSuccess {
                    loadUsers()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun updateUser(user: FirestoreUser, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userAdminRepository.updateUser(user)
                .onSuccess {
                    loadUsers()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun deleteUser(userId: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userAdminRepository.deleteUser(userId)
                .onSuccess {
                    loadUsers()
                    onComplete?.invoke(true, null)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    onComplete?.invoke(false, e.message)
                }
        }
    }

    fun deactivateUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userAdminRepository.deactivateUser(userId)
                .onSuccess { loadUsers() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun getUserById(userId: String): FirestoreUser? {
        return _uiState.value.users.find { it.uid == userId }
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================
private fun FirestoreMember.toUiModel(): MemberUiModel {
    val status = when (this.status.uppercase()) {
        "ACTIVE" -> MemberStatus.ACTIVE
        "EXPIRING_SOON" -> MemberStatus.EXPIRING_SOON
        "EXPIRED" -> MemberStatus.EXPIRED
        "SUSPENDED" -> MemberStatus.SUSPENDED
        else -> MemberStatus.ACTIVE
    }

    val initials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifEmpty { "?" }

    return MemberUiModel(
        id = memberId,
        memberCode = memberCode,
        name = fullName,
        phone = phoneNumber,
        email = email,
        address = address,
        planName = "",
        status = status,
        startDate = joinedAt?.formatDate() ?: "",
        expiredDate = "",
        avatarInitial = initials
    )
}

private fun FirestoreMembershipPlan.toUiModel(): MembershipPlanUiModel {
    val planType = when {
        type.equals("DAILY", ignoreCase = true) || durationType.equals("DAY", ignoreCase = true) -> PlanType.DAILY
        type.equals("YEARLY", ignoreCase = true) || durationType.equals("YEAR", ignoreCase = true) -> PlanType.YEARLY
        else -> PlanType.MONTHLY
    }

    val durationStr = if (duration.isNotBlank()) {
        duration
    } else {
        when (planType) {
            PlanType.DAILY -> "$durationValue Hari"
            PlanType.YEARLY -> "$durationValue Tahun"
            PlanType.MONTHLY -> "$durationValue Bulan"
        }
    }

    return MembershipPlanUiModel(
        id = planId,
        name = name,
        type = planType,
        price = price.formatCurrency(),
        duration = durationStr,
        maxMembers = maxMembers ?: 0,
        isActive = isActive
    )
}

private fun FirestoreCheckin.toActivityMock(): CheckinActivityMock {
    val activityType = if (status == "CHECKED_IN") ActivityType.CHECK_IN else ActivityType.CHECK_OUT

    return CheckinActivityMock(
        memberId = memberId,
        name = memberName,
        time = checkInAt?.formatTime() ?: "",
        type = activityType
    )
}

private fun Date?.formatDate(): String {
    return try {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        this?.let { dateFormat.format(it) } ?: ""
    } catch (e: Exception) { "" }
}

private fun Date?.formatTime(): String {
    return try {
        val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        this?.let { timeFormat.format(it) } ?: "" }
    catch (e: Exception) { "" }
}

// ============================================================================
// KEUANGAN VIEWMODEL (FIRESTORE)
// ============================================================================
data class KeuanganUiState(
    val isLoading: Boolean = true,
    val todayRevenue: Long = 0,
    val monthRevenue: Long = 0,
    val todayTransactions: Int = 0,
    val error: String? = null,
    val successMessage: String? = null,
    val branchId: String = ""
)

class KeuanganViewModel : ViewModel() {

    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()
    private val reportRepository: ReportRepository = ReportRepositoryImpl()

    private val _uiState = MutableStateFlow(KeuanganUiState())
    val uiState: StateFlow<KeuanganUiState> = _uiState.asStateFlow()

    fun loadSummary(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, branchId = branchId, error = null) }

            try {
                val todayRevenue = paymentRepository.getTodayRevenue(branchId).getOrDefault(0L)
                
                val calendar = Calendar.getInstance()
                val month = calendar.get(Calendar.MONTH) + 1
                val year = calendar.get(Calendar.YEAR)
                val monthlyReport = reportRepository.getMonthlyReport(branchId, year, month).getOrNull()
                val monthRevenue = monthlyReport?.totalRevenue ?: todayRevenue // Fallback to today if month report not ready
                
                // Fetch today's transactions count
                val today = Date()
                val startOfDay = Calendar.getInstance().apply {
                    time = today
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val endOfDay = Calendar.getInstance().apply {
                    time = today
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                val paymentsResult = paymentRepository.getPaymentsByBranch(branchId, startDate = startOfDay, endDate = endOfDay, limit = 100)
                val todayTransactions = paymentsResult.getOrDefault(emptyList()).size

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        todayRevenue = todayRevenue,
                        monthRevenue = monthRevenue,
                        todayTransactions = todayTransactions
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun recordPayment(
        memberId: String,
        memberName: String,
        membershipId: String?,
        amount: Long,
        paymentMethod: String,
        paymentType: String,
        planName: String
    ) {
        val branchId = _uiState.value.branchId
        if (branchId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            paymentRepository.createPayment(
                memberId = memberId,
                memberName = memberName,
                membershipId = membershipId,
                branchId = branchId,
                amount = amount,
                paymentMethod = paymentMethod,
                paymentType = paymentType,
                planName = planName
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Pembayaran berhasil dicatat") }
                loadSummary(branchId)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

// ============================================================================
// INVOICE LIST VIEWMODEL (FIRESTORE)
// ============================================================================
data class InvoiceListUiState(
    val isLoading: Boolean = true,
    val invoices: List<FirestorePayment> = emptyList(),
    val error: String? = null,
    val branchId: String = ""
)

class InvoiceListViewModel : ViewModel() {

    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()

    private val _uiState = MutableStateFlow(InvoiceListUiState())
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()

    fun loadInvoices(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, branchId = branchId, error = null) }

            try {
                // Fetch recent payments for the branch
                val paymentsResult = paymentRepository.getPaymentsByBranch(branchId, limit = 50)
                
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        invoices = paymentsResult.getOrDefault(emptyList())
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

// ============================================================================
// LAPORAN KEUANGAN VIEWMODEL (FIRESTORE)
// ============================================================================
data class LaporanKeuanganUiState(
    val isLoading: Boolean = true,
    val totalRevenue: Long = 0,
    val totalTransactions: Int = 0,
    val averageTransaction: Long = 0,
    val methodSummaries: Map<String, Long> = emptyMap(),
    val paymentTypeSummaries: Map<String, Long> = emptyMap(),
    val dailyIncomes: Map<Int, Long> = emptyMap(),
    val error: String? = null,
    val branchId: String = ""
)

class LaporanKeuanganViewModel : ViewModel() {
    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()

    private val _uiState = MutableStateFlow(LaporanKeuanganUiState())
    val uiState: StateFlow<LaporanKeuanganUiState> = _uiState.asStateFlow()

    fun loadLaporan(branchId: String, year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, branchId = branchId, error = null) }

            try {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startDate = calendar.time
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endDate = calendar.time

                val payments = paymentRepository.getPaymentsByBranch(branchId, startDate = startDate, endDate = endDate, limit = 500)
                    .getOrDefault(emptyList())

                val totalRevenue = payments.sumOf { it.amount }
                val totalTransactions = payments.size
                val averageTransaction = if (totalTransactions > 0) totalRevenue / totalTransactions else 0L

                val methodSummaries = payments.groupBy { it.paymentMethod }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val paymentTypeSummaries = payments.groupBy { it.paymentType }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val dailyIncomes = payments.groupBy { 
                    val cal = Calendar.getInstance()
                    cal.time = it.createdAt ?: Date()
                    cal.get(Calendar.DAY_OF_MONTH)
                }.mapValues { entry -> entry.value.sumOf { it.amount } }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        totalRevenue = totalRevenue,
                        totalTransactions = totalTransactions,
                        averageTransaction = averageTransaction,
                        methodSummaries = methodSummaries,
                        paymentTypeSummaries = paymentTypeSummaries,
                        dailyIncomes = dailyIncomes
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
