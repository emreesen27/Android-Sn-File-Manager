package com.sn.snfilemanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val mySharedPreferences: MySharedPreferences,
    ) : ViewModel() {
        private val _firstRunLiveData: MutableLiveData<Event<Boolean>> = MutableLiveData()
        val firstRunLiveData: LiveData<Event<Boolean>> = _firstRunLiveData

        init {
            checkFirsRun()
        }

        private fun checkFirsRun() {
            val firstRun: Boolean = mySharedPreferences.getBoolean(PrefsTag.FIRST_RUN)
            _firstRunLiveData.postValue(Event(firstRun))
        }
    }
