package com.sn.snfilemanager.feature.media.adapter

import androidx.recyclerview.widget.DiffUtil
import com.sn.snfilemanager.providers.mediastore.MediaFile

class MediaDiffCallback(
    private val oldList: List<MediaFile>,
    private val newList: List<MediaFile>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}