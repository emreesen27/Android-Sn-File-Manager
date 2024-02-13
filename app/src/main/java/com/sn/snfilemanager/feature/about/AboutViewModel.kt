package com.sn.snfilemanager.feature.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.BuildConfig

class AboutViewModel : ViewModel() {
    private val _versionLiveData: MutableLiveData<String> = MutableLiveData()
    val versionLiveData: LiveData<String> = _versionLiveData

    init {
        getVersion()
    }

    private fun getVersion() {
        _versionLiveData.value = BuildConfig.VERSION_NAME
    }
}
