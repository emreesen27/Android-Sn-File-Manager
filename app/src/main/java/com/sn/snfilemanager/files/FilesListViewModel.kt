package com.sn.snfilemanager.files

import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.ui.StorageType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FilesListViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider
) : ViewModel() {

    private val directoryList: MutableList<String> = mutableListOf()

    fun getStoragePath(storageType: StorageType): String = if (storageType == StorageType.INTERNAL)
        filePathProvider.internalStorageRootPath
    else
        filePathProvider.externalStorageRootPath

    fun updateDirectoryList(directoryPath: String) {
        if (!directoryList.contains(directoryPath))
            directoryList.add(directoryPath)
    }

    fun getFilesList(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        return directory.listFiles()?.toList() ?: emptyList()
    }

    fun getDirectoryList() = directoryList

}