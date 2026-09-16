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
    val rawMembers: List<FirestoreMember> = emptyList(),
    val filteredMembers: List<MemberUiModel> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: MemberStatus? = null,
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

    fun loadMembers(branchId: String? = null, reset: Boolean = false) {
        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(isLoading = true, members = emptyList(), rawMembers = emptyList(), lastDocumentId = null, hasMore = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val effectiveBranchId = branchId?.ifBlank { null }
                val result = memberRepository.getMembers(effectiveBranchId, limit = 100)

                result.onSuccess { firestoreMembers ->
                    val memberUiModels = firestoreMembers.map { member ->
                        val initialUiModel = member.toUiModel()
                        val finalUiModel = if (initialUiModel.planName.isBlank()) {
                            val membership = membershipRepository.getActiveMembership(member.memberId).getOrNull()
                            initialUiModel.copy(
                                planName = membership?.planName ?: "",
                                expiredDate = membership?.endDate?.formatDate() ?: "",
                                planPrice = membership?.price?.formatCurrency() ?: ""
                            )
                        } else {
                            initialUiModel
                        }
                        val resolvedStatus = resolveMemberStatus(member.status, finalUiModel.expiredDate, member.createdAt ?: finalUiModel.createdAt)
                        finalUiModel.copy(status = resolvedStatus)
                    }

                    _uiState.update { state ->
                        val newList = if (reset) memberUiModels else state.members + memberUiModels
                        val newRawList = if (reset) firestoreMembers else state.rawMembers + firestoreMembers
                        state.copy(
                            isLoading = false,
                            members = newList,
                            rawMembers = newRawList,
                            filteredMembers = applyFilters(newList, state.searchQuery, state.selectedFilter),
                            branchId = branchId ?: ""
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

    suspend fun getMemberById(memberId: String): Result<FirestoreMember> {
        val cached = _uiState.value.rawMembers.find { it.memberId == memberId || it.id == memberId }
        if (cached != null) return Result.success(cached)
        return memberRepository.getMemberById(memberId)
    }

    suspend fun getNextMemberCode(): String {
        return memberRepository.generateNextMemberCode().getOrDefault("PRMG-001")
    }

    fun createMember(member: FirestoreMember, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val result = memberRepository.createMember(member)
                result.fold(
                    onSuccess = { createdId ->
                        loadMembers(_uiState.value.branchId, reset = true)
                        onComplete?.invoke(true, createdId)
                    },
                    onFailure = { error ->
                        onComplete?.invoke(false, error.message)
                    }
                )
            } catch (e: Exception) {
                onComplete?.invoke(false, e.message)
            }
        }
    }

    fun updateMember(member: FirestoreMember, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val result = memberRepository.updateMember(member)
                result.fold(
                    onSuccess = {
                        loadMembers(_uiState.value.branchId, reset = true)
                        onComplete?.invoke(true, null)
                    },
                    onFailure = { error ->
                        onComplete?.invoke(false, error.message)
                    }
                )
            } catch (e: Exception) {
                onComplete?.invoke(false, e.message)
            }
        }
    }

    fun deleteMember(memberId: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val result = memberRepository.deleteMember(memberId)
                result.fold(
                    onSuccess = {
                        loadMembers(_uiState.value.branchId, reset = true)
                        onComplete?.invoke(true, null)
                    },
                    onFailure = { error ->
                        onComplete?.invoke(false, error.message)
                    }
                )
            } catch (e: Exception) {
                onComplete?.invoke(false, e.message)
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

    fun filterByStatus(status: MemberStatus?) {
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

    private fun applyFilters(members: List<MemberUiModel>, query: String, status: MemberStatus?): List<MemberUiModel> {
        val filtered = members.filter { member ->
            val matchesSearch = query.isBlank() ||
                    member.name.contains(query, ignoreCase = true) ||
                    member.memberCode.contains(query, ignoreCase = true) ||
                    member.phone.contains(query, ignoreCase = true)

            val matchesFilter = when (status) {
                null -> true
                MemberStatus.ACTIVE -> member.status == MemberStatus.ACTIVE || member.status == MemberStatus.EXPIRING_SOON
                MemberStatus.EXPIRING_SOON -> member.status == MemberStatus.EXPIRING_SOON
                MemberStatus.EXPIRED -> member.status == MemberStatus.EXPIRED
                MemberStatus.SUSPENDED -> member.status == MemberStatus.SUSPENDED
            }

            matchesSearch && matchesFilter
        }

        // Urutkan: Active & Expiring Soon berada di atas, Expired dan Suspended di posisi bawah
        return filtered.sortedWith(
            compareBy<MemberUiModel> {
                when (it.status) {
                    MemberStatus.ACTIVE -> 0
                    MemberStatus.EXPIRING_SOON -> 1
                    MemberStatus.EXPIRED -> 2
                    MemberStatus.SUSPENDED -> 3
                }
            }.thenByDescending {
                it.createdAt?.time ?: 0L
            }
        )
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
// CHECKIN VIEWMODEL (FIRESTORE)
// ============================================================================
data class CheckinUiState(
    val isLoading: Boolean = false,
    val isSearchingMember: Boolean = false,
    val activities: List<CheckinActivityMock> = emptyList(),
    val todayCheckins: List<FirestoreCheckin> = emptyList(),
    val allCheckins: List<FirestoreCheckin> = emptyList(),
    val allMembers: List<FirestoreMember> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val branchId: String = "",
    val memberToProcess: FirestoreMember? = null,
    val memberMembership: FirestoreMembership? = null,
    val activeCheckin: FirestoreCheckin? = null,
    val isCheckedIn: Boolean = false
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
                val recentActivities = checkinRepository.getRecentActivities(branchId, 25)

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

    fun loadAllCheckins(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, branchId = branchId) }

            try {
                val allCheckins = checkinRepository.getAllCheckins(branchId, 150)
                val todayCheckins = checkinRepository.getTodayCheckins(branchId)
                val allMembers = memberRepository.getMembers(branchId = branchId.ifBlank { null }, limit = 200)

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        allCheckins = allCheckins.getOrDefault(emptyList()),
                        todayCheckins = todayCheckins.getOrDefault(emptyList()),
                        allMembers = allMembers.getOrDefault(emptyList())
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearSearchState() {
        _uiState.update {
            it.copy(
                isSearchingMember = false,
                memberToProcess = null,
                memberMembership = null,
                activeCheckin = null,
                isCheckedIn = false,
                error = null
            )
        }
    }

    fun searchMemberByCode(memberCodeOrId: String, branchId: String = "") {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSearchingMember = true,
                    error = null,
                    memberToProcess = null,
                    memberMembership = null,
                    activeCheckin = null,
                    isCheckedIn = false,
                    branchId = if (branchId.isNotEmpty()) branchId else it.branchId
                )
            }

            try {
                val cleanCode = memberCodeOrId.trim().removePrefix("PRIMARAGA_MEMBER:")

                // Coba cari berdasarkan memberCode terlebih dahulu
                var member = memberRepository.getMemberByCode(cleanCode).getOrNull()

                // Jika tidak ditemukan, coba cari berdasarkan memberId langsung
                if (member == null) {
                    member = memberRepository.getMemberById(cleanCode).getOrNull()
                }

                if (member == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSearchingMember = false,
                            error = "Kode barcode / ID '$cleanCode' tidak terdaftar di database."
                        )
                    }
                    return@launch
                }

                val membership = membershipRepository.getActiveMembership(member.memberId).getOrNull()
                val activeCheckin = checkinRepository.getActiveCheckinForMember(member.memberId).getOrNull()
                val isCheckedIn = activeCheckin != null

                // Validasi masa aktif membership
                val isExpiredByDate = isDateExpired(member.expiredDate)
                val isMembershipActive = when {
                    member.status.equals("EXPIRED", ignoreCase = true) -> false
                    member.status.equals("SUSPENDED", ignoreCase = true) -> false
                    member.status.equals("INACTIVE", ignoreCase = true) -> false
                    isExpiredByDate -> false
                    membership != null -> membership.status == "ACTIVE" && (membership.endDate?.after(Date()) ?: true)
                    else -> member.status.equals("ACTIVE", ignoreCase = true)
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isSearchingMember = false,
                        memberToProcess = member,
                        memberMembership = membership,
                        activeCheckin = activeCheckin,
                        isCheckedIn = isCheckedIn
                    )
                }

                when {
                    member.status.equals("SUSPENDED", ignoreCase = true) -> {
                        _uiState.update { it.copy(error = "Status member sedang di-suspend.") }
                    }
                    !isMembershipActive -> {
                        _uiState.update { it.copy(error = "Membership member sudah kadaluarsa (Expired).") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isSearchingMember = false, 
                        error = e.message ?: "Terjadi kesalahan saat mencari member."
                    ) 
                }
            }
        }
    }

    fun performCheckin(branchIdParam: String = "") {
        val member = _uiState.value.memberToProcess ?: return
        if (member.status.equals("SUSPENDED", ignoreCase = true) || member.status.equals("BANNED", ignoreCase = true)) {
            _uiState.update { it.copy(isLoading = false, error = "Member sedang di-suspend dan tidak dapat melakukan check-in.") }
            return
        }
        val branchId = branchIdParam.ifEmpty { _uiState.value.branchId.ifEmpty { member.branchId } }
        val membershipId = _uiState.value.memberMembership?.membershipId ?: member.activeMembershipId ?: member.planId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            checkinRepository.checkIn(
                memberId = member.memberId,
                memberName = member.fullName,
                memberCode = member.memberCode,
                membershipId = membershipId,
                branchId = branchId
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Check-in berhasil untuk ${member.fullName} (${member.memberCode})",
                        isCheckedIn = true
                    )
                }
                if (branchId.isNotEmpty()) {
                    loadTodayActivities(branchId)
                }
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
                            isCheckedIn = false,
                            activeCheckin = null
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

    private fun isDateExpired(dateStr: String?): Boolean {
        if (dateStr.isNullOrBlank() || dateStr == "-") return false
        val parsed = parseExpiredDate(dateStr) ?: return false
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).time
        return now.time >= parsed.time
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
    val todayRevenue: String = "Rp 0",
    val unreadNotifications: Int = 0,
    val birthdayMembers: List<BirthdayMemberItem> = emptyList(),
    val activeNeverCheckinMembers: List<ActiveNeverCheckinMemberItem> = emptyList(),
    val inactiveMembers: List<InactiveMemberItem> = emptyList(),
    val error: String? = null
)

