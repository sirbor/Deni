package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.data.local.entity.UserProfileEntity
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.util.AuthEvents
import com.loki.deni.util.core.CreditPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UserProfile(
    val id: Int,
    val fullName: String,
    val phone: String,
    val email: String,
    val nationalId: String,
    val dateOfBirth: String,
    val county: String,
    val nearestLandmark: String,
    val monthlyIncome: Int,
    val salaryRange: String,
    val employerName: String,
    val employmentStatus: String,
    val educationLevel: String,
    val maritalStatus: String,
    val gender: String,
    val idFrontImageUri: String,
    val idBackImageUri: String,
    val kraPinImageUri: String,
    val passportPhotoImageUri: String,
    val nextOfKinOneName: String,
    val nextOfKinOnePhone: String,
    val nextOfKinOneRelationship: String,
    val nextOfKinTwoName: String,
    val nextOfKinTwoPhone: String,
    val nextOfKinTwoRelationship: String,
    val nextOfKinThreeName: String,
    val nextOfKinThreePhone: String,
    val nextOfKinThreeRelationship: String,
    val memberSince: String,
    val creditScore: Int,
    val creditLimit: Int,
    val loansTaken: Int,
    val repayRate: Int,
)

data class DebugProfileDiagnostics(
    val currentUserId: String = "",
    val hasSessionToken: Boolean = false,
    val savedPhone: String = "",
    val email: String = "",
    val contactsPermissionGranted: Boolean = false,
    val smsPermissionGranted: Boolean = false,
    val contactsCount: Int = 0,
    val financialSmsCount: Int = 0,
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferences: UserPreferencesStore,
    private val repository: DeniRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isNotificationsOn = MutableStateFlow(true)
    val isNotificationsOn: StateFlow<Boolean> = _isNotificationsOn.asStateFlow()
    private val _isBiometricsOn = MutableStateFlow(false)
    val isBiometricsOn: StateFlow<Boolean> = _isBiometricsOn.asStateFlow()

    private val _isEditingName = MutableStateFlow(false)
    val isEditingName: StateFlow<Boolean> = _isEditingName.asStateFlow()

    private val _editNameValue = MutableStateFlow("")
    val editNameValue: StateFlow<String> = _editNameValue.asStateFlow()
    private val _debugDiagnostics = MutableStateFlow(DebugProfileDiagnostics())
    val debugDiagnostics: StateFlow<DebugProfileDiagnostics> = _debugDiagnostics.asStateFlow()

    init {
        observePreferences()
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val userId = preferences.currentUserId.first()
            if (userId.isNullOrBlank()) {
                _uiState.value = ProfileUiState.Error("No active user session")
                AuthEvents.emitSessionExpired()
                return@launch
            }
            var usedLocalFallback = false
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
                repository.getUserProfile(userId).first() ?: run {
                    usedLocalFallback = true
                    fallback
                }
            }
            val loans = repository.getLoans(userId).first()
            val paid = loans.count { it.isPaid }
            val repayRate = if (loans.isEmpty()) 0 else ((paid.toFloat() / loans.size) * 100).toInt()
            val approvedLimit = CreditPolicy.resolveApprovedLimit(
                salaryRange = user.salaryRange,
                creditScore = user.creditScore,
                paidLoansCount = paid,
            )
            val memberSinceTs = loans.minOfOrNull { it.timestamp } ?: System.currentTimeMillis()
            val profile = UserProfile(
                id = 1,
                fullName = user.name,
                phone = user.phone,
                email = user.email ?: "",
                nationalId = user.nationalId ?: "",
                dateOfBirth = user.dateOfBirth ?: "",
                county = user.county ?: "",
                nearestLandmark = user.nearestLandmark ?: "",
                monthlyIncome = user.monthlyIncome ?: 0,
                salaryRange = user.salaryRange ?: "",
                employerName = user.employerName ?: "",
                employmentStatus = user.employmentStatus ?: "",
                educationLevel = user.educationLevel ?: "",
                maritalStatus = user.maritalStatus ?: "",
                gender = user.gender ?: "",
                idFrontImageUri = user.idFrontImageUri ?: "",
                idBackImageUri = user.idBackImageUri ?: "",
                kraPinImageUri = user.kraPinImageUri ?: "",
                passportPhotoImageUri = user.passportPhotoImageUri ?: "",
                nextOfKinOneName = user.nextOfKinOneName ?: "",
                nextOfKinOnePhone = user.nextOfKinOnePhone ?: "",
                nextOfKinOneRelationship = user.nextOfKinOneRelationship ?: "",
                nextOfKinTwoName = user.nextOfKinTwoName ?: "",
                nextOfKinTwoPhone = user.nextOfKinTwoPhone ?: "",
                nextOfKinTwoRelationship = user.nextOfKinTwoRelationship ?: "",
                nextOfKinThreeName = user.nextOfKinThreeName ?: "",
                nextOfKinThreePhone = user.nextOfKinThreePhone ?: "",
                nextOfKinThreeRelationship = user.nextOfKinThreeRelationship ?: "",
                memberSince = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date(memberSinceTs)),
                creditScore = user.creditScore.coerceIn(300, 850),
                creditLimit = approvedLimit,
                loansTaken = loans.size,
                repayRate = repayRate,
            )
            _uiState.value = ProfileUiState.Success(profile)
            _editNameValue.value = profile.fullName
            _debugDiagnostics.value = DebugProfileDiagnostics(
                currentUserId = userId,
                hasSessionToken = !preferences.sessionToken.value.isNullOrBlank(),
                savedPhone = preferences.savedPhone.value.orEmpty(),
                email = user.email.orEmpty(),
                contactsPermissionGranted = user.contactsPermissionGranted,
                smsPermissionGranted = user.smsPermissionGranted,
                contactsCount = user.contactsSnapshot
                    ?.split(";;")
                    ?.count { it.isNotBlank() }
                    ?: 0,
                financialSmsCount = user.smsSnapshot
                    ?.split(";;")
                    ?.count { it.isNotBlank() }
                    ?: 0,
            )
            if (usedLocalFallback) {
                // Retry remote write without blocking UI so profile eventually appears server-side.
                viewModelScope.launch {
                    runCatching { repository.upsertUserProfile(user) }
                }
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch { preferences.setDarkMode(!_isDarkMode.value) }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkMode(enabled) }
    }

    fun toggleNotifications() {
        viewModelScope.launch { preferences.setReminders(!_isNotificationsOn.value) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setReminders(enabled) }
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setBiometrics(enabled) }
    }

    fun startEditName() {
        _isEditingName.value = true
        val current = (_uiState.value as? ProfileUiState.Success)?.profile
        if (current != null) {
            _editNameValue.value = current.fullName
        }
    }

    fun updateNameValue(name: String) {
        _editNameValue.value = name
    }

    fun saveNameEdit() {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Success)?.profile ?: return@launch
            val userId = preferences.currentUserId.first()
            if (userId.isNullOrBlank()) {
                AuthEvents.emitSessionExpired()
                return@launch
            }
            val user = repository.getUserProfile(userId).first() ?: return@launch
            val trimmed = _editNameValue.value.trim()
            if (trimmed.isEmpty()) {
                _uiState.value = ProfileUiState.Error("Name cannot be empty")
                _uiState.value = ProfileUiState.Success(current)
                return@launch
            }
            repository.upsertUserProfile(user.copy(name = trimmed))
            _uiState.value = ProfileUiState.Success(current.copy(fullName = trimmed))
            _isEditingName.value = false
        }
    }

    fun cancelNameEdit() {
        val current = (_uiState.value as? ProfileUiState.Success)?.profile ?: return
        _editNameValue.value = current.fullName
        _isEditingName.value = false
    }

    fun maskPhone(phone: String): String {
        if (phone.length < 7) return phone
        return "${phone.take(4)}***${phone.takeLast(3)}"
    }

    fun maskId(id: String): String {
        if (id.length < 4) return id
        return "${id.first()}XXXX${id.takeLast(3)}"
    }

    fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "KE"))
        return "KES ${formatter.format(amount)}"
    }

    suspend fun updateProfile(
        name: String,
        photoUri: String?,
        email: String,
        county: String,
        employerName: String,
        monthlyIncome: Int,
    ) {
        val current = (_uiState.value as? ProfileUiState.Success)?.profile ?: return
        val userId = preferences.currentUserId.first()
        if (userId.isNullOrBlank()) {
            AuthEvents.emitSessionExpired()
            return
        }
        val user = repository.getUserProfile(userId).first() ?: return
        repository.upsertUserProfile(
            user.copy(
                name = name,
                email = email,
                county = county,
                employerName = employerName,
                monthlyIncome = monthlyIncome,
            ),
        )
        val updated = current.copy(
            fullName = name,
            email = email,
            county = county,
            employerName = employerName,
            monthlyIncome = monthlyIncome,
        )
        _uiState.value = ProfileUiState.Success(updated)
    }

    suspend fun toggleReminders(enabled: Boolean) {
        preferences.setReminders(enabled)
    }

    suspend fun startKyc(step: String) {
        // Placeholder for KYC backend action.
    }

    fun toggleRemindersPersist(enabled: Boolean) {
        viewModelScope.launch { preferences.setReminders(enabled) }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                preferences.darkMode,
                preferences.remindersEnabled,
                preferences.biometricsEnabled,
            ) { darkMode, reminders, biometrics ->
                Triple(darkMode, reminders, biometrics)
            }.collect { (darkMode, reminders, biometrics) ->
                _isDarkMode.value = darkMode
                _isNotificationsOn.value = reminders
                _isBiometricsOn.value = biometrics
            }
        }
    }
}
