package com.loki.deni.data.local.dao

import androidx.room.*
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeniDao {
    @Query("SELECT * FROM user_profile WHERE id = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE phone = :phone")
    suspend fun getUserByPhone(phone: String): UserProfileEntity?

    @Upsert
    suspend fun upsertUserProfile(userProfile: UserProfileEntity)

    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLoansByUserId(userId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE userId = :userId AND isPaid = 0 LIMIT 1")
    fun getActiveLoan(userId: String): Flow<LoanEntity?>

    @Upsert
    suspend fun upsertLoan(loan: LoanEntity)

    @Insert
    suspend fun insertLoan(loan: LoanEntity): Long

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transId = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Int): TransactionEntity?

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = :status WHERE loanId = :loanId")
    suspend fun updateTransactionStatusByLoanId(loanId: Int, status: String)
}
