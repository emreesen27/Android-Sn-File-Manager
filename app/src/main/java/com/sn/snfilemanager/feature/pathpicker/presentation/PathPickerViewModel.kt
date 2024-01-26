package com.sn.snfilemanager.feature.pathpicker.presentation

import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.io.path.isDirectory
import kotlin.streams.toList

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

    fun getDirectoryList(directoryPath: String): List<Path> {
        val directory = Paths.get(directoryPath)
        return Files.list(directory).filter { it.isDirectory() }.toList()
    }

}