package com.sn.snfilemanager.providers.mediastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MimeTypes(val types: String, val values: Array<String>) : Parcelable {
    IMAGES("images", arrayOf("jpeg", "png", "gif", "bmp", "WebP", "HEIF")),
    VIDEOS("videos", arrayOf("mp4", "3gp", "avi", "mkv", "wmv"));
}
