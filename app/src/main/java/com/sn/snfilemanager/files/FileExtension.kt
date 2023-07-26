package com.sn.snfilemanager.files

import com.sn.snfilemanager.R

enum class FileExtension(val extension: String, val iconResId: Int) {
    TXT("txt", R.drawable.ic_txt),
    PDF("pdf", R.drawable.ic_pdf),
    MP4("mp4", 0),
    JPG("jpg", 0),
    PNG("png", 0),
    APK("apk", R.drawable.ic_apk),
    DOC("doc", R.drawable.ic_doc),
    EPS("eps", R.drawable.ic_eps),
    DMG("dmg", R.drawable.ic_dmg),
    HTML("html", R.drawable.ic_html),
    ISO("iso", R.drawable.ic_iso);


    companion object {
        fun getIconResourceId(extension: String): Int {
            return FileExtension.values().find { it.extension == extension }?.iconResId
                ?: R.drawable.ic_unknown
        }

        fun isVideoExtension(extension: String): Boolean {
            return listOf("mp4").contains(extension)
        }
    }
}