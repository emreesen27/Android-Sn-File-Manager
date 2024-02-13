package com.sn.snfilemanager.feature.settings

import android.content.Context
import android.util.AttributeSet
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.appbar.MaterialToolbar
import com.sn.snfilemanager.R

class SettingsToolbar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : Preference(context, attrs, defStyleAttr) {
        override fun onBindViewHolder(holder: PreferenceViewHolder) {
            super.onBindViewHolder(holder)

            val toolbar = holder.findViewById(R.id.toolbar_settings) as MaterialToolbar
            toolbar.setNavigationOnClickListener {
                toolbar.findNavController().popBackStack()
            }
        }
    }