class SuperAdminDashboardViewModel : ViewModel() {

    private val userAdminRepository: UserAdminRepository = UserAdminRepositoryImpl()
    private val memberRepository: MemberRepository = MemberRepositoryImpl()
    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
    private val checkinRepository: CheckinRepository = CheckinRepositoryImpl()

    private val _uiState = MutableStateFlow(SuperAdminDashboardUiState())
    val uiState: StateFlow<SuperAdminDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val totalUsers = userAdminRepository.getUsersCount()
                val totalMembers = memberRepository.getActiveMembersCount("").getOrDefault(0)
                val todayRevenue = paymentRepository.getTodayRevenue("").getOrDefault(0L)
                val unreadNotifications = notificationRepository.getUnreadCount()

                // Member insights data
                val allMembers = memberRepository.getMembers(limit = 500).getOrDefault(emptyList())
                val allCheckins = checkinRepository.getAllCheckins("", limit = 1000).getOrDefault(emptyList())

                val checkedInMemberIds = HashSet<String>()
                val checkedInMemberCodes = HashSet<String>()
                allCheckins.forEach { checkin ->
                    if (checkin.memberId.isNotBlank()) checkedInMemberIds.add(checkin.memberId.trim())
                    if (checkin.memberCode.isNotBlank()) checkedInMemberCodes.add(checkin.memberCode.trim())
                }

