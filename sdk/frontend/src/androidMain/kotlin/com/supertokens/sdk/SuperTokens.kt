package com.supertokens.sdk

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.startup.Initializer
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

internal actual fun getDefaultSettings(): Settings {
  return settings
}

internal lateinit var settings: Settings

internal class DefaultSettingsInitializer : Initializer<Settings> {
  override fun create(context: Context): Settings {
    return SharedPreferencesSettings(
            EncryptedSharedPreferences.create(
                context,
                "SuperTokens",
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM),
            false)
        .also { settings = it }
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    // No dependencies on other libraries.
    return emptyList()
  }
}
