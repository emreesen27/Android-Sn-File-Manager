package com.sn.snfilemanager.feature.files.adapter

import androidx.recyclerview.widget.DiffUtil
import com.sn.snfilemanager.feature.files.data.FileModel

class FileDiffCallback(
    private val oldList: List<FileModel>,
    private val newList: List<FileModel>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
