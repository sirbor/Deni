package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.data.local.entity.UserProfileEntity
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.util.AuthEvents
import com.loki.deni.util.core.CreditPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardStats(
    val creditScore: Int = 500,
    val scoreBand: String = "Fair",
    val approvedLimit: Int = 0,
    val availableLimit: Int = 0,
    val outstanding: Int = 0,
    val totalBorrowed: Int = 0,
    val totalRepaid: Int = 0,
)

@HiltViewModel
class AccountDataViewModel @Inject constructor(
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private var started = false
    private val _user = MutableStateFlow<UserProfileEntity?>(null)
    val user: StateFlow<UserProfileEntity?> = _user.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()
    private val _loans = MutableStateFlow<List<LoanEntity>>(emptyList())
    val loans: StateFlow<List<LoanEntity>> = _loans.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    fun load() {
        if (started) return
        started = true
        viewModelScope.launch {
            preferences.currentUserId.collectLatest { userId ->
                if (userId.isNullOrBlank()) {
                    AuthEvents.emitSessionExpired()
                    _user.value = null
                    _transactions.value = emptyList()
                    _loans.value = emptyList()
                    _stats.value = DashboardStats()
                    return@collectLatest
                }
                repository.reconcileLoanStatuses(userId)
                combine(
                    repository.getUserProfile(userId),
                    repository.getTransactions(userId),
                    repository.getLoans(userId),
                ) { user, transactions, loans ->
                    Triple(user, transactions, loans)
                }.collect { (user, transactions, loans) ->
                    _user.value = user
                    _transactions.value = transactions
                    _loans.value = loans

                    val paidLoans = loans.count { it.isPaid }
                    val effectiveScore = (user?.creditScore ?: CreditPolicy.starterScoreForSalaryRange(user?.salaryRange))
                        .coerceIn(300, 850)
                    val approvedLimit = CreditPolicy.resolveApprovedLimit(
                        salaryRange = user?.salaryRange,
                        creditScore = effectiveScore,
                        paidLoansCount = paidLoans,
                    )
                    val outstanding = loans
                        .filter { !it.isPaid }
                        .sumOf { loan ->
                            val totalRepayment = loan.amount + (loan.amount * loan.interestRate)
                            (totalRepayment - loan.repaidAmount).coerceAtLeast(0.0)
                        }
                        .toInt()
                    val availableLimit = (approvedLimit - outstanding).coerceIn(0, approvedLimit)
                    val totalBorrowed = loans.sumOf { it.amount }.toInt()
                    val totalRepaid = transactions
                        .filter { it.type.equals("debit", ignoreCase = true) }
                        .sumOf { it.amount }
                        .toInt()

                    _stats.value = DashboardStats(
                        creditScore = effectiveScore,
                        scoreBand = CreditPolicy.scoreBand(effectiveScore),
                        approvedLimit = approvedLimit,
                        availableLimit = availableLimit,
                        outstanding = outstanding,
                        totalBorrowed = totalBorrowed,
                        totalRepaid = totalRepaid,
                    )
                }
            }
        }
    }

}
