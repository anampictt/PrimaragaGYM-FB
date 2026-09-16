package com.pws.primaragagym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pws.primaragagym.data.repository.PaymentRepositoryImpl
import com.pws.primaragagym.domain.model.FirestorePayment
import com.pws.primaragagym.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

// ============================================================================
// FILTER PERIOD ENUM
// ============================================================================
enum class LaporanFilterPeriod(val label: String) {
    TODAY("Hari Ini"),
    WEEK("Minggu Ini"),
    MONTH("Bulan Ini"),
    ALL("Semua"),
    CUSTOM("Custom")
}

// ============================================================================
// METHOD BREAKDOWN MODEL
// ============================================================================
data class MethodBreakdownItem(
    val key: String,
    val label: String,
    val amount: Long,
    val transactionCount: Int,
    val percentage: Float
)

// ============================================================================
// UI STATE
// ============================================================================
data class LaporanPemasukanUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val branchId: String = "",
    val allPayments: List<FirestorePayment> = emptyList(),

    // Period filter
    val selectedPeriod: LaporanFilterPeriod = LaporanFilterPeriod.MONTH,
    val customStartDate: Date? = null,
    val customEndDate: Date? = null,

    // Secondary filters
    val selectedTypeFilter: String = "ALL", // ALL, REGISTRATION, RENEWAL
    val searchQuery: String = "",

    // Computed metrics for selected period
    val totalIncome: Long = 0L,
    val registrationIncome: Long = 0L,
    val registrationCount: Int = 0,
    val renewalIncome: Long = 0L,
    val renewalCount: Int = 0,
    val otherIncome: Long = 0L,
    val otherCount: Int = 0,
    val totalTransactions: Int = 0,
    val methodSummaries: List<MethodBreakdownItem> = emptyList(),

    // Transactions list to display
    val displayedTransactions: List<FirestorePayment> = emptyList()
)

