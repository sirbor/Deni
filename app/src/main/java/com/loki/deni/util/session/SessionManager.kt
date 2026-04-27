package com.loki.deni.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("deni_prefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun clearSession() {
        prefs.edit().remove("user_id").apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }
}
