package com.sn.snfilemanager.media

import android.net.Uri
import com.idanatz.oneadapter.external.interfaces.Diffable

data class MediaFile(
    var id: Long,
    val name: String,
    val dateAdded: Long,
    val mimeType: String,
    val size: Long,
    val mediaType: MediaType,
    val uri: Uri?,
) : Diffable {
    override val uniqueIdentifier: Long
        get() = id

    override fun areContentTheSame(other: Any): Boolean = id == (other as? MediaFile)?.id
//val deletedRows = contentResolver.delete(uri, null, null)
}
