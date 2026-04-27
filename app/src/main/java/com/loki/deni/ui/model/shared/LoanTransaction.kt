package com.loki.deni.ui.model

enum class LoanStatus {
    PAID,
    ACTIVE,
    OVERDUE,
}

enum class ChartPeriod(val label: String) {
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
}

data class LoanTransaction(
    val id: Int,
    val title: String,
    val amount: Int,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalRepayment: Double,
    val repaidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val disbursedDate: String,
    val dueDate: String,
    val tenureMonths: Int,
    val status: LoanStatus,
    val monthGroup: String,
)

data class MonthlyChartPoint(
    val monthLabel: String,
    val borrowed: Float,
    val repaid: Float,
)

data class HistorySummary(
    val totalBorrowed: Int,
    val totalRepaid: Int,
    val repayRate: Int,
    val totalLoans: Int,
    val paidCount: Int,
    val activeCount: Int,
    val overdueCount: Int,
    val onTimeCount: Int,
    val lateCount: Int,
)
