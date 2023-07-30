package com.sn.snfilemanager.feature.home

import android.os.StatFs
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.snfilemanager.core.extensions.toHumanReadableByteCount
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider
) : ViewModel() {

    private val availableStorageMutableLiveData: MutableLiveData<String> = MutableLiveData()
    val availableStorageLiveData: LiveData<String> = availableStorageMutableLiveData

    private val availableExternalStorageMutableLiveData: MutableLiveData<String> = MutableLiveData()
    val availableExternalStorageLiveData: LiveData<String> = availableExternalStorageMutableLiveData

    init {
        getFreeInternalMemory()
        getFreeExternalMemory()
    }

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
                getFreeMemory(filePathProvider.externalSdCardDirectories.first()).toHumanReadableByteCount()
            }
            availableExternalStorageMutableLiveData.value = memory
        }
    }

    private fun getFreeMemory(path: File): Long {
        val stats = StatFs(path.absolutePath)
        return stats.availableBlocksLong * stats.blockSizeLong
    }

}