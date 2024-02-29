package com.sn.snfilemanager.job.file

import com.sn.snfilemanager.core.base.BaseJob
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobType
import java.nio.file.Files
import java.nio.file.Path

class CreateDirectory(
    private val targetPath: Path,
    private val completed: JobCompletedCallback,
) : BaseJob() {
    private val directory: MutableList<Path> = mutableListOf()

    override fun run() {
        val dir = Files.createDirectories(targetPath)
        directory.add(dir)
    }

    override fun onCompleted() {
        completed.jobOnCompleted(JobType.CREATE, directory)
    }
}
