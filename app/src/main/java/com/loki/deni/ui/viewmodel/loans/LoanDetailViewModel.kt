package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.ui.model.LoanTransaction
import com.loki.deni.ui.model.LoanStatus
import com.loki.deni.ui.model.ScheduleItem
import com.loki.deni.ui.model.ScheduleStatus
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.util.core.CreditPolicy
import com.loki.deni.util.AuthEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class LoanDetailViewModel @Inject constructor(
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private val supabaseFunctionsBaseUrl = "https://gigxwidiwfteigolfpma.supabase.co/functions/v1"
    private fun normalizePurpose(raw: String): String {
        val normalized = raw.trim().lowercase()
        return when {
            normalized.contains("business") -> "Business"
            normalized.contains("emergency") -> "Emergency"
            else -> "Personal"
        }
    }
    sealed interface TopupUiState {
        data object Idle : TopupUiState
        data object Loading : TopupUiState
        data class Success(val newDueDate: String) : TopupUiState
        data class Error(val message: String) : TopupUiState
    }

    sealed interface RepayUiState {
        data object Idle : RepayUiState
        data object Loading : RepayUiState
        data class Success(val receiptRef: String, val transactionId: Int) : RepayUiState
        data class Error(val message: String) : RepayUiState
    }

    private val _loan = MutableStateFlow<LoanTransaction?>(null)
    val loan: StateFlow<LoanTransaction?> = _loan.asStateFlow()

    private val _schedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val schedule: StateFlow<List<ScheduleItem>> = _schedule.asStateFlow()
    private val _outstandingKes = MutableStateFlow(0)
    val outstandingKes: StateFlow<Int> = _outstandingKes.asStateFlow()
    private val _repayState = MutableStateFlow<RepayUiState>(RepayUiState.Idle)
    val repayState: StateFlow<RepayUiState> = _repayState.asStateFlow()
    private val _topupState = MutableStateFlow<TopupUiState>(TopupUiState.Idle)
    val topupState: StateFlow<TopupUiState> = _topupState.asStateFlow()
    private val _topupHeadroomKes = MutableStateFlow(0)
    val topupHeadroomKes: StateFlow<Int> = _topupHeadroomKes.asStateFlow()

    fun loadLoan(loanId: Int) {
        viewModelScope.launch {
            val userId = preferences.currentUserId.first()
            if (userId.isNullOrBlank()) {
                AuthEvents.emitSessionExpired()
                return@launch
            }
            repository.reconcileLoanStatuses(userId)
            val loans = repository.getLoans(userId).first()
            val profile = repository.getUserProfile(userId).first()
            val loan = if (loanId > 0) {
                loans.firstOrNull { it.loanId == loanId }
            } else {
                null
            } ?: loans
                .filter { !it.isPaid }
                .maxByOrNull { it.timestamp }
                ?: loans.maxByOrNull { it.timestamp }
                ?: return@launch
            val interest = loan.amount * loan.interestRate
            val total = loan.amount + interest
            val monthly = if (loan.tenureMonths > 0) total / loan.tenureMonths else total
            val paid = loan.repaidAmount
            _outstandingKes.value = (total - paid).coerceAtLeast(0.0).toInt()
            _loan.value = LoanTransaction(
                id = loan.loanId,
                title = loan.type,
                amount = loan.amount.toInt(),
                monthlyEmi = monthly,
                totalInterest = interest,
                totalRepayment = total,
                repaidAmount = paid,
                remainingAmount = (total - paid).coerceAtLeast(0.0),
                disbursedDate = formatDate(loan.disbursedDate),
                dueDate = formatDate(loan.dueDate),
                tenureMonths = loan.tenureMonths,
                status = when {
                    loan.isPaid -> LoanStatus.PAID
                    loan.dueDate < System.currentTimeMillis() -> LoanStatus.OVERDUE
                    else -> LoanStatus.ACTIVE
                },
                monthGroup = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date(loan.disbursedDate)),
            )
            _schedule.value = (1..loan.tenureMonths).map { idx ->
                val remaining = (total - (monthly * idx)).coerceAtLeast(0.0)
                ScheduleItem(
                    installmentNumber = idx,
                    dueDate = formatDate(loan.disbursedDate + (idx * 30L * 24L * 60L * 60L * 1000L)),
                    emiAmount = monthly,
                    principal = loan.amount / loan.tenureMonths,
                    interest = interest / loan.tenureMonths,
                    balance = remaining,
                    status = if (paid >= monthly * idx) ScheduleStatus.PAID else ScheduleStatus.UPCOMING,
                )
            }
            val paidLoansCount = loans.count { it.isPaid }
            val approvedLimit = CreditPolicy.resolveApprovedLimit(
                salaryRange = profile?.salaryRange,
                creditScore = profile?.creditScore ?: 500,
                paidLoansCount = paidLoansCount,
            )
            val totalOutstanding = loans
                .filter { !it.isPaid }
                .sumOf { active -> ((active.amount + (active.amount * active.interestRate)) - active.repaidAmount).coerceAtLeast(0.0) }
            _topupHeadroomKes.value = (approvedLimit - totalOutstanding).coerceAtLeast(0.0).toInt()
        }
    }

    private fun formatDate(ts: Long): String =
        SimpleDateFormat("MMM d yyyy", Locale.ENGLISH).format(Date(ts))

    fun repay(loanId: Int, amount: Double, mpesaRef: String?) {
        viewModelScope.launch {
            val userId = preferences.currentUserId.first()
            val sessionToken = preferences.sessionToken.value
            if (userId.isNullOrBlank()) {
                AuthEvents.emitSessionExpired()
                return@launch
            }
            _repayState.value = RepayUiState.Loading
            runCatching {
                val allLoans = repository.getLoans(userId).first()
                val sourceLoan = (if (loanId > 0) allLoans.firstOrNull { it.loanId == loanId } else null)
                    ?: allLoans.filter { !it.isPaid }.maxByOrNull { it.timestamp }
                    ?: allLoans.maxByOrNull { it.timestamp }
                    ?: error("No loan exists")
                if (amount <= 0.0) error("Amount must be greater than zero")
                val totalRepayment = sourceLoan.amount + (sourceLoan.amount * sourceLoan.interestRate)
                val outstanding = (totalRepayment - sourceLoan.repaidAmount).coerceAtLeast(0.0)
                if (amount > outstanding) error("Amount exceeds outstanding balance")
                if (sessionToken.isNullOrBlank() || !looksLikeJwt(sessionToken)) {
                    error("No valid Supabase session token.")
                }
                val txId = runRepayRemote(
                    sessionToken = sessionToken,
                    loanId = sourceLoan.referenceNumber.ifBlank { sourceLoan.loanId.toString() },
                    amountKes = amount,
                )
                val receiptRef = mpesaRef?.ifBlank { null }
                    ?: "MPESA${System.currentTimeMillis().toString().takeLast(8)}"
                receiptRef to txId
            }.onSuccess { (ref, txId) ->
                _repayState.value = RepayUiState.Success(ref, txId)
                loadLoan(loanId)
            }.onFailure {
                _repayState.value = RepayUiState.Error(it.message ?: "Repayment failed")
            }
        }
    }

    fun resetRepayState() {
        _repayState.value = RepayUiState.Idle
    }

    fun topUpLoan(loanId: Int, topUpAmount: Double, extensionDays: Int, purpose: String) {
        viewModelScope.launch {
            val normalizedPurpose = normalizePurpose(purpose)
            val userId = preferences.currentUserId.first()
            val sessionToken = preferences.sessionToken.value
            if (userId.isNullOrBlank()) {
                AuthEvents.emitSessionExpired()
                return@launch
            }
            _topupState.value = TopupUiState.Loading
            runCatching {
                val loans = repository.getLoans(userId).first()
                val profile = repository.getUserProfile(userId).first()
                val sourceLoan = loans.firstOrNull { it.loanId == loanId } ?: error("Loan not found")
                if (topUpAmount <= 0.0) error("Top up amount must be greater than zero")
                val paidLoansCount = loans.count { it.isPaid }
                val approvedLimit = CreditPolicy.resolveApprovedLimit(
                    salaryRange = profile?.salaryRange,
                    creditScore = profile?.creditScore ?: 500,
                    paidLoansCount = paidLoansCount,
                )
                val totalOutstanding = loans
                    .filter { !it.isPaid }
                    .sumOf { active -> ((active.amount + (active.amount * active.interestRate)) - active.repaidAmount).coerceAtLeast(0.0) }
                val availableHeadroom = (approvedLimit - totalOutstanding).coerceAtLeast(0.0)
                if (topUpAmount > availableHeadroom) {
                    error("Top up exceeds available limit. Max allowed is KES ${availableHeadroom.toInt()}.")
                }
                val mergedPrincipal = (((sourceLoan.amount + (sourceLoan.amount * sourceLoan.interestRate)) - sourceLoan.repaidAmount) + topUpAmount)
                    .coerceAtLeast(topUpAmount)
                    .toInt()
                val allowedDays = LoanCalculator.allowedTenureDaysFor(
                    userLimit = approvedLimit,
                    principal = mergedPrincipal,
                )
                if (extensionDays !in allowedDays) {
                    error("Selected days not allowed. Eligible days: ${allowedDays.joinToString(", ")}")
                }
                if (sessionToken.isNullOrBlank() || !looksLikeJwt(sessionToken)) {
                    // Fallback to local top-up when Supabase auth token is unavailable.
                    val updated = repository.topUpLoan(sourceLoan, topUpAmount, extensionDays, normalizedPurpose)
                    formatDate(updated.dueDate)
                } else {
                    val remoteDueDate = runCatching {
                        runTopupRemote(
                            sessionToken = sessionToken,
                            loanId = sourceLoan.referenceNumber.ifBlank { sourceLoan.loanId.toString() },
                            topUpAmount = topUpAmount,
                            extensionDays = extensionDays,
                            purpose = normalizedPurpose,
                        )
                    }.getOrNull()
                    if (remoteDueDate == null) {
                        // If backend ID/token context is unavailable, keep app functional via local cache path.
                        val fallback = repository.topUpLoan(sourceLoan, topUpAmount, extensionDays, normalizedPurpose)
                        formatDate(fallback.dueDate)
                    } else {
                        // Keep local cache in sync with backend-confirmed top-up.
                        repository.topUpLoan(sourceLoan, topUpAmount, extensionDays, normalizedPurpose)
                        remoteDueDate
                    }
                }
            }.onSuccess { due ->
                _topupState.value = TopupUiState.Success(newDueDate = due)
                loadLoan(loanId)
            }.onFailure {
                _topupState.value = TopupUiState.Error(it.message ?: "Top up failed")
            }
        }
    }

    fun resetTopupState() {
        _topupState.value = TopupUiState.Idle
    }

    private suspend fun runTopupRemote(
        sessionToken: String,
        loanId: String,
        topUpAmount: Double,
        extensionDays: Int,
        purpose: String,
    ): String = withContext(Dispatchers.IO) {
        val conn = (URL("$supabaseFunctionsBaseUrl/topup-loan").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $sessionToken")
            setRequestProperty("Idempotency-Key", "topup-${System.currentTimeMillis()}-${loanId.takeLast(8)}")
        }
        val body = JSONObject()
            .put("loanId", loanId)
            .put("topupAmountKes", topUpAmount)
            .put("extensionDays", extensionDays)
            .put("topupPurpose", purpose)
            .toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        val response = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()
        if (code !in 200..299) {
            throw IllegalStateException("Top up backend failed (${code}): ${response.take(180)}")
        }
        val json = JSONObject(response)
        val dueAt = json.optJSONObject("loan")?.optString("due_at").orEmpty()
        if (dueAt.isBlank()) return@withContext formatDate(System.currentTimeMillis())
        val parsed = runCatching { java.time.Instant.parse(dueAt).toEpochMilli() }.getOrElse { System.currentTimeMillis() }
        formatDate(parsed)
    }

    private suspend fun runRepayRemote(
        sessionToken: String,
        loanId: String,
        amountKes: Double,
    ): Int = withContext(Dispatchers.IO) {
        val conn = (URL("$supabaseFunctionsBaseUrl/repay-loan").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $sessionToken")
            setRequestProperty("Idempotency-Key", "repay-${System.currentTimeMillis()}-${loanId.takeLast(8)}")
        }
        val body = JSONObject()
            .put("loanId", loanId)
            .put("amountKes", amountKes)
            .put("source", "APP")
            .toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        val response = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()
        if (code !in 200..299) {
            throw IllegalStateException("Repayment backend failed ($code): ${response.take(180)}")
        }
        JSONObject(response).optJSONObject("transaction")?.optInt("id", 0) ?: 0
    }

    private fun looksLikeJwt(token: String): Boolean = token.count { it == '.' } == 2
}
