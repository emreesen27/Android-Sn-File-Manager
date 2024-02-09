package com.sn.snfilemanager.view.dialog.detail

import androidx.databinding.ObservableBoolean
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
import com.sn.snfilemanager.feature.files.data.toFileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class DetailDialogViewModel : ViewModel() {
    private val _detailItemLiveData: MutableLiveData<MutableList<Detail>> = MutableLiveData()
    val detailItemLiveData: LiveData<MutableList<Detail>> = _detailItemLiveData

    val progressObservable = ObservableBoolean(false)

    fun <T> createDetailItem(itemList: MutableList<T>) {
        when (itemList.firstOrNull()) {
            is FileModel -> getFileDetailList(itemList.filterIsInstance<FileModel>())
            is Media -> getMediaDetailList(itemList.filterIsInstance<Media>())
        }
    }

    private fun getFileDetailList(itemList: List<FileModel>) {
        val detailItemList: MutableList<Detail> = mutableListOf()
        val isContainsFolder = itemsContainsFolder(itemList)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                progressObservable.set(true)
                val itemCount: Int = itemList.size
                val totalSizeAndCount = getTotalSizeAndFileCount(itemList)
                if (itemCount > 1) {
                    if (isContainsFolder) {
                        val items =
                            createDetailItemList(
                                R.string.selected_item_count to itemCount.toString(),
                                R.string.total_size to totalSizeAndCount.first.toHumanReadableByteCount(),
                                R.string.number_of_children to totalSizeAndCount.second.toString(),
                            )
                        detailItemList.addAll(items)
                    } else {
                        val items =
                            createDetailItemList(
                                R.string.selected_item_count to itemCount.toString(),
                                R.string.total_size to totalSizeAndCount.first.toHumanReadableByteCount(),
                            )
                        detailItemList.addAll(items)
                    }
                } else {
                    if (isContainsFolder) {
                        val items =
                            createDetailItemList(
                                R.string.name to itemList.first().name,
                                R.string.path to itemList.first().absolutePath.getDirectoryNameFromPath(),
                                R.string.size to totalSizeAndCount.first.toHumanReadableByteCount(),
                                R.string.number_of_children to totalSizeAndCount.second.toString(),
                            )
                        detailItemList.addAll(items)
                    } else {
                        val item = itemList.first()
                        val items =
                            createDetailItemList(
                                R.string.name to item.name,
                                R.string.path to item.absolutePath.getDirectoryNameFromPath(),
                                R.string.size to item.readableSize,
                                R.string.last_modified to item.lastModified,
                            )
                        detailItemList.addAll(items)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                _detailItemLiveData.postValue(detailItemList)
                progressObservable.set(false)
            }
        }
    }

    private fun getMediaDetailList(itemList: List<Media>) {
        val detailItemList: MutableList<Detail> = mutableListOf()

        if (itemList.size > 1) {
            val itemSize = itemList.size
            val totalSize: String = itemList.sumOf { it.size }.toHumanReadableByteCount()
            val items =
                createDetailItemList(
                    R.string.selected_item_count to itemSize.toString(),
                    R.string.total_size to totalSize,
                )
            detailItemList.addAll(items)
        } else if (itemList.size == 1) {
            val item = itemList.first()
            val items =
                createDetailItemList(
                    R.string.name to item.name,
                    R.string.path to item.data.getDirectoryNameFromPath(),
                    R.string.size to item.size.toHumanReadableByteCount(),
                    R.string.last_modified to item.dateModified.toFormattedDateFromUnixTime(),
                )
            detailItemList.addAll(items)
        }
        _detailItemLiveData.postValue(detailItemList)
    }

    private fun getTotalSizeAndFileCount(itemList: List<FileModel>): Pair<Long, Int> =
        itemList.fold(Pair(0L, 0)) { acc, item ->
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
