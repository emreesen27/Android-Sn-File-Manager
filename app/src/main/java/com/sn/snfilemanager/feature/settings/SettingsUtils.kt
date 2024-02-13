package com.sn.snfilemanager.feature.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

object SettingsUtils {
    const val SN_THEME_MODE = "sn.theme.mode"
    const val SN_HIDDEN_FILE = "sn.hidden.file"

    const val SYSTEM = "System"
    private const val DARK = "Dark"
    private const val LIGHT = "Light"

    fun resolveThemeMode(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(SN_THEME_MODE, SYSTEM).toString()
    }

    fun resolveHiddenFiles(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(SN_HIDDEN_FILE, false)
    }

    fun changeTheme(theme: String) {
        when (theme) {
            SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}
