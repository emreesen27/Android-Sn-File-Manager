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

    fun updateDirectoryList(path: String) {
        if (!directoryList.contains(path))
            directoryList.add(path)
    }

    fun getFilesList(path: String): List<File> {
        val directory = File(path)
        return directory.listFiles()?.toList() ?: emptyList()
    }

    fun getDirectoryList() = directoryList

}