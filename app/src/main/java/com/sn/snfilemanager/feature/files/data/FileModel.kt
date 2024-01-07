package com.sn.snfilemanager.feature.files.data

import com.idanatz.oneadapter.external.interfaces.Diffable
import com.sn.snfilemanager.core.extensions.toFormattedDate
import com.sn.snfilemanager.core.extensions.toHumanReadableByteCount
import java.io.File
import java.util.*

data class FileModel(
    val id: Long,
    val name: String,
    val isDirectory: Boolean,
    val absolutePath: String,
    val childCount: Int?,
    val lastModified: String,
    val size: String,
    val extension: String,
    val isHidden: Boolean,
) : Diffable {
    override val uniqueIdentifier: Long
        get() = id

    override fun areContentTheSame(other: Any): Boolean = id == (other as? FileModel)?.id
}

fun File.toFileModel(): FileModel {
    return FileModel(
        name = this.name,
        id = UUID.randomUUID().mostSignificantBits,
        isDirectory = this.isDirectory,
        absolutePath = this.absolutePath,
        childCount = if (this.isDirectory) this.listFiles()?.size ?: 0 else null,
        lastModified = this.lastModified().toFormattedDate(),
        size = this.length().toHumanReadableByteCount(),
        extension = this.extension,
        isHidden = this.isHidden
    )
}