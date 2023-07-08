package com.supertokens.sdk

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun getDefaultSettings(): Settings {
    return PreferencesSettings(
        Preferences.userRoot()
    )
}