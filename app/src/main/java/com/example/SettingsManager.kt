package com.example

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "replify_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var defaultTone: String
        get() = prefs.getString("default_tone", "Friendly 👋") ?: "Friendly 👋"
        set(value) = prefs.edit().putString("default_tone", value).apply()

    var autoSend: Boolean
        get() = prefs.getBoolean("auto_send", false)
        set(value) = prefs.edit().putBoolean("auto_send", value).apply()

    var showDetectedMessage: Boolean
        get() = prefs.getBoolean("show_detected", true)
        set(value) = prefs.edit().putBoolean("show_detected", value).apply()

    var isOnboardingComplete: Boolean
        get() = prefs.getBoolean("onboarding_complete", false)
        set(value) = prefs.edit().putBoolean("onboarding_complete", value).apply()

    fun clearSettings() {
        prefs.edit().clear().apply()
    }
}
