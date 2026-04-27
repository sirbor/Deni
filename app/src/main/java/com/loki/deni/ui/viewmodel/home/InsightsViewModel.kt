package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.ui.model.CostComparisonRow
import com.loki.deni.ui.model.InsightPeriod
import com.loki.deni.ui.model.InsightsUiData
import com.loki.deni.ui.model.InsightsUiState
import com.loki.deni.util.AuthEvents
import com.loki.deni.util.core.CreditPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(InsightPeriod.SIX_MONTHS)
    val selectedPeriod: StateFlow<InsightPeriod> = _selectedPeriod.asStateFlow()

    private val _scoreChartPeriod = MutableStateFlow(InsightPeriod.SIX_MONTHS)
    val scoreChartPeriod: StateFlow<InsightPeriod> = _scoreChartPeriod.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _uiState.value = InsightsUiState.Loading
            delay(500)
            runCatching {
                val userId = preferences.currentUserId.first()
                    ?: throw IllegalStateException("No active session")
                if (userId.isBlank()) {
                    AuthEvents.emitSessionExpired()
                    throw IllegalStateException("No active session")
                }
                val profile = repository.getUserProfile(userId).first()
                    ?: throw IllegalStateException("No profile")
                val loans = repository.getLoans(userId).first()
                val tx = repository.getTransactions(userId).first()
                val effectiveScore = (profile.creditScore.takeIf { it >= 300 }
                    ?: CreditPolicy.starterScoreForSalaryRange(profile.salaryRange))
                    .coerceIn(300, 850)
                val scoreHistory = tx.take(6).map {
                    com.loki.deni.ui.model.CreditScorePoint(
                        month = SimpleDateFormat("MMM", Locale.ENGLISH).format(Date(it.timestamp)),
                        score = effectiveScore,
                    )
                }.ifEmpty {
                    listOf(com.loki.deni.ui.model.CreditScorePoint("Now", effectiveScore))
                }
                val trend = loans.take(6).map {
                    com.loki.deni.ui.model.BorrowingTrendPoint(
                        month = SimpleDateFormat("MMM", Locale.ENGLISH).format(Date(it.disbursedDate)),
                        borrowed = it.amount.toFloat(),
                        repaid = it.repaidAmount.toFloat(),
                    )
                }
                this@InsightsViewModel.scoreHistory = scoreHistory
                this@InsightsViewModel.borrowingTrend = trend
                InsightsUiData(
                    currentScore = effectiveScore,
                    scoreBand = CreditPolicy.scoreBand(effectiveScore),
                    scoreDeltaLabel = "Updated from repayments",
                    percentileLabel = "Based on your repayment history",
                    scorePoints = scorePointsByPeriod(_scoreChartPeriod.value),
                    trendPoints = trendPointsByPeriod(_selectedPeriod.value),
                    metricCards = emptyList(),
                    purposeBreakdown = emptyList(),
                    statusBreakdown = emptyList(),
                    heatmap = emptyList(),
                    costComparison = buildCostComparison(),
                    paymentTimeline = emptyList(),
                    onTimeRateLabel = "${if (loans.isEmpty()) 0 else (loans.count { it.isPaid } * 100 / loans.size)}% On time",
                    loansCompletedLabel = "${loans.count { it.isPaid }} Loans Done",
                    tierLabel = CreditPolicy.scoreBand(effectiveScore),
                    savingsAmountLabel = "KES ${tx.filter { it.type.equals("debit", true) }.sumOf { it.amount }.toInt()}",
                    savingsPayoffLabel = "KES ${loans.sumOf { it.repaidAmount }.toInt()}",
                )
            }.onSuccess {
                _uiState.value = InsightsUiState.Success(it)
            }.onFailure {
                _uiState.value = InsightsUiState.Error(it.message ?: "Unable to load insights")
            }
        }
    }

    fun onPeriodChange(period: InsightPeriod) {
        _selectedPeriod.value = period
        _uiState.update { current ->
            if (current is InsightsUiState.Success) {
                current.copy(data = current.data.copy(trendPoints = trendPointsByPeriod(period)))
            } else {
                current
            }
        }
    }

    fun onScorePeriodChange(period: InsightPeriod) {
        _scoreChartPeriod.value = period
        _uiState.update { current ->
            if (current is InsightsUiState.Success) {
                current.copy(data = current.data.copy(scorePoints = scorePointsByPeriod(period)))
            } else {
                current
            }
        }
    }

    fun getScoreProgress(score: Int): Float = (score / 850f).coerceIn(0f, 1f)

    fun buildCostComparison(amount: Int = 15000): List<CostComparisonRow> {
        val tenures = listOf(1, 3, 6)
        return tenures.map { tenure ->
            val emi = LoanCalculator.calculateEMI(amount.toDouble(), LoanCalculator.annualInterestRate(), tenure)
            val interest = LoanCalculator.totalInterest(amount.toDouble(), LoanCalculator.annualInterestRate(), tenure)
            val total = LoanCalculator.totalRepayment(amount.toDouble(), LoanCalculator.annualInterestRate(), tenure)
            CostComparisonRow(
                tenureMonths = tenure,
                emiLabel = formatCurrency(emi),
                interestLabel = formatCurrency(interest),
                totalLabel = formatCurrency(total),
                highlighted = tenure == 3,
            )
        }
    }

    fun formatCurrency(amount: Int): String = "KES %,d".format(amount)

    fun formatCurrency(amount: Double): String = "KES %,d".format(amount.toInt())

    private var scoreHistory: List<com.loki.deni.ui.model.CreditScorePoint> = emptyList()
    private var borrowingTrend: List<com.loki.deni.ui.model.BorrowingTrendPoint> = emptyList()

    private fun scorePointsByPeriod(period: InsightPeriod) = when (period) {
        InsightPeriod.THREE_MONTHS -> scoreHistory.takeLast(3)
        InsightPeriod.SIX_MONTHS -> scoreHistory
        InsightPeriod.ONE_YEAR -> scoreHistory + scoreHistory
            .mapIndexed { idx, item -> item.copy(month = "P${idx + 1}") }
        InsightPeriod.ONE_MONTH -> scoreHistory.takeLast(1)
    }

    private fun trendPointsByPeriod(period: InsightPeriod) = when (period) {
        InsightPeriod.ONE_MONTH -> borrowingTrend.takeLast(1)
        InsightPeriod.THREE_MONTHS -> borrowingTrend.takeLast(3)
        InsightPeriod.SIX_MONTHS -> borrowingTrend
        InsightPeriod.ONE_YEAR -> borrowingTrend + borrowingTrend
    }
}
