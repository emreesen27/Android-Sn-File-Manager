package com.sn.snfilemanager.feature.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.util.StringValue

object SettingsUtils {
    const val SN_THEME_MODE = "sn.theme.mode"
    const val SN_HIDDEN_FILE = "sn.hidden.file"

    val SYSTEM = StringValue.StringResource(R.string.system)
    private val DARK = StringValue.StringResource(R.string.dark)
    private val LIGHT = StringValue.StringResource(R.string.light)

    fun resolveThemeMode(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(SN_THEME_MODE, SYSTEM.asString(context)).toString()
    }

    fun resolveHiddenFiles(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(SN_HIDDEN_FILE, false)
    }

    fun changeTheme(
        context: Context,
        theme: String,
    ) {
        when (theme) {
            SYSTEM.asString(context) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            DARK.asString(context) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            LIGHT.asString(context) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}
