package com.loki.deni.ui.model

data class ActiveLoanSummary(
    val title: String,
    val amount: Int,
    val remaining: Int,
    val monthlyEmi: Int,
    val disbursedDate: String,
    val dueDate: String,
    val daysUntilDue: Int,
    val percentRepaid: Float,
)

data class CreditSummary(
    val score: Int,
    val scoreLabel: String,
    val approvedLimit: Int,
    val availableLimit: Int,
    val usedPercent: Float,
)

data class TipCard(
    val icon: String,
    val tag: String,
    val tagColor: TipTagColor,
    val title: String,
    val body: String,
)

enum class TipTagColor {
    GREEN,
    GOLD,
    BLUE,
}

data class RecentTransaction(
    val id: Int,
    val title: String,
    val subtitle: String,
    val amount: Int,
    val isCredit: Boolean,
    val status: LoanStatus,
)

data class HomeUiState(
    val greeting: String,
    val userName: String,
    val creditSummary: CreditSummary,
    val activeLoan: ActiveLoanSummary?,
    val tips: List<TipCard>,
    val recentTransactions: List<RecentTransaction>,
)

sealed interface HomeScreenState {
    data object Loading : HomeScreenState
    data class Success(val data: HomeUiState) : HomeScreenState
    data class Error(val msg: String) : HomeScreenState
}
