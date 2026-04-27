package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.ui.model.ChartPeriod
import com.loki.deni.ui.model.HistorySummary
import com.loki.deni.ui.model.LoanStatus
import com.loki.deni.ui.model.LoanTransaction
import com.loki.deni.ui.model.MonthlyChartPoint
import com.loki.deni.util.AuthEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Error(val message: String) : HistoryUiState
    data class Success(
        val filteredTransactions: List<LoanTransaction>,
        val groupedTransactions: Map<String, List<LoanTransaction>>,
        val summary: HistorySummary,
        val chartData: List<MonthlyChartPoint>,
    ) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private var allTransactions: List<LoanTransaction> = emptyList()

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow<LoanStatus?>(null)
    val activeFilter: StateFlow<LoanStatus?> = _activeFilter.asStateFlow()

    private val _expandedTxId = MutableStateFlow<Int?>(null)
    val expandedTxId: StateFlow<Int?> = _expandedTxId.asStateFlow()

    private val _activePeriod = MutableStateFlow(ChartPeriod.SIX_MONTHS)
    val activePeriod: StateFlow<ChartPeriod> = _activePeriod.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            delay(600)
            val userId = preferences.currentUserId.first()
            if (userId.isNullOrBlank()) {
                _uiState.value = HistoryUiState.Empty
                AuthEvents.emitSessionExpired()
                return@launch
            }
            val mapped = combine(
                repository.getLoans(userId),
                repository.getTransactions(userId),
            ) { loans, tx -> mapLoanTransactions(loans, tx) }.first()
            allTransactions = mapped
            publishState()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        publishState()
    }

    fun onFilterChange(status: LoanStatus?) {
        _activeFilter.value = status
        publishState()
    }

    fun toggleExpandTx(id: Int) {
        _expandedTxId.value = if (_expandedTxId.value == id) null else id
    }

    fun onPeriodChange(period: ChartPeriod) {
        _activePeriod.value = period
        publishState()
    }

    fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "KE"))
        return "KES ${formatter.format(amount)}"
    }

    private fun publishState() {
        runCatching {
            val filtered = applyFilters(allTransactions)
            if (filtered.isEmpty()) {
                _uiState.value = HistoryUiState.Empty
                return
            }
            val grouped = filtered.groupBy { it.monthGroup }
            val summary = computeSummary(allTransactions)
            val chartData = buildChartData(filtered)
            _uiState.value = HistoryUiState.Success(
                filteredTransactions = filtered,
                groupedTransactions = grouped,
                summary = summary,
                chartData = chartData,
            )
        }.onFailure {
            _uiState.value = HistoryUiState.Error(it.message ?: "Unable to load history")
        }
    }

    private fun applyFilters(source: List<LoanTransaction>): List<LoanTransaction> {
        val query = _searchQuery.value.trim()
        val filter = _activeFilter.value
        return source.filter { tx ->
            val matchesFilter = filter == null || tx.status == filter
            val matchesQuery = query.isBlank() ||
                tx.title.contains(query, true) ||
                tx.disbursedDate.contains(query, true) ||
                tx.dueDate.contains(query, true) ||
                tx.amount.toString().contains(query, true)
            matchesFilter && matchesQuery
        }
    }

    private fun computeSummary(transactions: List<LoanTransaction>): HistorySummary {
        val totalBorrowed = transactions.sumOf { it.amount }
        val paid = transactions.filter { it.status == LoanStatus.PAID }
        val active = transactions.filter { it.status == LoanStatus.ACTIVE }
        val overdue = transactions.filter { it.status == LoanStatus.OVERDUE }
        val totalRepaid = paid.sumOf { it.amount } + active.sumOf { (it.monthlyEmi * 1).toInt() }
        val onTimeCount = paid.size
        val lateCount = overdue.size
        val repayRate = if (transactions.isEmpty()) 0 else ((onTimeCount.toFloat() / transactions.size) * 100).toInt()
        return HistorySummary(
            totalBorrowed = totalBorrowed,
            totalRepaid = totalRepaid,
            repayRate = repayRate,
            totalLoans = transactions.size,
            paidCount = paid.size,
            activeCount = active.size,
            overdueCount = overdue.size,
            onTimeCount = onTimeCount,
            lateCount = lateCount,
        )
    }

    private fun mapLoanTransactions(
        loans: List<LoanEntity>,
        transactions: List<TransactionEntity>,
    ): List<LoanTransaction> {
        val dateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
        return loans.map { loan ->
            val loanTx = transactions.filter { it.loanId == loan.loanId }
            val totalPaid = loanTx.filter { it.type.equals("Debit", true) }.sumOf { it.amount }
            val monthlyEmi = if (loan.tenureMonths > 0) {
                (loan.amount + (loan.amount * loan.interestRate)) / loan.tenureMonths
            } else {
                loan.amount
            }
            val status = when {
                loan.isPaid -> LoanStatus.PAID
                loan.dueDate < System.currentTimeMillis() -> LoanStatus.OVERDUE
                else -> LoanStatus.ACTIVE
            }
            LoanTransaction(
                id = loan.loanId,
                title = loan.type,
                amount = loan.amount.toInt(),
                monthlyEmi = monthlyEmi,
                totalInterest = loan.amount * loan.interestRate,
                totalRepayment = loan.amount + (loan.amount * loan.interestRate),
                disbursedDate = dateFormat.format(Date(loan.disbursedDate)),
                dueDate = dateFormat.format(Date(loan.dueDate)),
                tenureMonths = loan.tenureMonths,
                status = status,
                monthGroup = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date(loan.disbursedDate)),
            )
        }.sortedByDescending { it.id }
    }

    private fun buildChartData(transactions: List<LoanTransaction>): List<MonthlyChartPoint> {
        return transactions
            .groupBy { it.monthGroup }
            .map { (month, items) ->
                MonthlyChartPoint(
                    monthLabel = month.take(3),
                    borrowed = items.sumOf { it.amount }.toFloat(),
                    repaid = items.filter { it.status == LoanStatus.PAID }.sumOf { it.amount }.toFloat(),
                )
            }
            .takeLast(
                when (_activePeriod.value) {
                    ChartPeriod.THREE_MONTHS -> 3
                    ChartPeriod.SIX_MONTHS -> 6
                    ChartPeriod.ONE_YEAR -> 12
                },
            )
    }
}
