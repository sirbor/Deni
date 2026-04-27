package com.loki.deni.domain.repository

import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

interface DeniRepository {
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>
    suspend fun getUserByPhone(phone: String): UserProfileEntity?
    suspend fun upsertUserProfile(userProfile: UserProfileEntity)
    
    fun getLoans(userId: String): Flow<List<LoanEntity>>
    fun getActiveLoan(userId: String): Flow<LoanEntity?>
    suspend fun reconcileLoanStatuses(userId: String)
    suspend fun applyForLoan(loan: LoanEntity)
    suspend fun repayLoan(loan: LoanEntity, amount: Double)
    suspend fun topUpLoan(loan: LoanEntity, topUpAmount: Double, extensionDays: Int, purpose: String): LoanEntity
    
    fun getTransactions(userId: String): Flow<List<TransactionEntity>>
    suspend fun getTransactionById(transactionId: Int): TransactionEntity?
    suspend fun updateCreditScore(userId: String, points: Int)
}
