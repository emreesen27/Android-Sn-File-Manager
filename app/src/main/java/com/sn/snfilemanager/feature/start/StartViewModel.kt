package com.sn.snfilemanager.feature.start

import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartViewModel
    @Inject
    constructor(private val mySharedPreferences: MySharedPreferences) :
    ViewModel() {
        fun saveFirsRun() {
            mySharedPreferences.putBoolean(PrefsTag.FIRST_RUN, true)
        }
    }
