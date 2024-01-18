package com.sn.snfilemanager.feature.files.data

import com.sn.snfilemanager.core.extensions.toFormattedDate
import com.sn.snfilemanager.core.extensions.toHumanReadableByteCount
import java.io.File
import java.util.UUID

data class FileModel(
    val id: Long,
    val name: String,
    val isDirectory: Boolean,
    val absolutePath: String,
    val childCount: Int?,
    val childList: List<File>?,
    val lastModified: String,
    val readableSize: String,
    val size: Long,
    val extension: String,
    val isHidden: Boolean,
    var isSelected: Boolean
)

fun File.toFileModel(): FileModel {
    return FileModel(
        id = UUID.randomUUID().mostSignificantBits,
        name = this.name,
        isDirectory = this.isDirectory,
        absolutePath = this.absolutePath,
        childCount = if (this.isDirectory) this.listFiles()?.size ?: 0 else null,
        childList = if (this.isDirectory) this.listFiles()?.toList() else emptyList(),
        lastModified = this.lastModified().toFormattedDate(),
        readableSize = this.length().toHumanReadableByteCount(),
        size = this.length(),
        extension = this.extension,
        isHidden = this.isHidden,
        isSelected = false
    )
}