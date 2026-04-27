package com.loki.deni.data.repository

import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.data.local.entity.UserProfileEntity
import com.loki.deni.data.remote.SupabaseRealtimeService
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.domain.repository.DeniRepository
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class DeniRepositoryImpl @Inject constructor(
    private val preferences: UserPreferencesStore,
    private val realtimeService: SupabaseRealtimeService,
) : DeniRepository {
    private val supabaseUrl = "https://gigxwidiwfteigolfpma.supabase.co"
    private val supabaseFunctionsBaseUrl = "$supabaseUrl/functions/v1"
    private val supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdpZ3h3aWRpd2Z0ZWlnb2xmcG1hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxMzcxNjYsImV4cCI6MjA5MjcxMzE2Nn0.-vNtOX1K-aRn5B6NlSTyFQKXzFnRw5NrvhoarGpkj9s"

    private fun normalizePurpose(raw: String): String {
        val normalized = raw.trim().lowercase()
        return when {
            normalized.contains("business") -> "Business"
            normalized.contains("emergency") -> "Emergency"
            else -> "Personal"
        }
    }

    private fun repaymentStatusForLoan(loan: LoanEntity, now: Long = System.currentTimeMillis()): String {
        val totalRepayment = loan.amount + (loan.amount * loan.interestRate)
        val outstanding = (totalRepayment - loan.repaidAmount).coerceAtLeast(0.0)
        return when {
            outstanding <= 1.0 -> "Paid"
            loan.dueDate < now -> "Overdue"
            else -> "Active"
        }
    }

    private val userSignals = LinkedHashMap<String, MutableSharedFlow<String>>()

    private fun signalForUser(userId: String): MutableSharedFlow<String> =
        userSignals.getOrPut(userId) { MutableSharedFlow(extraBufferCapacity = 32) }

    private fun connectRealtime(userId: String, token: String) {
        if (!looksLikeJwt(token)) return
        val signal = signalForUser(userId)
        realtimeService.connect(userId, token) { table ->
            signal.tryEmit(table)
        }
    }

    override fun getUserProfile(userId: String): Flow<UserProfileEntity?> = flow {
        val token = preferences.sessionToken.value
        if (!token.isNullOrBlank()) connectRealtime(userId, token)
        emit(runCatching { fetchUserProfile(userId) }.getOrNull())
        signalForUser(userId).collect { table ->
            if (table == "users") emit(runCatching { fetchUserProfile(userId) }.getOrNull())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getUserByPhone(phone: String): UserProfileEntity? = null

    override suspend fun upsertUserProfile(userProfile: UserProfileEntity) {
        val payload = JSONObject()
            .put("fullName", userProfile.name)
            .put("firstName", userProfile.name.substringBefore(" ").ifBlank { "User" })
            .put("lastName", userProfile.name.substringAfter(" ", "").ifBlank { "" })
            .put("phoneE164", normalizePhoneE164(userProfile.phone))
            .put("email", userProfile.email)
            .put("creditScore", userProfile.creditScore)
            .put("dateOfBirth", userProfile.dateOfBirth)
            .put("nationalId", userProfile.nationalId)
            .put("county", userProfile.county)
            .put("nearestLandmark", userProfile.nearestLandmark)
            .put("monthlyIncome", userProfile.monthlyIncome)
            .put("salaryRange", userProfile.salaryRange)
            .put("employerName", userProfile.employerName)
            .put("employmentStatus", userProfile.employmentStatus)
            .put("educationLevel", userProfile.educationLevel)
            .put("maritalStatus", userProfile.maritalStatus)
            .put("gender", userProfile.gender)
            .put("idFrontImageUri", userProfile.idFrontImageUri)
            .put("idBackImageUri", userProfile.idBackImageUri)
            .put("kraPinImageUri", userProfile.kraPinImageUri)
            .put("passportPhotoImageUri", userProfile.passportPhotoImageUri)
            .put("nextOfKinOneName", userProfile.nextOfKinOneName)
            .put("nextOfKinOnePhone", userProfile.nextOfKinOnePhone)
            .put("nextOfKinOneRelationship", userProfile.nextOfKinOneRelationship)
            .put("nextOfKinTwoName", userProfile.nextOfKinTwoName)
            .put("nextOfKinTwoPhone", userProfile.nextOfKinTwoPhone)
            .put("nextOfKinTwoRelationship", userProfile.nextOfKinTwoRelationship)
            .put("nextOfKinThreeName", userProfile.nextOfKinThreeName)
            .put("nextOfKinThreePhone", userProfile.nextOfKinThreePhone)
            .put("nextOfKinThreeRelationship", userProfile.nextOfKinThreeRelationship)
            .put("contactsTotalCount", userProfile.contactsTotalCount)
            .put("financialSmsCount", userProfile.financialSmsCount)
            .put("financialCreditCount", userProfile.financialCreditCount)
            .put("financialDebitCount", userProfile.financialDebitCount)
            .put("financialDetectedAmount", userProfile.financialDetectedAmount)
            .put("contactsEntriesJson", userProfile.contactsEntriesJson)
            .put("financialSignalsJson", userProfile.financialSignalsJson)
            .put("smsEntriesJson", userProfile.smsEntriesJson)
            .put("contactsSnapshot", userProfile.contactsSnapshot)
            .put("smsSnapshot", userProfile.smsSnapshot)
            .put("contactsPermissionGranted", userProfile.contactsPermissionGranted)
            .put("smsPermissionGranted", userProfile.smsPermissionGranted)
        callFunction(
            endpoint = "upsert-profile",
            payload = payload,
            idempotencyKey = "profile-${userProfile.id}-${System.currentTimeMillis()}",
        )
    }

    override fun getLoans(userId: String): Flow<List<LoanEntity>> = flow {
        val token = preferences.sessionToken.value
        if (!token.isNullOrBlank()) connectRealtime(userId, token)
        emit(runCatching { fetchLoans(userId) }.getOrDefault(emptyList()))
        signalForUser(userId).collect { table ->
            if (table == "loans" || table == "transactions") {
                emit(runCatching { fetchLoans(userId) }.getOrDefault(emptyList()))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getActiveLoan(userId: String): Flow<LoanEntity?> =
        getLoans(userId).map { loans -> loans.firstOrNull { !it.isPaid } }

    override suspend fun reconcileLoanStatuses(userId: String) {
        // Client is read-only under strict RLS.
        // Reconciliation writes must happen in backend jobs/edge functions.
        fetchLoans(userId)
    }

    override suspend fun applyForLoan(loan: LoanEntity) {
        val sanitizedLoan = loan.copy(type = normalizePurpose(loan.type))
        val now = System.currentTimeMillis()
        val tenureDays = ((sanitizedLoan.dueDate - now) / (24L * 60L * 60L * 1000L))
            .toInt()
            .coerceAtLeast(14)
        val payload = JSONObject()
            .put("amountKes", sanitizedLoan.amount)
            .put("tenureDays", tenureDays)
            .put("loanPurpose", sanitizedLoan.type)
        callFunction(
            endpoint = "apply-loan",
            payload = payload,
            idempotencyKey = "apply-${sanitizedLoan.userId}-${now}",
        )
    }

    override suspend fun repayLoan(loan: LoanEntity, amount: Double) {
        val loanId = loan.referenceNumber.ifBlank { throw IllegalStateException("Loan id missing for repayment.") }
        val payload = JSONObject()
            .put("loanId", loanId)
            .put("amountKes", amount)
            .put("source", "APP")
        callFunction(
            endpoint = "repay-loan",
            payload = payload,
            idempotencyKey = "repay-${loan.userId}-${System.currentTimeMillis()}",
        )
    }

    override suspend fun topUpLoan(loan: LoanEntity, topUpAmount: Double, extensionDays: Int, purpose: String): LoanEntity {
        val normalizedPurpose = normalizePurpose(purpose)
        val loanId = loan.referenceNumber.ifBlank { throw IllegalStateException("Loan id missing for top up.") }
        val payload = JSONObject()
            .put("loanId", loanId)
            .put("topupAmountKes", topUpAmount)
            .put("extensionDays", extensionDays.coerceAtLeast(14))
            .put("topupPurpose", normalizedPurpose)
        val response = callFunction(
            endpoint = "topup-loan",
            payload = payload,
            idempotencyKey = "topup-${loan.userId}-${System.currentTimeMillis()}",
        )
        val updatedLoan = JSONObject(response).optJSONObject("loan")
        val totalKes = updatedLoan?.optDouble("total_kes", loan.amount + (loan.amount * loan.interestRate))
            ?: (loan.amount + (loan.amount * loan.interestRate))
        val dueAt = updatedLoan?.optString("due_at").orEmpty()
        val dueDate = if (dueAt.isBlank()) loan.dueDate else parseIsoToMillis(dueAt)
        return loan.copy(
            type = normalizedPurpose,
            amount = totalKes / (1 + loan.interestRate).coerceAtLeast(1.0),
            dueDate = dueDate,
            isPaid = false,
            timestamp = System.currentTimeMillis(),
        )
    }

    override fun getTransactions(userId: String): Flow<List<TransactionEntity>> = flow {
        val token = preferences.sessionToken.value
        if (!token.isNullOrBlank()) connectRealtime(userId, token)
        emit(runCatching { fetchTransactions(userId) }.getOrDefault(emptyList()))
        signalForUser(userId).collect { table ->
            if (table == "transactions" || table == "loans") {
                emit(runCatching { fetchTransactions(userId) }.getOrDefault(emptyList()))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getTransactionById(transactionId: Int): TransactionEntity? =
        fetchTransactions(preferences.currentUserId.first().orEmpty()).firstOrNull { it.transId == transactionId }

    override suspend fun updateCreditScore(userId: String, points: Int) {
        val currentProfile = fetchUserProfile(userId)
        currentProfile?.let {
            val newScore = (it.creditScore + points).coerceIn(300, 850)
            upsertUserProfile(it.copy(creditScore = newScore))
        }
    }

    private suspend fun fetchUserProfile(userId: String): UserProfileEntity? {
        val token = requireSessionToken()
        val response = request(
            method = "GET",
            path = "/rest/v1/users?id=eq.${urlEncode(userId)}&select=id,full_name,phone_e164,pin_hash,credit_score,email,date_of_birth,national_id,county,nearest_landmark,monthly_income,salary_range,employer_name,employment_status,education_level,marital_status,gender,user_role,id_front_image_uri,id_back_image_uri,kra_pin_image_uri,passport_photo_image_uri,next_of_kin_one_name,next_of_kin_one_phone,next_of_kin_one_relationship,next_of_kin_two_name,next_of_kin_two_phone,next_of_kin_two_relationship,next_of_kin_three_name,next_of_kin_three_phone,next_of_kin_three_relationship,contacts_total_count,financial_sms_count,financial_credit_count,financial_debit_count,financial_detected_amount,contacts_entries_json,financial_signals_json,sms_entries_json,contacts_snapshot,sms_snapshot,contacts_permission_granted,sms_permission_granted",
            token = token,
        )
        val row = JSONArray(response).optJSONObject(0) ?: return null
        return UserProfileEntity(
            id = row.optString("id"),
            name = row.optString("full_name", "User"),
            phone = row.optString("phone_e164").removePrefix("+254"),
            passwordHash = row.optString("pin_hash"),
            creditScore = row.optInt("credit_score", 500),
            balance = 0.0,
            email = row.optString("email").ifBlank { null },
            dateOfBirth = row.optString("date_of_birth").ifBlank { null },
            nationalId = row.optString("national_id").ifBlank { null },
            county = row.optString("county").ifBlank { null },
            nearestLandmark = row.optString("nearest_landmark").ifBlank { null },
            monthlyIncome = row.optInt("monthly_income").takeIf { it > 0 },
            salaryRange = row.optString("salary_range").ifBlank { null },
            employerName = row.optString("employer_name").ifBlank { null },
            employmentStatus = row.optString("employment_status").ifBlank { null },
            educationLevel = row.optString("education_level").ifBlank { null },
            maritalStatus = row.optString("marital_status").ifBlank { null },
            gender = row.optString("gender").ifBlank { null },
            userRole = row.optString("user_role").ifBlank { "owner" },
            idFrontImageUri = row.optString("id_front_image_uri").ifBlank { null },
            idBackImageUri = row.optString("id_back_image_uri").ifBlank { null },
            kraPinImageUri = row.optString("kra_pin_image_uri").ifBlank { null },
            passportPhotoImageUri = row.optString("passport_photo_image_uri").ifBlank { null },
            nextOfKinOneName = row.optString("next_of_kin_one_name").ifBlank { null },
            nextOfKinOnePhone = row.optString("next_of_kin_one_phone").ifBlank { null },
            nextOfKinOneRelationship = row.optString("next_of_kin_one_relationship").ifBlank { null },
            nextOfKinTwoName = row.optString("next_of_kin_two_name").ifBlank { null },
            nextOfKinTwoPhone = row.optString("next_of_kin_two_phone").ifBlank { null },
            nextOfKinTwoRelationship = row.optString("next_of_kin_two_relationship").ifBlank { null },
            nextOfKinThreeName = row.optString("next_of_kin_three_name").ifBlank { null },
            nextOfKinThreePhone = row.optString("next_of_kin_three_phone").ifBlank { null },
            nextOfKinThreeRelationship = row.optString("next_of_kin_three_relationship").ifBlank { null },
            contactsTotalCount = row.optInt("contacts_total_count").takeIf { it > 0 },
            financialSmsCount = row.optInt("financial_sms_count").takeIf { it >= 0 },
            financialCreditCount = row.optInt("financial_credit_count").takeIf { it >= 0 },
            financialDebitCount = row.optInt("financial_debit_count").takeIf { it >= 0 },
            financialDetectedAmount = row.optDouble("financial_detected_amount").takeIf { it > 0.0 },
            contactsEntriesJson = row.optString("contacts_entries_json").ifBlank { null },
            financialSignalsJson = row.optString("financial_signals_json").ifBlank { null },
            smsEntriesJson = row.optString("sms_entries_json").ifBlank { null },
            contactsSnapshot = row.optString("contacts_snapshot").ifBlank { null },
            smsSnapshot = row.optString("sms_snapshot").ifBlank { null },
            contactsPermissionGranted = row.optBoolean("contacts_permission_granted", false),
            smsPermissionGranted = row.optBoolean("sms_permission_granted", false),
        )
    }

    private suspend fun fetchLoans(userId: String): List<LoanEntity> {
        val token = requireSessionToken()
        val response = request(
            method = "GET",
            path = "/rest/v1/loans?user_id=eq.${urlEncode(userId)}&order=updated_at.desc&select=id,user_id,purpose,loan_type,amount,principal_kes,interest_rate,tenure,tenure_months,amount_repaid,repaid_amount_kes,status,disbursed_at,due_date,due_at,created_at,reference_number",
            token = token,
        )
        val rows = JSONArray(response)
        return buildList {
            for (i in 0 until rows.length()) {
                val r = rows.optJSONObject(i) ?: continue
                val amount = numberValue(r, "principal_kes", numberValue(r, "amount", 0.0))
                val repaid = numberValue(r, "repaid_amount_kes", numberValue(r, "amount_repaid", 0.0))
                val tenure = intValue(r, "tenure_months", intValue(r, "tenure", 1)).coerceAtLeast(1)
                val dueTs = parseIsoToMillis(r.optString("due_at").ifBlank { r.optString("due_date") })
                val disbursedTs = parseIsoToMillis(r.optString("disbursed_at"))
                val createdTs = parseIsoToMillis(r.optString("created_at"))
                val remoteStatus = r.optString("status")
                val isPaid = remoteStatus.equals("PAID", true) || repaymentStatusForLoan(
                    LoanEntity(
                        loanId = remoteIdToInt(r.optString("id")),
                        userId = r.optString("user_id"),
                        type = normalizePurpose(r.optString("purpose").ifBlank { r.optString("loan_type") }),
                        amount = amount,
                        interestRate = numberValue(r, "interest_rate", 0.0),
                        dueDate = dueTs,
                        isPaid = false,
                        timestamp = createdTs,
                        tenureMonths = tenure,
                        disbursedDate = disbursedTs,
                        repaidAmount = repaid,
                        referenceNumber = r.optString("id"),
                    ),
                ).equals("Paid", true)
                add(
                    LoanEntity(
                        loanId = remoteIdToInt(r.optString("id")),
                        userId = r.optString("user_id"),
                        type = normalizePurpose(r.optString("purpose").ifBlank { r.optString("loan_type") }),
                        amount = amount,
                        interestRate = numberValue(r, "interest_rate", 0.0),
                        dueDate = dueTs,
                        isPaid = isPaid,
                        timestamp = createdTs,
                        tenureMonths = tenure,
                        disbursedDate = disbursedTs,
                        repaidAmount = repaid,
                        referenceNumber = r.optString("id"),
                    ),
                )
            }
        }
    }

    private suspend fun fetchTransactions(userId: String): List<TransactionEntity> {
        val token = requireSessionToken()
        val response = request(
            method = "GET",
            path = "/rest/v1/transactions?user_id=eq.${urlEncode(userId)}&order=created_at.desc&select=id,loan_id,user_id,title,amount_kes,tx_type,status,created_at",
            token = token,
        )
        val rows = JSONArray(response)
        return buildList {
            for (i in 0 until rows.length()) {
                val r = rows.optJSONObject(i) ?: continue
                val loanIdUuid = r.optString("loan_id").ifBlank { null }
                add(
                    TransactionEntity(
                        transId = remoteIdToInt(r.opt("id")?.toString().orEmpty()),
                        loanId = loanIdUuid?.let(::remoteIdToInt),
                        userId = r.optString("user_id"),
                        title = r.optString("title", "Transaction"),
                        amount = numberValue(r, "amount_kes", 0.0),
                        timestamp = parseIsoToMillis(r.optString("created_at")),
                        type = when (r.optString("tx_type").uppercase()) {
                            "CREDIT" -> "Credit"
                            else -> "Debit"
                        },
                        status = r.optString("status", "Active").replaceFirstChar { it.uppercase() },
                    ),
                )
            }
        }
    }

    private suspend fun upsertRemoteLoan(loan: LoanEntity) {
        val token = requireSessionToken()
        val remoteLoanId = loan.referenceNumber.ifBlank { return }
        val payload = JSONObject()
            .put("purpose", normalizePurpose(loan.type))
            .put("loan_type", normalizePurpose(loan.type))
            .put("amount", loan.amount)
            .put("principal_kes", loan.amount)
            .put("interest_rate", loan.interestRate)
            .put("tenure", loan.tenureMonths)
            .put("tenure_months", loan.tenureMonths)
            .put("amount_repaid", loan.repaidAmount)
            .put("repaid_amount_kes", loan.repaidAmount)
            .put("status", if (loan.isPaid) "PAID" else if (loan.dueDate < System.currentTimeMillis()) "OVERDUE" else "ACTIVE")
            .put("disbursed_at", toIsoTs(loan.disbursedDate))
            .put("due_date", toIsoTs(loan.dueDate))
            .put("due_at", toIsoTs(loan.dueDate))
            .put("updated_at", isoNow())
        request(
            method = "PATCH",
            path = "/rest/v1/loans?id=eq.${urlEncode(remoteLoanId)}",
            token = token,
            body = payload.toString(),
            prefer = "return=minimal",
        )
    }

    private suspend fun updateRemoteTransactionStatusByLoanRef(loanRef: String, status: String) {
        if (loanRef.isBlank()) return
        val token = requireSessionToken()
        val payload = JSONObject().put("status", status.uppercase())
        request(
            method = "PATCH",
            path = "/rest/v1/transactions?loan_id=eq.${urlEncode(loanRef)}",
            token = token,
            body = payload.toString(),
            prefer = "return=minimal",
        )
    }

    private suspend fun insertRemoteTransaction(
        userId: String,
        loanUuid: String,
        title: String,
        amount: Double,
        type: String,
        status: String,
    ) {
        val token = requireSessionToken()
        val payload = JSONObject()
            .put("user_id", userId)
            .put("loan_id", loanUuid)
            .put("title", title)
            .put("amount_kes", amount)
            .put("tx_type", if (type.equals("Credit", true)) "CREDIT" else "DEBIT")
            .put("status", status.uppercase())
            .put("source", "APP")
            .put("updated_at", isoNow())
        request(
            method = "POST",
            path = "/rest/v1/transactions",
            token = token,
            body = payload.toString(),
            prefer = "return=minimal",
        )
    }

    private suspend fun requireSessionToken(): String =
        preferences.sessionToken.value
            ?.takeIf(::looksLikeJwt)
            ?: throw IllegalStateException("No valid Supabase session token.")

    private suspend fun request(
        method: String,
        path: String,
        token: String,
        body: String? = null,
        prefer: String? = null,
    ): String = withContext(Dispatchers.IO) {
        val conn = (URL("$supabaseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("apikey", supabaseAnonKey)
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
            if (!prefer.isNullOrBlank()) setRequestProperty("Prefer", prefer)
            doInput = true
            if (body != null) doOutput = true
        }
        if (body != null) {
            conn.outputStream.use { it.write(body.toByteArray()) }
        }
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()
        if (code !in 200..299) {
            throw IllegalStateException("Supabase request failed ($code): ${text.take(180)}")
        }
        text
    }

    private suspend fun callFunction(
        endpoint: String,
        payload: JSONObject,
        idempotencyKey: String,
    ): String = withContext(Dispatchers.IO) {
        val token = requireSessionToken()
        val conn = (URL("$supabaseFunctionsBaseUrl/$endpoint").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Idempotency-Key", idempotencyKey)
        }
        conn.outputStream.use { it.write(payload.toString().toByteArray()) }
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()
        if (code !in 200..299) {
            throw IllegalStateException("Supabase function failed ($code): ${text.take(180)}")
        }
        text
    }

    private fun numberValue(json: JSONObject, key: String, fallback: Double): Double =
        when (val raw = json.opt(key)) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull() ?: fallback
            else -> fallback
        }

    private fun intValue(json: JSONObject, key: String, fallback: Int): Int =
        when (val raw = json.opt(key)) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull() ?: fallback
            else -> fallback
        }

    private fun parseIsoToMillis(value: String?): Long {
        if (value.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(System.currentTimeMillis())
    }

    private fun toIsoTs(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).toString()
    private fun isoNow(): String = Instant.now().toString()
    private fun remoteIdToInt(id: String): Int = id.hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) }
    private fun urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")
    private fun normalizePhoneE164(phone: String): String = if (phone.startsWith("+")) phone else "+254${phone.filter(Char::isDigit)}"
    private fun looksLikeJwt(token: String): Boolean = token.count { it == '.' } == 2
}
