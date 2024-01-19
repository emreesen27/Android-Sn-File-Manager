package com.sn.snfilemanager.providers.fileprovider

import com.sn.filetaskpv.FileOperationCallback
import com.sn.filetaskpv.FileTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.inject.Inject

class FileTaskProvider @Inject constructor(private val fileTask: FileTask) {
    suspend fun moveFilesAndDirectories(
        sourcePaths: List<Path>,
        destinationPath: Path,
        callback: FileOperationCallback,
        isCopy: Boolean
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        fileTask.moveFilesAndDirectories(sourcePaths, destinationPath, callback, isCopy)
    }

    suspend fun deleteFilesAndDirectories(sourcePaths: List<Path>): Result<List<String>> =
        withContext(Dispatchers.IO) {
            val result = fileTask.deleteFilesAndDirectories(sourcePaths)
            Result.success(result)
        }
}