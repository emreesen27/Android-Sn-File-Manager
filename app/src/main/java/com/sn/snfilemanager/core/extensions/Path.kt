package com.sn.snfilemanager.core.extensions

import java.nio.file.Files
import java.nio.file.Path

fun Path.getUniqueFileNameWithCounter(): Path {
    var index = 1
    var uniquePath = this

    if (Files.isDirectory(this)) {
        val folderName = this.fileName.toString()
        uniquePath = uniquePath.resolveSibling(folderName)

        while (Files.exists(uniquePath)) {
            uniquePath = this.resolveSibling("$folderName($index)")
            index++
        }
    } else {
        while (Files.exists(uniquePath)) {
            val fileName = this.fileName.toString()
            val dotIndex = fileName.lastIndexOf('.')
            val baseName = if (dotIndex != -1) fileName.substring(0, dotIndex) else fileName
            val extension = if (dotIndex != -1) fileName.substring(dotIndex) else ""

            uniquePath = this.resolveSibling("$baseName($index)$extension")
            index++
        }
    }

    return uniquePath
}
