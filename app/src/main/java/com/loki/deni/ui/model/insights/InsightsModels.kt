package com.loki.deni.ui.model

enum class InsightPeriod(val label: String) {
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
}

data class CreditScorePoint(
    val month: String,
    val score: Int,
)

data class BorrowingTrendPoint(
    val month: String,
    val borrowed: Float,
    val repaid: Float,
)

data class MetricCardData(
    val icon: String,
    val title: String,
    val value: String,
    val delta: String,
    val isPositive: Boolean? = null,
)

data class PurposeBreakdownItem(
    val label: String,
    val count: Int,
    val percent: Int,
    val colorKey: String,
)

data class StatusBreakdownItem(
    val label: String,
    val count: Int,
    val colorKey: String,
)

data class HeatmapDay(
    val level: Int,
)

data class PaymentTimelineItem(
    val installmentNumber: Int,
    val title: String,
    val dueDate: String,
    val amountLabel: String,
    val status: String,
    val daysLabel: String? = null,
)

data class CostComparisonRow(
    val tenureMonths: Int,
    val emiLabel: String,
    val interestLabel: String,
    val totalLabel: String,
    val highlighted: Boolean = false,
)

data class InsightsUiData(
    val currentScore: Int,
    val scoreBand: String,
    val scoreDeltaLabel: String,
    val percentileLabel: String,
    val scorePoints: List<CreditScorePoint>,
    val trendPoints: List<BorrowingTrendPoint>,
    val metricCards: List<MetricCardData>,
    val purposeBreakdown: List<PurposeBreakdownItem>,
    val statusBreakdown: List<StatusBreakdownItem>,
    val heatmap: List<HeatmapDay>,
    val costComparison: List<CostComparisonRow>,
    val paymentTimeline: List<PaymentTimelineItem>,
    val onTimeRateLabel: String,
    val loansCompletedLabel: String,
    val tierLabel: String,
    val savingsAmountLabel: String,
    val savingsPayoffLabel: String,
)

sealed class InsightsUiState {
    data object Loading : InsightsUiState()
    data class Success(val data: InsightsUiData) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
}
