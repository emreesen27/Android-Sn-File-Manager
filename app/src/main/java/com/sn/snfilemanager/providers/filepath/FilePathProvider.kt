package com.sn.snfilemanager.providers.filepath

import android.app.Application
import android.os.Environment
import java.io.File
import javax.inject.Inject

class FilePathProvider
    @Inject
    constructor(application: Application) {
        private val filesDirs: Array<File> = application.getExternalFilesDirs(null)

        val internalStorageDirectory: File
            get() = filesDirs.first()

        val externalSdCardDirectories: List<File>
            get() = filesDirs.drop(1)

        val internalStorageRootPath
            get() = filesDirs.first().absolutePath.substringBefore("/Android/data")

        val externalStorageRootPath
            get() = filesDirs.drop(1).first().absolutePath.substringBefore("/Android/data")

        val downloadDirectoryPath: String =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    }
