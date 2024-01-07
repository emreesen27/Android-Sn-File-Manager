package com.sn.snfilemanager.providers.mediastore

import android.net.Uri
import com.idanatz.oneadapter.external.interfaces.Diffable
import com.sn.mediastorepv.data.Media
import com.sn.mediastorepv.data.MediaType

data class MediaFile(
    var id: Long,
    val name: String,
    val dateAdded: Long,
    val mimeType: String,
    val size: Long,
    val mediaType: MediaType,
    val uri: Uri?,
    val ext: String?,
    val data: String,
    val dateModified: Long,
    val directoryPath: String
) : Diffable {
    override val uniqueIdentifier: Long
        get() = id

    override fun areContentTheSame(other: Any): Boolean = id == (other as? MediaFile)?.id
}

fun MediaFile.toMedia(): Media {
    return Media(
        id = id,
        name = name,
        dateAdded = dateAdded,
        mimeType = mimeType,
        size = size,
        mediaType = mediaType,
        uri = uri,
        ext = ext,
        data = data,
        dateModified = dateModified,
        directoryPath = directoryPath
    )
}

fun Media.toMediaFile(): MediaFile {
    return MediaFile(
        id = id,
        name = name,
        dateAdded = dateAdded,
        mimeType = mimeType,
        size = size,
        mediaType = mediaType,
        uri = uri,
        ext = ext,
        data = data,
        dateModified = dateModified,
        directoryPath = directoryPath
    )
}
