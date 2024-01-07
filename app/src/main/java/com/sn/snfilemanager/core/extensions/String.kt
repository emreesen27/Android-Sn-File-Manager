package com.sn.snfilemanager.core.extensions

fun String.getFileExtension(): String? {
    val lastDotIndex = this.lastIndexOf(".")
    if (lastDotIndex >= 0) {
        return this.substring(lastDotIndex + 1)
    }
    return null
}

fun String.getDirectoryNameFromPath(): String {
    val lastSeparatorIndex = this.lastIndexOf("/")
    return if (lastSeparatorIndex != -1) {
        this.substring(0, lastSeparatorIndex)
    } else {
        this
    }
}