package com.sn.snfilemanager.pathpicker

import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PathPickerViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider
) : ViewModel() {

    private val directoryList: MutableList<String> = mutableListOf()
    var currentPath: String? = null

    fun updateDirectoryList(path: String) {
        if (!directoryList.contains(path))
            directoryList.add(path)
    }

    fun getDirectoryList() = directoryList

    fun getStoragePath(rootPath: RootPath): String = when (rootPath) {
        RootPath.INTERNAL -> filePathProvider.internalStorageRootPath
        RootPath.EXTERNAL -> filePathProvider.externalStorageRootPath
        else -> filePathProvider.downloadDirectoryPath
    }

    fun getDirectoryList(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        return directory.listFiles()?.filter { it.isDirectory }?.toList() ?: emptyList()
    }

}