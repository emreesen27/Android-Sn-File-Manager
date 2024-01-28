package com.sn.snfilemanager.feature.home

import android.os.StatFs
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.snfilemanager.core.extensions.toHumanReadableByteCount
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider,
    private val preferences: MySharedPreferences
) : ViewModel() {

    var notificationRuntimeRequested: Boolean = false
    private val availableStorageMutableLiveData: MutableLiveData<String> = MutableLiveData()
    val availableStorageLiveData: LiveData<String> = availableStorageMutableLiveData

    private val availableExternalStorageMutableLiveData: MutableLiveData<String?> =
        MutableLiveData()
    val availableExternalStorageLiveData: LiveData<String?> =
        availableExternalStorageMutableLiveData

    init {
        getFreeInternalMemory()
        getFreeExternalMemory()
    }

    fun hasStorageRequestedPermissionBefore() = preferences.getBoolean(PrefsTag.PERMISSION_STORAGE)
    fun setStoragePermissionRequested() = preferences.putBoolean(PrefsTag.PERMISSION_STORAGE, true)
    fun hasNotificationRequestedPermissionBefore() =
        preferences.getBoolean(PrefsTag.PERMISSION_NOTIFICATION)

    fun setNotificationPermissionRequested() =
        preferences.putBoolean(PrefsTag.PERMISSION_NOTIFICATION, true)


    private fun getFreeInternalMemory() {
        viewModelScope.launch {
            val memory = withContext(Dispatchers.IO) {
                getFreeMemory(filePathProvider.internalStorageDirectory).toHumanReadableByteCount()
            }
            availableStorageMutableLiveData.value = memory
        }
    }

    private fun getFreeExternalMemory() {
        viewModelScope.launch {
            val memory = withContext(Dispatchers.IO) {
                filePathProvider.externalSdCardDirectories.firstOrNull()?.let {
                    getFreeMemory(it).toHumanReadableByteCount()
                }
            }
            availableExternalStorageMutableLiveData.value = memory
        }
    }

    private fun getFreeMemory(path: File): Long {
        val stats = StatFs(path.absolutePath)
        return stats.availableBlocksLong * stats.blockSizeLong
    }

}