package com.sn.snfilemanager.view.dialog.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.mediastorepv.data.Media
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.getDirectoryNameFromPath
import com.sn.snfilemanager.core.extensions.toFormattedDateFromUnixTime
import com.sn.snfilemanager.core.extensions.toHumanReadableByteCount
import com.sn.snfilemanager.core.util.StringValue
import com.sn.snfilemanager.feature.files.data.FileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class DetailDialogViewModel : ViewModel() {
    private val _detailItemLiveData: MutableLiveData<MutableList<Detail>> = MutableLiveData()
    val detailItemLiveData: LiveData<MutableList<Detail>> = _detailItemLiveData
    private val updateInterval = 500L

    fun <T> createDetailItem(itemList: MutableList<T>) {
        when (itemList.firstOrNull()) {
            is FileModel -> getFileDetailList(itemList.filterIsInstance<FileModel>())
            is Media -> getMediaDetailList(itemList.filterIsInstance<Media>())
        }
    }

    private fun getFileDetailList(itemList: List<FileModel>) {
        val detailItemList: MutableList<Detail> = mutableListOf()
        val isContainsFolder = itemsContainsFolder(itemList)

        viewModelScope.launch(Dispatchers.IO) {
            val itemCount: Int = itemList.size
            if (itemCount > 1) {
                detailItemList.addAll(
                    createDetailItemList(R.string.selected_item_count to itemCount.toString()),
                )
            } else {
                val item = itemList.first()
                detailItemList.addAll(
                    createDetailItemList(
                        R.string.name to item.name,
                        R.string.path to item.absolutePath.getDirectoryNameFromPath(),
                    ),
                )
            }

            _detailItemLiveData.postValue(detailItemList)

            var totalSize = 0L
            var totalCount = 0L

            itemList.forEach { fileModel ->
                if (fileModel.isDirectory) {
                    calculateDirectoryDetails(
                        Paths.get(fileModel.absolutePath),
                        totalSize,
                        totalCount,
                    ) { size, count ->
                        totalSize = size
                        totalCount = count
                        updateDetailList(
                            itemCount,
                            isContainsFolder,
                            fileModel,
                            totalSize,
                            totalCount,
                        )
                    }
                } else {
                    totalSize += fileModel.size
                    totalCount++

                    updateDetailList(
                        itemCount,
                        isContainsFolder,
                        fileModel,
                        totalSize,
                        totalCount,
                    )
                }
            }
        }
    }

    private fun calculateDirectoryDetails(
        directory: Path,
        currentSize: Long,
        currentCount: Long,
        onUpdate: (Long, Long) -> Unit,
    ) {
        var totalSize = currentSize
        var totalCount = currentCount
        var lastUpdateTime = System.currentTimeMillis()

        try {
            Files.list(directory).use { paths ->
                paths.forEach { path ->
                    try {
                        if (Files.isDirectory(path)) {
                            calculateDirectoryDetails(path, totalSize, totalCount) { size, count ->
                                totalSize = size
                                totalCount = count

                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastUpdateTime >= updateInterval) {
                                    onUpdate(totalSize, totalCount)
                                    lastUpdateTime = currentTime
                                }
                            }
                        } else {
                            totalSize += Files.size(path)
                            totalCount++

                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastUpdateTime >= updateInterval) {
                                onUpdate(totalSize, totalCount)
                                lastUpdateTime = currentTime
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onUpdate(totalSize, totalCount)
        }

        onUpdate(totalSize, totalCount)
    }

    private fun updateDetailList(
        itemCount: Int,
        isContainsFolder: Boolean,
        fileModel: FileModel,
        totalSize: Long,
        totalCount: Long,
    ) {
        val newList =
            if (itemCount > 1) {
                if (isContainsFolder) {
                    createDetailItemList(
                        R.string.selected_item_count to itemCount.toString(),
                        R.string.total_size to totalSize.toHumanReadableByteCount(),
                        R.string.number_of_children to totalCount.toString(),
                    )
                } else {
                    createDetailItemList(
                        R.string.selected_item_count to itemCount.toString(),
                        R.string.total_size to totalSize.toHumanReadableByteCount(),
                    )
                }
            } else if (isContainsFolder) {
                createDetailItemList(
                    R.string.name to fileModel.name,
                    R.string.path to fileModel.absolutePath.getDirectoryNameFromPath(),
                    R.string.size to totalSize.toHumanReadableByteCount(),
                    R.string.number_of_children to totalCount.toString(),
                )
            } else {
                emptyList()
            }

        _detailItemLiveData.postValue(newList.toMutableList())
    }

    private fun getMediaDetailList(itemList: List<Media>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (itemList.size > 1) {
                val itemSize = itemList.size
                val initialList =
                    createDetailItemList(
                        R.string.selected_item_count to itemSize.toString(),
                    )
                _detailItemLiveData.postValue(initialList.toMutableList())

                var totalSize = 0L
                var lastUpdateTime = System.currentTimeMillis()

                itemList.forEach { media ->
                    totalSize += media.size

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= updateInterval) {
                        val newList =
                            createDetailItemList(
                                R.string.selected_item_count to itemSize.toString(),
                                R.string.total_size to totalSize.toHumanReadableByteCount(),
                            )
                        _detailItemLiveData.postValue(newList.toMutableList())
                        lastUpdateTime = currentTime
                    }
                }

                val finalList =
                    createDetailItemList(
                        R.string.selected_item_count to itemSize.toString(),
                        R.string.total_size to totalSize.toHumanReadableByteCount(),
                    )
                _detailItemLiveData.postValue(finalList.toMutableList())
            } else if (itemList.size == 1) {
                val item = itemList.first()
                val singleItemList =
                    createDetailItemList(
                        R.string.name to item.name,
                        R.string.path to item.data.getDirectoryNameFromPath(),
                        R.string.size to item.size.toHumanReadableByteCount(),
                        R.string.last_modified to item.dateModified.toFormattedDateFromUnixTime(),
                    )
                _detailItemLiveData.postValue(singleItemList.toMutableList())
            }
        }
    }

    private fun itemsContainsFolder(itemList: List<FileModel>): Boolean = itemList.any { it.isDirectory }

    private fun createDetailItemList(vararg pairs: Pair<Int, String>): List<Detail> {
        return pairs.map { (titleResId, value) ->
            Detail(
                StringValue.StringResource(titleResId),
                value,
            )
        }
    }
}
