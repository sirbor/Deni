package com.loki.deni.util

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import android.os.CancellationSignal
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object BiometricAuth {
    fun isAvailable(context: Context): Boolean {
        val result = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK,
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        context: Context,
        title: String,
        subtitle: String,
        negativeText: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onError("Biometric authentication is unavailable.")
            return
        }
        if (activity is FragmentActivity) {
            authenticateWithAndroidX(
                activity = activity,
                title = title,
                subtitle = subtitle,
                negativeText = negativeText,
                onSuccess = onSuccess,
                onError = onError,
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticateWithFramework(
                activity = activity,
                title = title,
                subtitle = subtitle,
                negativeText = negativeText,
                onSuccess = onSuccess,
                onError = onError,
            )
        } else {
            onError("Biometric authentication is unavailable on this Android version.")
        }
    }
}

private fun authenticateWithAndroidX(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    negativeText: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val cryptoObject = buildCryptoObject()
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                    errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_CANCELED
                ) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                onError("Fingerprint not recognized. Try again.")
            }
        },
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeText)
        .build()

    if (cryptoObject != null) {
        prompt.authenticate(promptInfo, cryptoObject)
    } else {
        prompt.authenticate(promptInfo)
    }
}

@androidx.annotation.RequiresApi(Build.VERSION_CODES.P)
private fun authenticateWithFramework(
    activity: Activity,
    title: String,
    subtitle: String,
    negativeText: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = android.hardware.biometrics.BiometricPrompt.Builder(activity)
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButton(negativeText, executor) { _, _ -> }
        .build()

    val cancellationSignal = CancellationSignal()
    prompt.authenticate(
        cancellationSignal,
        executor,
        object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult?) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                if (errorCode != android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED &&
                    errorCode != android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_CANCELED
                ) {
                    onError(errString?.toString() ?: "Authentication failed.")
                }
            }

            override fun onAuthenticationFailed() {
                onError("Fingerprint not recognized. Try again.")
            }
        },
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val BIOMETRIC_KEYSTORE = "AndroidKeyStore"
private const val BIOMETRIC_KEY_ALIAS = "deni_biometric_key_v1"
private const val BIOMETRIC_TRANSFORMATION = "AES/GCM/NoPadding"

private fun buildCryptoObject(): BiometricPrompt.CryptoObject? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
    return runCatching {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(BIOMETRIC_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        BiometricPrompt.CryptoObject(cipher)
    }.getOrNull()
}

@androidx.annotation.RequiresApi(Build.VERSION_CODES.M)
private fun getOrCreateSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance(BIOMETRIC_KEYSTORE).apply { load(null) }
    val existing = (keyStore.getKey(BIOMETRIC_KEY_ALIAS, null) as? SecretKey)
    if (existing != null) return existing

    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, BIOMETRIC_KEYSTORE)
    val spec = KeyGenParameterSpec.Builder(
        BIOMETRIC_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setUserAuthenticationRequired(true)
        .setInvalidatedByBiometricEnrollment(true)
        .build()
    keyGenerator.init(spec)
    return keyGenerator.generateKey()
}
