package com.sn.snfilemanager.files

import com.idanatz.oneadapter.external.interfaces.Diffable
import com.sn.snfilemanager.R
import com.sn.snfilemanager.extensions.toHumanReadableByteCount
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class FileModel(
    val id: Long,
    val name: String,
    val isDirectory: Boolean,
    val absolutePath: String,
    val childCount: Int?,
    val lastModified: String,
    val size: String
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
        lastModified = convertLastModifiedToDate(this),
        size = this.length().toHumanReadableByteCount()
    )
}

// Todo burdan taşı
fun convertLastModifiedToDate(file: File): String {
    val lastModified = file.lastModified()
    val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
    val date = Date(lastModified)
    return dateFormat.format(date)
}
