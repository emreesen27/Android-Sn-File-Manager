package com.sn.snfilemanager.core.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MimeTypes(val types: String, val values: Array<String>) : Parcelable {
    IMAGES("images", arrayOf("jpeg", "png", "gif", "bmp", "WebP", "HEIF")),
    VIDEOS("videos", arrayOf("mp4", "3gp", "avi", "mkv", "wmv")),
    AUDIOS("audios", arrayOf("mp3", "aac", "wav", "ogg", "mid", "flac", "amr"))
}
