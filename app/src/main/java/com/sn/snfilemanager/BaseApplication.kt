package com.sn.snfilemanager

import android.app.Application
import com.sn.snfilemanager.feature.settings.SettingsUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val theme = SettingsUtils.resolveThemeMode(this)
        SettingsUtils.changeTheme(theme)
    }
}
