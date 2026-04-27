package com.loki.deni.util.core

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object DatabaseSecurity {
    private const val PREF_FILE = "deni_secure_db_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase_b64"

    fun passphrase(context: Context): ByteArray {
        val prefs = securePrefs(context)
        val stored = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (!stored.isNullOrBlank()) {
            return Base64.decode(stored, Base64.NO_WRAP)
        }
        val raw = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        val passphraseText = Base64.encodeToString(raw, Base64.NO_WRAP)
        prefs.edit().putString(
            KEY_DB_PASSPHRASE,
            passphraseText,
        ).apply()
        return raw
    }

    private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREF_FILE,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}