// ============================================================================
// VIEW MODEL
// ============================================================================
class LaporanPemasukanViewModel(
    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaporanPemasukanUiState())
    val uiState: StateFlow<LaporanPemasukanUiState> = _uiState.asStateFlow()

    fun loadPayments(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, branchId = branchId, error = null) }
            try {
                // Fetch up to 500 payments
                val result = paymentRepository.getPaymentsByBranch(
                    branchId = branchId,
                    startDate = null,
                    endDate = null,
                    limit = 500
                )
                val payments = result.getOrDefault(emptyList())

                _uiState.update { state ->
                    val updated = state.copy(
                        isLoading = false,
                        allPayments = payments,
                        error = null
                    )
                    calculateMetrics(updated)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat data laporan pemasukan"
                    )
                }
            }
        }
    }

    fun setPeriodFilter(
        period: LaporanFilterPeriod,
        startDate: Date? = null,
        endDate: Date? = null
    ) {
        _uiState.update { state ->
            val updated = state.copy(
                selectedPeriod = period,
                customStartDate = if (period == LaporanFilterPeriod.CUSTOM) (startDate ?: state.customStartDate) else null,
                customEndDate = if (period == LaporanFilterPeriod.CUSTOM) (endDate ?: state.customEndDate) else null
            )
            calculateMetrics(updated)
        }
    }

    fun setTypeFilter(type: String) {
        _uiState.update { state ->
            val updated = state.copy(selectedTypeFilter = type)
            calculateMetrics(updated)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val updated = state.copy(searchQuery = query)
            calculateMetrics(updated)
        }
    }

    fun refresh() {
        loadPayments(_uiState.value.branchId)
    }

    // ========================================================================
    // CALCULATION LOGIC
    // ========================================================================
    private fun calculateMetrics(state: LaporanPemasukanUiState): LaporanPemasukanUiState {
        val (startDate, endDate) = getPeriodDateRange(
            state.selectedPeriod,
            state.customStartDate,
            state.customEndDate
        )

        // 1. Filter by period date range & income only
        val inPeriod = state.allPayments.filter { p ->
            val pDate = p.paidAt ?: p.createdAt
            val inRange = when {
                pDate == null -> false
                startDate != null && endDate != null -> pDate.time >= startDate.time && pDate.time <= endDate.time
                startDate != null -> pDate.time >= startDate.time
                endDate != null -> pDate.time <= endDate.time
                else -> true
            }
            val isIncome = p.transactionType != "EXPENSE" && p.status != "CANCELLED"
            inRange && isIncome
        }

        val totalIncome = inPeriod.sumOf { it.amount }
        val totalTransactions = inPeriod.size

        // 2. Separate Registration (Pendaftaran Member Baru) vs Renewal vs Others
        val registrationPayments = inPeriod.filter { isRegistration(it) }
        val renewalPayments = inPeriod.filter { isRenewal(it) }
        val otherPayments = inPeriod.filter { !isRegistration(it) && !isRenewal(it) }

        val registrationIncome = registrationPayments.sumOf { it.amount }
        val registrationCount = registrationPayments.size

        val renewalIncome = renewalPayments.sumOf { it.amount }
        val renewalCount = renewalPayments.size

        val otherIncome = otherPayments.sumOf { it.amount }
        val otherCount = otherPayments.size

        // 3. Method breakdown (Cash, Transfer, QRIS, Lainnya)
        val methodGroups = inPeriod.groupBy { normalizeMethod(it.paymentMethod) }

        val cashPayments = methodGroups["CASH"] ?: emptyList()
        val transferPayments = methodGroups["TRANSFER"] ?: emptyList()
        val qrisPayments = methodGroups["QRIS"] ?: emptyList()
        val otherMethodPayments = methodGroups["LAINNYA"] ?: emptyList()

        val cashAmount = cashPayments.sumOf { it.amount }
        val transferAmount = transferPayments.sumOf { it.amount }
        val qrisAmount = qrisPayments.sumOf { it.amount }
        val otherMethodAmount = otherMethodPayments.sumOf { it.amount }

        val methodSummaries = mutableListOf<MethodBreakdownItem>()

        methodSummaries.add(
            MethodBreakdownItem(
                key = "CASH",
                label = "Cash / Tunai",
                amount = cashAmount,
                transactionCount = cashPayments.size,
                percentage = if (totalIncome > 0) (cashAmount.toFloat() / totalIncome.toFloat()) * 100f else 0f
            )
        )
        methodSummaries.add(
            MethodBreakdownItem(
                key = "TRANSFER",
                label = "Transfer Bank",
                amount = transferAmount,
                transactionCount = transferPayments.size,
                percentage = if (totalIncome > 0) (transferAmount.toFloat() / totalIncome.toFloat()) * 100f else 0f
            )
        )
        methodSummaries.add(
            MethodBreakdownItem(
                key = "QRIS",
                label = "QRIS",
                amount = qrisAmount,
                transactionCount = qrisPayments.size,
                percentage = if (totalIncome > 0) (qrisAmount.toFloat() / totalIncome.toFloat()) * 100f else 0f
            )
        )
        if (otherMethodAmount > 0) {
            methodSummaries.add(
                MethodBreakdownItem(
                    key = "LAINNYA",
                    label = "Metode Lainnya",
                    amount = otherMethodAmount,
                    transactionCount = otherMethodPayments.size,
                    percentage = if (totalIncome > 0) (otherMethodAmount.toFloat() / totalIncome.toFloat()) * 100f else 0f
                )
            )
        }

        // 4. Secondary filter: category & search
        var displayed = when (state.selectedTypeFilter) {
            "REGISTRATION" -> registrationPayments
            "RENEWAL" -> renewalPayments
            else -> inPeriod
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.trim().lowercase()
            displayed = displayed.filter { p ->
                p.memberName.lowercase().contains(query) ||
                p.memberCode.lowercase().contains(query) ||
                p.invoiceNumber.lowercase().contains(query) ||
                p.planName.lowercase().contains(query) ||
                p.paymentMethod.lowercase().contains(query)
            }
        }

        return state.copy(
            totalIncome = totalIncome,
            registrationIncome = registrationIncome,
            registrationCount = registrationCount,
            renewalIncome = renewalIncome,
            renewalCount = renewalCount,
            otherIncome = otherIncome,
            otherCount = otherCount,
            totalTransactions = totalTransactions,
            methodSummaries = methodSummaries,
            displayedTransactions = displayed.sortedByDescending { it.paidAt ?: it.createdAt }
        )
    }

    private fun getPeriodDateRange(
        period: LaporanFilterPeriod,
        customStart: Date?,
        customEnd: Date?
    ): Pair<Date?, Date?> {
        return when (period) {
            LaporanFilterPeriod.TODAY -> {
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
            LaporanFilterPeriod.WEEK -> {
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
            LaporanFilterPeriod.MONTH -> {
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
            LaporanFilterPeriod.ALL -> {
                Pair(null, null)
            }
            LaporanFilterPeriod.CUSTOM -> {
                val s = customStart?.let {
                    Calendar.getInstance().apply {
                        time = it
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                }
                val e = (customEnd ?: customStart)?.let {
                    Calendar.getInstance().apply {
                        time = it
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.time
                }
                Pair(s, e)
            }
        }
    }

    private fun normalizeMethod(raw: String): String {
        val up = raw.uppercase().trim()
        return when {
            up.contains("CASH") || up.contains("TUNAI") -> "CASH"
            up.contains("TRANSFER") || up.contains("BANK") -> "TRANSFER"
            up.contains("QRIS") -> "QRIS"
            else -> "LAINNYA"
        }
    }

    private fun isRegistration(payment: FirestorePayment): Boolean {
        val type = payment.paymentType.uppercase()
        val notes = payment.notes.lowercase()
        val plan = payment.planName.lowercase()
        return type == "REGISTRASI" ||
               type == "NEW_MEMBERSHIP" ||
               type.contains("PENDAFTARAN") ||
               notes.contains("registrasi") ||
               notes.contains("pendaftaran") ||
               plan.contains("registrasi") ||
               plan.contains("pendaftaran")
    }

    private fun isRenewal(payment: FirestorePayment): Boolean {
        val type = payment.paymentType.uppercase()
        val notes = payment.notes.lowercase()
        val plan = payment.planName.lowercase()
        return type == "RENEWAL" ||
               type.contains("PERPANJANG") ||
               notes.contains("perpanjang") ||
               plan.contains("perpanjang")
    }
}
