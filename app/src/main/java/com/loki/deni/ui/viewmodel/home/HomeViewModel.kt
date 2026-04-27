package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.UserProfileEntity
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.ui.model.ActiveLoanSummary
import com.loki.deni.ui.model.CreditSummary
import com.loki.deni.ui.model.HomeScreenState
import com.loki.deni.ui.model.HomeUiState
import com.loki.deni.ui.model.LoanStatus
import com.loki.deni.ui.model.RecentTransaction
import com.loki.deni.ui.model.TipCard
import com.loki.deni.ui.model.TipTagColor
import com.loki.deni.ui.model.UiState
import com.loki.deni.util.AuthEvents
import com.loki.deni.util.core.CreditPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private val _screenState = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val screenState: StateFlow<HomeScreenState> = _screenState.asStateFlow()
    private val _activeLoan = MutableStateFlow<UiState<LoanEntity>>(UiState.Loading)
    val activeLoan: StateFlow<UiState<LoanEntity>> = _activeLoan.asStateFlow()
    val unreadNotifications: StateFlow<Int> = MutableStateFlow(0)

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _screenState.value = HomeScreenState.Loading
            runCatching {
                val userId = preferences.currentUserId.first()
                    ?: throw IllegalStateException("SESSION_EXPIRED")
                val user = repository.getUserProfile(userId).first() ?: run {
                    val fallback = UserProfileEntity(
                        id = userId,
                        name = preferences.firstName.value?.trim().orEmpty().ifBlank { "User" },
                        phone = preferences.savedPhone.value.orEmpty(),
                        passwordHash = preferences.savedPin.value.orEmpty(),
                        creditScore = 500,
                        balance = 0.0,
                    )
                    runCatching { repository.upsertUserProfile(fallback) }
                    repository.getUserProfile(userId).first() ?: fallback
                }
                val loans = repository.getLoans(userId).first()
                val tx = repository.getTransactions(userId).first()
                val greeting = greetingByTime()
                val active = loans.firstOrNull { !it.isPaid }
                val activeLoan = active?.let { loan ->
                    val totalRepayment = loan.amount + (loan.amount * loan.interestRate)
                    val remaining = (totalRepayment - loan.repaidAmount).toInt().coerceAtLeast(0)
                    val monthlyEmi = if (loan.tenureMonths > 0) (totalRepayment / loan.tenureMonths).toInt() else totalRepayment.toInt()
                    ActiveLoanSummary(
                        title = loan.type,
                        amount = loan.amount.toInt(),
                        remaining = remaining,
                        monthlyEmi = monthlyEmi,
                        disbursedDate = formatDate(loan.disbursedDate),
                        dueDate = formatDate(loan.dueDate),
                        daysUntilDue = daysUntil(loan.dueDate),
                        percentRepaid = (loan.repaidAmount / totalRepayment).toFloat().coerceIn(0f, 1f),
                    )
                }

                val repayRate = repayRate(loans)
                val (score, label, limit) = when {
                    repayRate >= 95 -> Triple(732, "Good", 50000)
                    repayRate >= 80 -> Triple(650, "Fair", 30000)
                    else -> Triple(560, "Poor", 15000)
                }
                val available = activeLoan?.let { (limit - it.remaining).coerceIn(0, limit) } ?: (limit * 0.7f).toInt()
                val usedPercent = ((limit - available).toFloat() / limit.toFloat()).coerceIn(0f, 1f)
                val creditSummary = CreditSummary(
                    score = score,
                    scoreLabel = label,
                    approvedLimit = limit,
                    availableLimit = available,
                    usedPercent = usedPercent,
                )

                val tips = listOf(
                    TipCard(
                        icon = "TIP",
                        tag = "Credit Tip",
                        tagColor = TipTagColor.GREEN,
                        title = "Pay early to boost your score",
                        body = "Early repayments can increase your limit by up to 20%",
                    ),
                    TipCard(
                        icon = "OFFER",
                        tag = "Offer",
                        tagColor = TipTagColor.GOLD,
                        title = "You qualify for a higher limit",
                        body = "Based on your history, you may get KES 75,000 next month",
                    ),
                    TipCard(
                        icon = "REM",
                        tag = "Reminder",
                        tagColor = TipTagColor.BLUE,
                        title = "Next payment due in ${activeLoan?.daysUntilDue ?: 0} days",
                        body = "KES ${(activeLoan?.monthlyEmi ?: 0).formatKes()} due on ${activeLoan?.dueDate ?: "-"} - set a reminder",
                    ),
                )

                val recent = tx
                    .take(3)
                    .map { t ->
                        RecentTransaction(
                            id = t.transId,
                            title = t.title,
                            subtitle = formatDate(t.timestamp),
                            amount = t.amount.toInt(),
                            isCredit = t.type.equals("credit", true),
                            status = if (t.status.equals("paid", true)) LoanStatus.PAID else LoanStatus.ACTIVE,
                        )
                    }

                HomeUiState(
                    greeting = greeting,
                    userName = user.name,
                    creditSummary = creditSummary,
                    activeLoan = activeLoan,
                    tips = tips,
                    recentTransactions = recent,
                )
            }.onSuccess { ui ->
                _screenState.value = HomeScreenState.Success(ui)
                _activeLoan.value = ui.activeLoan?.let {
                    UiState.Success(
                        LoanEntity(
                            userId = preferences.currentUserId.first() ?: "",
                            type = it.title,
                            amount = it.amount.toDouble(),
                            interestRate = 0.15,
                            dueDate = parseDate(it.dueDate),
                            isPaid = false,
                            tenureMonths = if (it.monthlyEmi > 0) 3 else 1,
                            disbursedDate = parseDate(it.disbursedDate),
                            repaidAmount = (it.amount - it.remaining).toDouble(),
                            referenceNumber = "DENI12345678",
                        ),
                    )
                } ?: UiState.Error("No active loan")
            }.onFailure {
                if (it.message == "SESSION_EXPIRED") {
                    AuthEvents.emitSessionExpired()
                }
                _screenState.value = HomeScreenState.Error(it.message ?: "Unable to load home data")
                _activeLoan.value = UiState.Error(it.message ?: "Unable to load home data")
            }
        }
    }

    fun onApplyClick() = Unit

    fun onRepayClick() = Unit

    fun onHistoryClick() = Unit

    fun onSupportClick() = Unit

    suspend fun applyLoan(
        amount: Double,
        tenureDays: Int,
        effectiveRate: Double,
        totalRepayment: Double,
        reference: String,
        purpose: String = "Personal",
    ): Result<Unit> = runCatching {
        val userId = preferences.currentUserId.first()
        if (userId.isNullOrBlank()) {
            AuthEvents.emitSessionExpired()
            error("Session expired. Please sign in again.")
        }
        val user = repository.getUserProfile(userId).first() ?: run {
            val fallbackName = preferences.firstName.value?.trim().orEmpty().ifBlank { "User" }
            val fallbackPhone = preferences.savedPhone.value.orEmpty()
            val fallbackProfile = UserProfileEntity(
                id = userId,
                name = fallbackName,
                phone = fallbackPhone,
                passwordHash = preferences.savedPin.value.orEmpty(),
                creditScore = 500,
                balance = 0.0,
            )
            repository.upsertUserProfile(fallbackProfile)
            repository.getUserProfile(userId).first()
                ?: error("Profile setup is incomplete. Please try again.")
        }
        val loans = repository.getLoans(userId).first()
        val paidLoans = loans.count { it.isPaid }
        val approvedLimit = CreditPolicy.resolveApprovedLimit(
            salaryRange = user.salaryRange,
            creditScore = user.creditScore,
            paidLoansCount = paidLoans,
        )
        val allowedDays = LoanCalculator.allowedTenureDaysFor(
            userLimit = approvedLimit,
            principal = amount.toInt(),
        )
        if (tenureDays !in allowedDays) {
            error("Selected tenure is not allowed for your current limit.")
        }
        val now = System.currentTimeMillis()
        repository.applyForLoan(
            LoanEntity(
                userId = userId,
                type = purpose.ifBlank { "Personal" },
                amount = amount,
                interestRate = effectiveRate,
                dueDate = now + tenureDays * 24L * 60L * 60L * 1000L,
                isPaid = false,
                tenureMonths = 1,
                disbursedDate = now,
                repaidAmount = 0.0,
                referenceNumber = reference,
            ),
        )
        loadHomeData()
    }

    fun repay(amount: Double, phone: String) {
        val loan = (_activeLoan.value as? UiState.Success)?.data ?: return
        val updated = loan.copy(repaidAmount = (loan.repaidAmount + amount).coerceAtMost(loan.amount))
        _activeLoan.value = UiState.Success(updated)
    }

    private fun greetingByTime(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    private fun repayRate(loans: List<LoanEntity>): Int {
        val all = loans
        if (all.isEmpty()) return 0
        val paid = all.count { it.isPaid }
        return ((paid.toFloat() / all.size.toFloat()) * 100f).toInt()
    }

    private fun daysUntil(due: Long): Int {
        val diff = due - System.currentTimeMillis()
        return (diff / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
    }

    private fun Int.formatKes(): String = String.format("%,d", this)

    private fun formatDate(date: Long): String {
        val parser = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
        return parser.format(Date(date))
    }

    private fun parseDate(date: String): Long {
        val parser = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)
        return parser.parse(date)?.time ?: System.currentTimeMillis()
    }
}
