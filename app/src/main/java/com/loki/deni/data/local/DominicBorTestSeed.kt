package com.loki.deni.data.local

import com.loki.deni.data.local.dao.DeniDao
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.data.local.entity.UserProfileEntity
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlinx.coroutines.flow.first

/**
 * Dev/test profile: sign in with phone **722000001** and PIN **1234**.
 * ~2 months of activity, KES 300,000 total transaction volume, each txn ≤ KES 15,000.
 */
object DominicBorTestSeed {

    const val USER_ID = "seed-user-dominic-bor"
    const val PHONE = "722000001"
    private const val PIN_1234_SHA256 =
        "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"

    private val transactionTitles = listOf(
        "M-Pesa repayment",
        "Loan disbursement",
        "M-Pesa transfer",
        "Wallet top up",
        "M-Pesa repayment",
        "Loan disbursement",
        "M-Pesa pay bill",
        "M-Pesa repayment",
        "Loan disbursement",
        "M-Pesa send money",
        "M-Pesa repayment",
        "Loan disbursement",
        "M-Pesa repayment",
        "Airtime purchase",
        "M-Pesa repayment",
        "Loan disbursement",
        "M-Pesa repayment",
        "M-Pesa withdrawal",
        "M-Pesa repayment",
        "Loan disbursement",
    )

    suspend fun seedIfNeeded(dao: DeniDao) {
        val existingByPhone = dao.getUserByPhone(PHONE)
        val userId = existingByPhone?.id ?: USER_ID
        val now = System.currentTimeMillis()
        val twoMonthsMs = TimeUnit.DAYS.toMillis(62)

        dao.upsertUserProfile(
            UserProfileEntity(
                id = userId,
                name = "Dominic Bor",
                phone = PHONE,
                passwordHash = PIN_1234_SHA256,
                creditScore = maxOf(existingByPhone?.creditScore ?: 0, 712),
                balance = maxOf(existingByPhone?.balance ?: 0.0, 18_450.75),
                email = existingByPhone?.email ?: "dominic.bor@test.deni",
            ),
        )

        val disbursedPast = now - TimeUnit.DAYS.toMillis(58)
        val dueSoon = now + TimeUnit.DAYS.toMillis(18)
        val existingLoans = dao.getLoansByUserId(userId).first()
        if (existingLoans.isEmpty()) {
            dao.insertLoan(
                LoanEntity(
                    loanId = 0,
                    userId = userId,
                    type = "Personal loan",
                    amount = 45_000.0,
                    interestRate = 0.12,
                    dueDate = disbursedPast + TimeUnit.DAYS.toMillis(30),
                    isPaid = true,
                    timestamp = disbursedPast,
                    tenureMonths = 1,
                    disbursedDate = disbursedPast,
                    repaidAmount = 50_400.0,
                    referenceNumber = "DN-SEED-001",
                ),
            )

            dao.insertLoan(
                LoanEntity(
                    loanId = 0,
                    userId = userId,
                    type = "Salary advance",
                    amount = 80_000.0,
                    interestRate = 0.10,
                    dueDate = dueSoon,
                    isPaid = false,
                    timestamp = now - TimeUnit.DAYS.toMillis(12),
                    tenureMonths = 3,
                    disbursedDate = now - TimeUnit.DAYS.toMillis(12),
                    repaidAmount = 35_000.0,
                    referenceNumber = "DN-SEED-002",
                ),
            )
        }

        // Ensure loan principal totals also reflect KES 300,000 in "My Loans"/loan aggregates.
        val targetLoanPrincipal = 300_000.0
        val refreshedLoans = dao.getLoansByUserId(userId).first()
        var remainingLoanPrincipal = (targetLoanPrincipal - refreshedLoans.sumOf { it.amount }).coerceAtLeast(0.0)
        var extraLoanIndex = 1
        while (remainingLoanPrincipal > 0.0) {
            val principal = minOf(75_000.0, remainingLoanPrincipal)
            val disbursedAt = now - TimeUnit.DAYS.toMillis((40L + extraLoanIndex * 8L).coerceAtMost(110L))
            val dueAt = disbursedAt + TimeUnit.DAYS.toMillis(30)
            val interestRate = 0.12
            val totalRepayment = principal + (principal * interestRate)
            dao.insertLoan(
                LoanEntity(
                    loanId = 0,
                    userId = userId,
                    type = "Personal loan",
                    amount = principal,
                    interestRate = interestRate,
                    dueDate = dueAt,
                    isPaid = true,
                    timestamp = disbursedAt,
                    tenureMonths = 1,
                    disbursedDate = disbursedAt,
                    repaidAmount = totalRepayment,
                    referenceNumber = "DN-SEED-EXTRA-${extraLoanIndex.toString().padStart(2, '0')}",
                ),
            )
            remainingLoanPrincipal -= principal
            extraLoanIndex++
        }

        val existingTransactions = dao.getTransactionsByUserId(userId).first()
        val currentVolume = existingTransactions.sumOf { it.amount }
        val targetVolume = 300_000.0
        val missing = (targetVolume - currentVolume).coerceAtLeast(0.0)
        if (missing <= 0.0) return

        val maxTxn = 15_000.0
        val neededCount = ceil(missing / maxTxn).toInt().coerceAtLeast(1)
        var remaining = missing
        repeat(neededCount) { index ->
            val amount = minOf(maxTxn, remaining)
            remaining -= amount
            val fraction = if (neededCount <= 1) 0.0 else index.toDouble() / (neededCount - 1)
            val timestamp = (now - twoMonthsMs + (fraction * twoMonthsMs).toLong())
                .coerceIn(now - twoMonthsMs, now - TimeUnit.HOURS.toMillis(2))
            val isCredit = index % 2 == 0
            dao.insertTransaction(
                TransactionEntity(
                    loanId = null,
                    userId = userId,
                    title = transactionTitles[index % transactionTitles.size],
                    amount = amount,
                    timestamp = timestamp,
                    type = if (isCredit) "Credit" else "Debit",
                    status = "Completed",
                ),
            )
        }
    }
}
