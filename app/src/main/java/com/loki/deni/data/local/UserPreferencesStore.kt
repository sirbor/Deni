package com.loki.deni.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class UserPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "deni_secure_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("deni_prefs") },
    )

    private val _sessionToken = MutableStateFlow(encryptedPrefs.getString(SecureKeys.SESSION_TOKEN, null))
    val sessionToken: StateFlow<String?> = _sessionToken.asStateFlow()
    val isLoggedIn: Flow<Boolean> = sessionToken.map { !it.isNullOrBlank() }
    val darkMode: Flow<Boolean> = dataStore.data.map { it[DataStoreKeys.DARK_MODE] ?: false }
    val remindersEnabled: Flow<Boolean> = dataStore.data.map { it[DataStoreKeys.REMINDERS] ?: true }
    val biometricsEnabled: Flow<Boolean> = dataStore.data.map { it[DataStoreKeys.BIOMETRICS] ?: false }
    private val _savedPhone = MutableStateFlow(encryptedPrefs.getString(SecureKeys.USER_PHONE, null))
    val savedPhone: StateFlow<String?> = _savedPhone.asStateFlow()
    private val _savedPin = MutableStateFlow(encryptedPrefs.getString(SecureKeys.PIN_HASH, null))
    val savedPin: StateFlow<String?> = _savedPin.asStateFlow()
    private val _firstName = MutableStateFlow(encryptedPrefs.getString(SecureKeys.FIRST_NAME, null))
    val firstName: StateFlow<String?> = _firstName.asStateFlow()
    private val _currentUserId = MutableStateFlow(encryptedPrefs.getString(SecureKeys.USER_ID, null))
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
    private val _pendingSignupPinPlain = MutableStateFlow(encryptedPrefs.getString(SecureKeys.PENDING_SIGNUP_PIN_PLAIN, null))
    val pendingSignupPinPlain: StateFlow<String?> = _pendingSignupPinPlain.asStateFlow()
    val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { it[KEY_SEEN_ONBOARDING] ?: false }
    val wrongPinAttempts: Flow<Int> = dataStore.data.map { it[DataStoreKeys.WRONG_PIN_ATTEMPTS] ?: 0 }
    val supportTicketsRaw: Flow<String> = dataStore.data.map { it[DataStoreKeys.SUPPORT_TICKETS_RAW] ?: "" }

    suspend fun setSessionToken(token: String?) {
        encryptedPrefs.edit().apply {
            if (token.isNullOrBlank()) remove(SecureKeys.SESSION_TOKEN) else putString(SecureKeys.SESSION_TOKEN, token)
        }.apply()
        _sessionToken.value = token
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DataStoreKeys.DARK_MODE] = enabled }
    }

    suspend fun setReminders(enabled: Boolean) {
        dataStore.edit { it[DataStoreKeys.REMINDERS] = enabled }
    }

    suspend fun setBiometrics(enabled: Boolean) {
        dataStore.edit { it[DataStoreKeys.BIOMETRICS] = enabled }
    }

    suspend fun saveCredentials(phone: String, pinHash: String) {
        encryptedPrefs.edit()
            .putString(SecureKeys.USER_PHONE, phone)
            .putString(SecureKeys.PIN_HASH, pinHash)
            .apply()
        _savedPhone.value = phone
        _savedPin.value = pinHash
    }

    suspend fun setFirstName(name: String?) {
        encryptedPrefs.edit().apply {
            if (name.isNullOrBlank()) remove(SecureKeys.FIRST_NAME) else putString(SecureKeys.FIRST_NAME, name)
        }.apply()
        _firstName.value = name
    }

    suspend fun setUserId(userId: String?) {
        encryptedPrefs.edit().apply {
            if (userId.isNullOrBlank()) remove(SecureKeys.USER_ID) else putString(SecureKeys.USER_ID, userId)
        }.apply()
        _currentUserId.value = userId
    }

    suspend fun setPendingSignupPinPlain(pin: String?) {
        encryptedPrefs.edit().apply {
            if (pin.isNullOrBlank()) remove(SecureKeys.PENDING_SIGNUP_PIN_PLAIN) else putString(SecureKeys.PENDING_SIGNUP_PIN_PLAIN, pin)
        }.apply()
        _pendingSignupPinPlain.value = pin
    }

    suspend fun clearSession() {
        encryptedPrefs.edit().remove(SecureKeys.SESSION_TOKEN).apply()
        _sessionToken.value = null
        setPendingSignupPinPlain(null)
        dataStore.edit { prefs -> prefs[DataStoreKeys.WRONG_PIN_ATTEMPTS] = 0 }
    }

    suspend fun incrementWrongAttempts() {
        dataStore.edit { prefs ->
            val current = prefs[DataStoreKeys.WRONG_PIN_ATTEMPTS] ?: 0
            prefs[DataStoreKeys.WRONG_PIN_ATTEMPTS] = current + 1
        }
    }

    suspend fun resetWrongAttempts() {
        dataStore.edit { prefs ->
            prefs[DataStoreKeys.WRONG_PIN_ATTEMPTS] = 0
        }
    }

    suspend fun setSeenOnboarding(seen: Boolean) {
        dataStore.edit { it[KEY_SEEN_ONBOARDING] = seen }
    }

    suspend fun setSupportTicketsRaw(raw: String) {
        dataStore.edit { it[DataStoreKeys.SUPPORT_TICKETS_RAW] = raw }
    }

    companion object {
        private val KEY_SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
    }
}

object DataStoreKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val REMINDERS = booleanPreferencesKey("reminders")
    val BIOMETRICS = booleanPreferencesKey("biometrics_enabled")
    val WRONG_PIN_ATTEMPTS = androidx.datastore.preferences.core.intPreferencesKey("wrong_pin_attempts")
    val SUPPORT_TICKETS_RAW = androidx.datastore.preferences.core.stringPreferencesKey("support_tickets_raw")
}

private object SecureKeys {
    const val SESSION_TOKEN = "session_token"
    const val PIN_HASH = "pin_hash"
    const val USER_PHONE = "user_phone"
    const val FIRST_NAME = "first_name"
    const val USER_ID = "user_id"
    const val PENDING_SIGNUP_PIN_PLAIN = "pending_signup_pin_plain"
}
