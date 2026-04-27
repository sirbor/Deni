package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.data.local.entity.UserProfileEntity
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.util.core.CreditPolicy
import java.net.HttpURLConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import java.security.MessageDigest
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

sealed interface AuthUiState<out T> {
    data object Idle : AuthUiState<Nothing>
    data object Loading : AuthUiState<Nothing>
    data class Success<T>(val data: T) : AuthUiState<T>
    data class Error(val message: String) : AuthUiState<Nothing>
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferences: UserPreferencesStore,
    private val repository: DeniRepository,
) : ViewModel() {
    private val supabaseFunctionsBaseUrl = "https://gigxwidiwfteigolfpma.supabase.co/functions/v1"
    private data class PendingSignup(
        val phone: String,
        val pinPlain: String,
        val pinHash: String,
        val biometricsEnabled: Boolean,
    )

    private val _signInState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val signInState: StateFlow<AuthUiState<Unit>> = _signInState.asStateFlow()
    private val _checkState = MutableStateFlow<AuthUiState<Boolean>>(AuthUiState.Idle)
    val checkState: StateFlow<AuthUiState<Boolean>> = _checkState.asStateFlow()
    private val _createState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val createState: StateFlow<AuthUiState<Unit>> = _createState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = preferences.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val wrongAttempts: StateFlow<Int> = preferences.wrongPinAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val biometricsEnabled: StateFlow<Boolean> = preferences.biometricsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var pendingSignup: PendingSignup? = null

    fun signIn(phone: String, pin: String, countryCode: String = "+254") {
        viewModelScope.launch {
            _signInState.value = AuthUiState.Loading
            val normalizedPhone = normalizePhone(phone, countryCode)
            runCatching {
                signInRemote(normalizedPhone, pin)
            }.onSuccess { remote ->
                preferences.setSessionToken(remote.accessToken)
                preferences.saveCredentials(normalizedPhone, sha256(pin))
                preferences.setUserId(remote.userId)
                preferences.setFirstName(remote.firstName.ifBlank { "User" })
                preferences.resetWrongAttempts()
                _signInState.value = AuthUiState.Success(Unit)
            }.onFailure {
                preferences.incrementWrongAttempts()
                _signInState.value = AuthUiState.Error("wrong_credentials")
            }
        }
    }

    fun checkPhoneAvailable(phone: String, countryCode: String = "+254") {
        viewModelScope.launch {
            _checkState.value = AuthUiState.Loading
            runCatching {
                // Availability check now follows backend-first auth path.
                // We keep this permissive and enforce uniqueness at register call.
                normalizePhone(phone, countryCode).isNotBlank()
            }.onSuccess {
                _checkState.value = AuthUiState.Success(it)
            }.onFailure {
                _checkState.value = AuthUiState.Error(it.message ?: "Something went wrong. Try again.")
            }
        }
    }

    fun createAccount(phone: String, pin: String, countryCode: String = "+254") {
        viewModelScope.launch {
            _createState.value = AuthUiState.Loading
            val normalizedPhone = normalizePhone(phone, countryCode)
            val pinHash = sha256(pin)
            runCatching {
                registerRemote(
                    firstName = "User",
                    lastName = "",
                    phone = normalizedPhone,
                    pin = pin,
                )
            }.onSuccess { remote ->
                preferences.saveCredentials(phone = normalizedPhone, pinHash = pinHash)
                preferences.setSessionToken(remote.accessToken)
                preferences.setUserId(remote.userId)
                preferences.setFirstName("User")
                runCatching { createWelcomeTransaction(remote.accessToken, remote.userId) }
                preferences.resetWrongAttempts()
                _createState.value = AuthUiState.Success(Unit)
            }.onFailure {
                _createState.value = AuthUiState.Error(it.message ?: "Unable to create account.")
            }
        }
    }

    fun setPendingSignup(phone: String, pin: String, biometricsEnabled: Boolean) {
        val pinHash = sha256(pin)
        pendingSignup = PendingSignup(
            phone = phone,
            pinPlain = pin,
            pinHash = pinHash,
            biometricsEnabled = biometricsEnabled,
        )
        viewModelScope.launch {
            preferences.saveCredentials(phone = phone, pinHash = pinHash)
            preferences.setPendingSignupPinPlain(pin)
        }
    }

    fun completeSignupProfile(
        firstName: String,
        lastName: String,
        email: String,
        dob: String,
        nationalId: String,
        county: String,
        nearestLandmark: String,
        salaryRange: String,
        employer: String,
        educationLevel: String,
        maritalStatus: String,
        gender: String,
        idFrontImageUri: String?,
        idBackImageUri: String?,
        kraPinImageUri: String?,
        passportPhotoImageUri: String?,
        nextOfKinOneName: String?,
        nextOfKinOnePhone: String?,
        nextOfKinOneRelationship: String?,
        nextOfKinTwoName: String?,
        nextOfKinTwoPhone: String?,
        nextOfKinTwoRelationship: String?,
        nextOfKinThreeName: String?,
        nextOfKinThreePhone: String?,
        nextOfKinThreeRelationship: String?,
        contactsEntriesJson: String?,
        financialSignalsJson: String?,
        smsEntriesJson: String?,
        contactsSnapshot: String?,
        smsSnapshot: String?,
        contactsPermissionGranted: Boolean,
        smsPermissionGranted: Boolean,
    ) {
        viewModelScope.launch {
            _createState.value = AuthUiState.Loading
            val pending = pendingSignup
            val normalizedPhone = pending?.phone ?: preferences.savedPhone.first()
            val pinHash = pending?.pinHash ?: preferences.savedPin.first()
            if (normalizedPhone.isNullOrBlank() || pinHash.isNullOrBlank()) {
                _createState.value = AuthUiState.Error("Signup session expired. Please start again.")
                return@launch
            }

            runCatching {
                withTimeout(25_000) {
                    val fullName = "$firstName $lastName".trim()
                    var userId = preferences.currentUserId.first().orEmpty()
                    var sessionToken = preferences.sessionToken.value
                    if (sessionToken.isNullOrBlank() || !looksLikeJwt(sessionToken)) {
                        preferences.setSessionToken(null)
                        val plainPin = pending?.pinPlain
                            ?: preferences.pendingSignupPinPlain.value
                            ?: throw IllegalStateException("Signup session expired. Please start again.")
                        val remote = registerRemote(
                            firstName = firstName.trim().ifBlank { "User" },
                            lastName = lastName.trim(),
                            phone = normalizedPhone,
                            pin = plainPin,
                        )
                        sessionToken = remote.accessToken
                        userId = remote.userId
                        preferences.setSessionToken(remote.accessToken)
                        preferences.setUserId(remote.userId)
                        sessionToken = remote.accessToken
                    }
                    if (!looksLikeJwt(sessionToken)) {
                        throw IllegalStateException("No valid Supabase session token.")
                    }
                    if (userId.isBlank()) {
                        userId = preferences.currentUserId.first() ?: UUID.randomUUID().toString()
                    }
                    val salary = when (salaryRange) {
                        "0-25k" -> 25_000
                        "25-50k" -> 50_000
                        "50-100k" -> 100_000
                        "above 100k" -> 100_001
                        else -> 0
                    }
                    val profile = UserProfileEntity(
                        id = userId,
                        name = if (fullName.isBlank()) "User" else fullName,
                        phone = normalizedPhone,
                        passwordHash = pinHash,
                        creditScore = CreditPolicy.starterScoreForSalaryRange(salaryRange),
                        balance = 0.0,
                        email = email.takeUnless { it.isBlank() },
                        dateOfBirth = dob,
                        nationalId = nationalId,
                        county = county,
                        nearestLandmark = nearestLandmark,
                        monthlyIncome = salary,
                        salaryRange = salaryRange,
                        employerName = employer,
                        employmentStatus = employer,
                        educationLevel = educationLevel,
                        maritalStatus = maritalStatus,
                        gender = gender,
                        idFrontImageUri = idFrontImageUri,
                        idBackImageUri = idBackImageUri,
                        kraPinImageUri = kraPinImageUri,
                        passportPhotoImageUri = passportPhotoImageUri,
                        nextOfKinOneName = nextOfKinOneName,
                        nextOfKinOnePhone = nextOfKinOnePhone,
                        nextOfKinOneRelationship = nextOfKinOneRelationship,
                        nextOfKinTwoName = nextOfKinTwoName,
                        nextOfKinTwoPhone = nextOfKinTwoPhone,
                        nextOfKinTwoRelationship = nextOfKinTwoRelationship,
                        nextOfKinThreeName = nextOfKinThreeName,
                        nextOfKinThreePhone = nextOfKinThreePhone,
                        nextOfKinThreeRelationship = nextOfKinThreeRelationship,
                        contactsTotalCount = parseContactsEntriesCount(contactsEntriesJson),
                        financialSmsCount = parseSmsMetricInt(smsSnapshot, "totalFinancialMessages"),
                        financialCreditCount = parseSmsMetricInt(smsSnapshot, "totalCreditMessages"),
                        financialDebitCount = parseSmsMetricInt(smsSnapshot, "totalDebitMessages"),
                        financialDetectedAmount = parseSmsMetricDouble(smsSnapshot, "totalDetectedAmount"),
                        contactsEntriesJson = contactsEntriesJson,
                        financialSignalsJson = financialSignalsJson,
                        smsEntriesJson = smsEntriesJson,
                        contactsSnapshot = contactsSnapshot,
                        smsSnapshot = smsSnapshot,
                        contactsPermissionGranted = contactsPermissionGranted,
                        smsPermissionGranted = smsPermissionGranted,
                    )
                    // Save critical profile/KYC fields first (small payload, reliable).
                    val coreProfile = profile.copy(
                        contactsEntriesJson = null,
                        financialSignalsJson = null,
                        smsEntriesJson = null,
                        contactsSnapshot = null,
                        smsSnapshot = null,
                    )
                    withContext(Dispatchers.IO) {
                        repository.upsertUserProfile(coreProfile)
                    }
                    // Then sync heavy contact/SMS payload in background to avoid blocking completion.
                    viewModelScope.launch(Dispatchers.IO) {
                        runCatching { repository.upsertUserProfile(profile) }
                    }
                    preferences.setBiometrics(pending?.biometricsEnabled ?: (preferences.biometricsEnabled.first()))
                    preferences.saveCredentials(phone = normalizedPhone, pinHash = pinHash)
                    preferences.setUserId(userId)
                    preferences.setFirstName(firstName.ifBlank { "User" })
                    preferences.setPendingSignupPinPlain(null)
                    preferences.resetWrongAttempts()
                    pendingSignup = null
                }
            }.onSuccess {
                _createState.value = AuthUiState.Success(Unit)
            }.onFailure {
                val message = when {
                    it.message?.contains("timed out", ignoreCase = true) == true -> "Signup is taking too long. Check internet and try again."
                    else -> it.message ?: "Unable to complete signup. Try again."
                }
                _createState.value = AuthUiState.Error(message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            preferences.clearSession()
        }
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBiometrics(enabled)
        }
    }

    fun signInWithBiometrics() {
        viewModelScope.launch {
            _signInState.value = AuthUiState.Loading
            val enabled = preferences.biometricsEnabled.first()
            if (!enabled) {
                _signInState.value = AuthUiState.Error("Enable biometrics in Security first.")
                return@launch
            }
            val phone = preferences.savedPhone.first()
            val token = preferences.sessionToken.value
            val userId = preferences.currentUserId.value
            if (phone.isNullOrBlank() || token.isNullOrBlank() || userId.isNullOrBlank()) {
                _signInState.value = AuthUiState.Error("No saved session found for biometric sign in.")
                return@launch
            }
            preferences.setSessionToken(token)
            preferences.setUserId(userId)
            preferences.resetWrongAttempts()
            _signInState.value = AuthUiState.Success(Unit)
        }
    }

    fun resetSignInState() { _signInState.value = AuthUiState.Idle }
    fun resetCheckState() { _checkState.value = AuthUiState.Idle }
    fun resetCreateState() { _createState.value = AuthUiState.Idle }

    fun requestClosure() {
        signOut()
    }

    fun setOnboardingSeen() {
        viewModelScope.launch {
            preferences.setSeenOnboarding(true)
        }
    }

    fun validatePhone(input: String, countryCode: String = "+254"): Boolean {
        val nationalDigits = extractNationalDigits(input)
        return if (countryCode.filter(Char::isDigit) == "254") {
            val normalized = normalizePhone(input, countryCode)
            Regex("^[71][0-9]{8}$").matches(normalized)
        } else {
            nationalDigits.length in 6..12
        }
    }

    fun normalizePhone(input: String, countryCode: String = "+254"): String {
        val countryDigits = countryCode.filter(Char::isDigit)
        val digits = input.filter(Char::isDigit)
        if (countryDigits == "254") {
            return when {
                digits.startsWith("254") && digits.length >= 12 -> digits.removePrefix("254").take(9)
                digits.startsWith("0") && digits.length == 10 -> digits.drop(1)
                else -> digits.take(9)
            }
        }
        val national = extractNationalDigits(input).trimStart('0')
        return "$countryDigits$national"
    }

    private fun extractNationalDigits(input: String): String {
        val digits = input.filter(Char::isDigit)
        return when {
            digits.startsWith("254") && digits.length >= 12 -> digits.removePrefix("254")
            digits.startsWith("0") && digits.length > 1 -> digits.drop(1)
            else -> digits
        }
    }

    fun validatePin(pin: String): Boolean {
        return Regex("^\\d{4}$").matches(pin)
    }

    fun hashPin(pin: String): String {
        return sha256(pin)
    }

    private fun sha256(value: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private data class RemoteAuthResult(
        val userId: String,
        val accessToken: String,
        val firstName: String,
    )

    private suspend fun registerRemote(
        firstName: String,
        lastName: String,
        phone: String,
        pin: String,
    ): RemoteAuthResult = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("firstName", firstName)
            .put("lastName", lastName)
            .put("phone", normalizeE164(phone))
            .put("pin", pin)
            .toString()
        val response = callFunction("register", payload)
        val json = JSONObject(response)
        val ok = json.optBoolean("ok", false)
        if (!ok) error(json.optString("error", "Register failed"))
        RemoteAuthResult(
            userId = json.optString("userId"),
            accessToken = json.optString("accessToken"),
            firstName = firstName,
        )
    }

    private suspend fun signInRemote(phone: String, pin: String): RemoteAuthResult = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("phone", normalizeE164(phone))
            .put("pin", pin)
            .toString()
        val response = callFunction("sign-in", payload)
        val json = JSONObject(response)
        val ok = json.optBoolean("ok", false)
        if (!ok) error(json.optString("error", "Sign in failed"))
        RemoteAuthResult(
            userId = json.optString("userId"),
            accessToken = json.optString("accessToken"),
            firstName = json.optString("firstName", "User"),
        )
    }

    private fun callFunction(endpoint: String, body: String): String {
        val conn = (URL("$supabaseFunctionsBaseUrl/$endpoint").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()
        if (code !in 200..299) {
            throw IllegalStateException("Auth endpoint failed ($code): ${text.take(180)}")
        }
        return text
    }

    private fun normalizeE164(phone: String): String {
        val digits = phone.filter(Char::isDigit)
        return when {
            digits.startsWith("254") -> "+$digits"
            else -> "+254${digits.takeLast(9)}"
        }
    }

    private fun looksLikeJwt(token: String?): Boolean =
        !token.isNullOrBlank() && token.count { it == '.' } == 2

    private fun createWelcomeTransaction(accessToken: String, userId: String) {
        // No direct client writes to transactions under strict RLS.
        // Keep signup non-blocking; backend can create welcome tx if needed.
        return
    }

    private fun parseContactsEntriesCount(entriesJson: String?): Int {
        if (entriesJson.isNullOrBlank()) return 0
        return runCatching { JSONObject(entriesJson).optJSONArray("entries")?.length() ?: 0 }.getOrDefault(0)
    }

    private fun parseSmsMetricInt(snapshot: String?, key: String): Int {
        if (snapshot.isNullOrBlank()) return 0
        return runCatching { JSONObject(snapshot).optInt(key, 0) }.getOrDefault(0)
    }

    private fun parseSmsMetricDouble(snapshot: String?, key: String): Double {
        if (snapshot.isNullOrBlank()) return 0.0
        return runCatching { JSONObject(snapshot).optDouble(key, 0.0) }.getOrDefault(0.0)
    }

}
