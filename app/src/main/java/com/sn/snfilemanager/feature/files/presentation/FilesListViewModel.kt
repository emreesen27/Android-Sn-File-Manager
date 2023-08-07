package com.sn.snfilemanager.feature.files.presentation

import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import com.sn.snfilemanager.core.util.RootPath
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FilesListViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider
) : ViewModel() {

    private val directoryList: MutableList<String> = mutableListOf()

    fun getStoragePath(rootPath: RootPath): String = when (rootPath) {
        RootPath.INTERNAL -> filePathProvider.internalStorageRootPath
        RootPath.EXTERNAL -> filePathProvider.externalStorageRootPath
        else -> filePathProvider.downloadDirectoryPath
    }

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