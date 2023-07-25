package com.sn.snfilemanager.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MimeTypes(val types: String, val values: Array<String>) : Parcelable {
    IMAGE("image", arrayOf("jpeg", "png", "gif", "bmp", "WebP", "HEIF"))
}