                val birthdayMembers = calculateUpcomingBirthdays(allMembers, maxDaysAhead = 7)
                val activeNeverCheckinMembers = calculateActiveNeverCheckin(allMembers, checkedInMemberIds, checkedInMemberCodes)
                val inactiveMembers = calculateLongInactiveMembers(allMembers)

                _uiState.update { it.copy(
                    isLoading = false,
                    totalUsers = totalUsers.getOrDefault(0),
                    totalBranches = 0,
                    totalMembers = totalMembers,
                    todayRevenue = todayRevenue.formatCurrency(),
                    unreadNotifications = unreadNotifications.getOrDefault(0),
                    birthdayMembers = birthdayMembers,
                    activeNeverCheckinMembers = activeNeverCheckinMembers,
                    inactiveMembers = inactiveMembers
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
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
    val status = resolveMemberStatus(this.status, this.expiredDate, this.createdAt ?: this.joinedAt)

    val initials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifEmpty { "?" }

    val priceStr = if (planPrice > 0) {
        "Rp " + java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(planPrice)
    } else {
        "Rp 0"
    }

        return MemberUiModel(
            id = memberId,
            memberCode = memberCode,
            name = fullName,
            phone = phoneNumber,
            email = email,
            address = address,
            planName = planName,
            status = status,
            startDate = startDate.ifEmpty { joinedAt?.formatDate() ?: "" },
            expiredDate = expiredDate,
            avatarInitial = initials,
            planPrice = priceStr,
            createdAt = createdAt ?: joinedAt,
            dateOfBirth = dateOfBirth,
            photoUrl = photoUrl,
            paymentProofUrl = paymentProofUrl
        )
    }

private fun FirestoreMembershipPlan.toUiModel(): MembershipPlanUiModel {
    val planType = when {
        type.equals("DAILY", ignoreCase = true) || (durationType.equals("DAY", ignoreCase = true) && durationValue == 1) -> PlanType.DAILY
        type.equals("YEARLY", ignoreCase = true) || durationType.equals("YEAR", ignoreCase = true) -> PlanType.YEARLY
        type.equals("CUSTOM", ignoreCase = true) || durationType.equals("CUSTOM", ignoreCase = true) || (durationType.equals("DAY", ignoreCase = true) && durationValue != 30) -> PlanType.CUSTOM
        else -> PlanType.MONTHLY
    }

    val durationStr = if (duration.isNotBlank()) {
        duration
    } else {
        when (planType) {
            PlanType.DAILY -> "$durationValue Hari"
            PlanType.YEARLY -> "$durationValue Tahun"
            PlanType.MONTHLY -> "$durationValue Bulan"
            PlanType.CUSTOM -> "$durationValue Hari"
        }
    }

    return MembershipPlanUiModel(
        id = planId,
        name = name,
        type = planType,
        price = price.formatCurrency(),
        duration = durationStr,
        maxMembers = maxMembers ?: 0,
        isActive = isActive,
        createdAt = createdAt
    )
}

private fun FirestoreCheckin.toActivityMock(): CheckinActivityMock {
    val activityType = if (status == "CHECKED_IN") ActivityType.CHECK_IN else ActivityType.CHECK_OUT
    val displayDate = if (status == "CHECKED_OUT") checkOutAt ?: checkInAt else checkInAt

    return CheckinActivityMock(
        memberId = memberCode.ifBlank { memberId },
        name = memberName,
        time = displayDate?.formatTime() ?: "",
        type = activityType
    )
}

private fun Date?.formatDate(): String {
    return try {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
        this?.let { dateFormat.format(it) } ?: ""
    } catch (e: Exception) { "" }
}

private fun Date?.formatTime(): String {
    return try {
        val timeFormat = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
        this?.let { timeFormat.format(it) } ?: ""
    } catch (e: Exception) { "" }
}

// ============================================================================
// KEUANGAN VIEWMODEL (FIRESTORE)
// ============================================================================
data class FinancialChartPoint(
    val label: String,
    val income: Long,
    val expense: Long,
    val net: Long = income - expense
)

data class KeuanganUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val todayRevenue: Long = 0,             // Pendapatan Kotor Hari Ini
    val todayExpense: Long = 0,             // Pengeluaran Hari Ini
    val todayNetProfit: Long = 0,           // Laba Bersih Hari Ini (Revenue - Expense)
    val monthRevenue: Long = 0,
    val todayTransactions: Int = 0,
    val chartFilter: String = "HARI_INI",     // "HARI_INI", "7_HARI", "BULAN_INI", "PILIH_TANGGAL"
    val selectedCustomDate: Date? = null,
    val chartPoints: List<FinancialChartPoint> = emptyList(),
    val filteredTotalIncome: Long = 0,
    val filteredTotalExpense: Long = 0,
    val filteredNetProfit: Long = 0,
    val recentTransactions: List<FirestorePayment> = emptyList(),
    val filteredPayments: List<FirestorePayment> = emptyList(),
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
                val todayExpense = paymentRepository.getTodayExpense(branchId).getOrDefault(0L)
                val todayNetProfit = todayRevenue - todayExpense
                
                val calendar = Calendar.getInstance()
                val month = calendar.get(Calendar.MONTH) + 1
                val year = calendar.get(Calendar.YEAR)
                val monthlyReport = reportRepository.getMonthlyReport(branchId, year, month).getOrNull()
                val monthRevenue = monthlyReport?.totalRevenue ?: todayRevenue

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
                
                val todayPaymentsResult = paymentRepository.getPaymentsByBranch(branchId, startDate = startOfDay, endDate = endOfDay, limit = 100)
                val todayTransactions = todayPaymentsResult.getOrDefault(emptyList()).size

                _uiState.update { state ->
                    state.copy(
                        todayRevenue = todayRevenue,
                        todayExpense = todayExpense,
                        todayNetProfit = todayNetProfit,
                        monthRevenue = monthRevenue,
                        todayTransactions = todayTransactions
                    )
                }

                // Load chart data for current filter
                loadChartData(branchId, _uiState.value.chartFilter, _uiState.value.selectedCustomDate)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setChartFilter(filter: String, customDate: Date? = null) {
        val branchId = _uiState.value.branchId
        _uiState.update { it.copy(chartFilter = filter, selectedCustomDate = customDate, isLoading = true) }
        loadChartData(branchId, filter, customDate)
    }

    private fun loadChartData(branchId: String, filter: String, customDate: Date?) {
        viewModelScope.launch {
            try {
                val (startDate, endDate) = when (filter) {
                    "HARI_INI" -> {
                        val s = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                        val e = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.time
                        Pair(s, e)
                    }
                    "7_HARI" -> {
                        val s = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -6)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                        val e = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.time
                        Pair(s, e)
                    }
                    "BULAN_INI" -> {
                        val s = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                        val e = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.time
                        Pair(s, e)
                    }
                    "PILIH_TANGGAL" -> {
                        val target = customDate ?: Date()
                        val s = Calendar.getInstance().apply {
                            time = target
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                        val e = Calendar.getInstance().apply {
                            time = target
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.time
                        Pair(s, e)
                    }
                    else -> Pair(null, null)
                }

                val paymentsResult = paymentRepository.getPaymentsByBranch(
                    branchId = branchId,
                    startDate = startDate,
                    endDate = endDate,
                    limit = 300
                )
                val payments = paymentsResult.getOrDefault(emptyList())

                val points = when (filter) {
                    "HARI_INI", "PILIH_TANGGAL" -> {
                        val intervals = listOf(
                            Triple("06-09", 6, 9),
                            Triple("09-12", 9, 12),
                            Triple("12-15", 12, 15),
                            Triple("15-18", 15, 18),
                            Triple("18-21", 18, 21),
                            Triple("21-24", 21, 24)
                        )
                        intervals.map { (label, startHour, endHour) ->
                            val slotPayments = payments.filter { p ->
                                val pDate = p.paidAt ?: p.createdAt
                                if (pDate != null) {
                                    val cal = Calendar.getInstance().apply { time = pDate }
                                    val h = cal.get(Calendar.HOUR_OF_DAY)
                                    h in startHour until endHour
                                } else false
                            }
                            val inc = slotPayments.filter { it.transactionType != "EXPENSE" }.sumOf { it.amount }
                            val exp = slotPayments.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }
                            FinancialChartPoint(label = label, income = inc, expense = exp)
                        }
                    }
                    "7_HARI" -> {
                        val dayFormat = SimpleDateFormat("EEE d", Locale("id", "ID"))
                        (6 downTo 0).map { daysAgo ->
                            val targetCal = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -daysAgo)
                            }
                            val dayStart = Calendar.getInstance().apply {
                                time = targetCal.time
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time
                            val dayEnd = Calendar.getInstance().apply {
                                time = targetCal.time
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }.time

                            val dayPayments = payments.filter { p ->
                                val pDate = p.paidAt ?: p.createdAt
                                pDate != null && pDate.time >= dayStart.time && pDate.time <= dayEnd.time
                            }
                            val inc = dayPayments.filter { it.transactionType != "EXPENSE" }.sumOf { it.amount }
                            val exp = dayPayments.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }
                            FinancialChartPoint(label = dayFormat.format(targetCal.time), income = inc, expense = exp)
                        }
                    }
                    "BULAN_INI" -> {
                        val buckets = listOf(
                            Triple("Tgl 1-7", 1, 7),
                            Triple("Tgl 8-14", 8, 14),
                            Triple("Tgl 15-21", 15, 21),
                            Triple("Tgl 22-28", 22, 28),
                            Triple("Tgl 29+", 29, 31)
                        )
                        buckets.map { (label, startD, endD) ->
                            val bPayments = payments.filter { p ->
                                val pDate = p.paidAt ?: p.createdAt
                                if (pDate != null) {
                                    val cal = Calendar.getInstance().apply { time = pDate }
                                    val d = cal.get(Calendar.DAY_OF_MONTH)
                                    d in startD..endD
                                } else false
                            }
                            val inc = bPayments.filter { it.transactionType != "EXPENSE" }.sumOf { it.amount }
                            val exp = bPayments.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }
                            FinancialChartPoint(label = label, income = inc, expense = exp)
                        }
                    }
                    else -> emptyList()
                }

                val totIncome = payments.filter { it.transactionType != "EXPENSE" }.sumOf { it.amount }
                val totExpense = payments.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        chartPoints = points,
                        filteredTotalIncome = totIncome,
                        filteredTotalExpense = totExpense,
                        filteredNetProfit = totIncome - totExpense,
                        recentTransactions = payments.take(20),
                        filteredPayments = payments
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun recordTransaction(
        type: String, // "INCOME" or "EXPENSE"
        category: String,
        amount: Long,
        paymentMethod: String,
        notes: String = "",
        title: String = "",
        memberId: String = "",
        memberName: String = "",
        proofUrl: String? = null,
        transactionDate: Date = Date(),
        branchId: String = "",
        onSuccess: (() -> Unit)? = null,
        onError: ((String?) -> Unit)? = null
    ) {
        val targetBranchId = branchId.ifBlank { _uiState.value.branchId }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val pType = if (type == "EXPENSE") "PENGELUARAN" else "PEMASUKAN"
            val effectivePlanName = if (title.isNotBlank()) title else category

            paymentRepository.createPayment(
                memberId = memberId,
                memberName = memberName,
                membershipId = null,
                branchId = targetBranchId,
                amount = amount,
                paymentMethod = paymentMethod,
                paymentType = pType,
                planName = effectivePlanName,
                proofUrl = proofUrl,
                transactionType = type,
                category = category,
                notes = notes,
                transactionDate = transactionDate
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false, successMessage = "Transaksi berhasil dicatat") }
                loadSummary(targetBranchId)
                onSuccess?.invoke()
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                onError?.invoke(e.message)
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
        planName: String,
        proofUrl: String? = null
    ) {
        recordTransaction(
            type = "INCOME",
            category = "Pembayaran Member",
            amount = amount,
            paymentMethod = paymentMethod,
            notes = "",
            title = planName,
            memberId = memberId,
            memberName = memberName,
            proofUrl = proofUrl
        )
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

// ============================================================================
// INVOICE LIST VIEWMODEL (FIRESTORE)
// ============================================================================
data class InvoiceListUiState(
    val isLoading: Boolean = false,
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
                val paymentsResult = paymentRepository.getPaymentsByBranch(branchId, limit = 150)
                
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
// INVOICE DETAIL VIEWMODEL (FIRESTORE)
// ============================================================================
data class InvoiceDetailUiState(
    val isLoading: Boolean = false,
    val invoice: FirestorePayment? = null,
    val memberCode: String = "",
    val error: String? = null
)

class InvoiceDetailViewModel : ViewModel() {
    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()
    private val memberRepository: MemberRepository = MemberRepositoryImpl()

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()

    fun loadDetail(invoiceId: String) {
        if (invoiceId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            paymentRepository.getPaymentById(invoiceId)
                .onSuccess { payment ->
                    val resolvedCode = if (payment.memberCode.isNotBlank()) {
                        payment.memberCode
                    } else if (payment.memberId.isNotBlank()) {
                        try {
                            val member = memberRepository.getMemberById(payment.memberId).getOrNull()
                            member?.memberCode?.ifBlank { null } ?: ""
                        } catch (_: Exception) { "" }
                    } else {
                        ""
                    }
                    _uiState.update { it.copy(isLoading = false, invoice = payment, memberCode = resolvedCode) }
                }
                .onFailure { e ->
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
