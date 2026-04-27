package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.loki.deni.data.local.UserPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    suspend fun verifyPin(pin: String): Boolean {
        val savedHash = preferences.savedPin.first() ?: return false
        return sha256(pin) == savedHash
    }

    private fun sha256(value: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
