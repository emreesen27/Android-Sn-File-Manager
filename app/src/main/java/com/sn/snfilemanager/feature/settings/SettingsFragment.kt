package com.sn.snfilemanager.feature.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.util.Config

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var mListenerOptions: OnSharedPreferenceChangeListener

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themeListPreference: ListPreference? = findPreference(SettingsUtils.SN_THEME_MODE)
        val hiddenFilePreference: SwitchPreferenceCompat? =
            findPreference(SettingsUtils.SN_HIDDEN_FILE)

        mListenerOptions =
            OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String? ->
                when (key) {
                    SettingsUtils.SN_THEME_MODE -> {
                        SettingsUtils.changeTheme(
                            requireContext(),
                            themeListPreference?.value ?: SettingsUtils.SYSTEM.asString(context),
                        )
                    }

                    SettingsUtils.SN_HIDDEN_FILE -> {
                        Config.hiddenFile = hiddenFilePreference?.isChecked ?: false
                    }
                }
            }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
            mListenerOptions,
        )
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(
            mListenerOptions,
        )
    }
}
