package com.sn.snfilemanager.core.util

import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.feature.files.data.toFileModel
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

object FileUtils {
    fun getTotalSizeAndFileCount(itemList: List<FileModel>): Pair<Long, Long> =
        itemList.fold(Pair(0L, 0L)) { acc, item ->
            if (item.isDirectory && Files.isReadable(Paths.get(item.absolutePath))) {
                val childResult =
                    getTotalSizeAndFileCount(
                        Files.list(Paths.get(item.absolutePath)).collect(Collectors.toList())
                            .map { it.toFileModel() },
                    )
                Pair(acc.first + childResult.first, acc.second + childResult.second)
            } else {
                Pair(acc.first + item.size, acc.second + 1)
            }
        }
}
