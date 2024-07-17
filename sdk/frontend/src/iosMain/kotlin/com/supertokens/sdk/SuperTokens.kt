package com.supertokens.sdk

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

@OptIn(ExperimentalSettingsImplementation::class)
internal actual fun getDefaultSettings(): Settings {
  return KeychainSettings("SuperTokens")
}
