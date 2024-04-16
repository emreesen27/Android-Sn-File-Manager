package com.sn.snfilemanager.job.file

import com.sn.snfilemanager.core.base.BaseJob
import com.sn.snfilemanager.core.extensions.scanFile
import com.sn.snfilemanager.core.extensions.toFormattedDate
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobType
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime

class RenameFileJob(
    private val file: FileModel,
    private val newName: String,
    private val callback: JobCompletedCallback,
) : BaseJob() {
    private lateinit var newFile: FileModel

    override fun run() {
        val sourcePath = Paths.get(file.absolutePath)
        val targetPath = sourcePath.resolveSibling(newName)
        val newPath = Files.move(sourcePath, targetPath)

        val lastModifiedTime = FileTime.fromMillis(System.currentTimeMillis())
        Files.setLastModifiedTime(newPath, lastModifiedTime)

        newFile =
            file.copy(
                name = newPath.fileName.toString(),
                absolutePath = newPath.toAbsolutePath().toString(),
                lastModified = lastModifiedTime.toMillis().toFormattedDate(),
            )
    }

    override fun onCompleted() {
        scanFile(listOf(newFile.absolutePath))
        callback.jobOnCompleted(JobType.RENAME, listOf(newFile))
    }
}
