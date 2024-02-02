package com.sn.snfilemanager.feature.pathpicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.util.DirectoryType
import com.sn.snfilemanager.databinding.ItemDirectoryBinding
import com.sn.snfilemanager.feature.files.adapter.FileDiffCallback
import com.sn.snfilemanager.feature.files.data.FileModel

class DirectoryItemAdapter(
    private val onClick: ((FileModel) -> Unit)? = null,
) : RecyclerView.Adapter<DirectoryItemAdapter.DirectoryViewHolder>() {
    private var directoryItems: List<FileModel> = emptyList()

    fun setItems(newItems: List<FileModel>) {
        val diffResult = DiffUtil.calculateDiff(FileDiffCallback(directoryItems, newItems))
        directoryItems = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DirectoryItemAdapter.DirectoryViewHolder {
        val binding =
            ItemDirectoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DirectoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DirectoryItemAdapter.DirectoryViewHolder,
        position: Int,
    ) {
        holder.bind(directoryItems[position])
    }

    override fun getItemCount(): Int {
        return directoryItems.size
    }

    inner class DirectoryViewHolder(private val binding: ItemDirectoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.click {
                val position = adapterPosition
                onClick?.invoke(directoryItems[position])
            }
        }

        fun bind(model: FileModel) {
            binding.tvDirectoryName.text = model.name
            binding.ivDirectoryImage.setImageResource(getDirectoryIcon(model.name))
        }
    }

    private fun getDirectoryIcon(name: String) =
        when (name) {
            DirectoryType.MUSIC.type -> R.drawable.ic_directory_music
            DirectoryType.MOVIES.type -> R.drawable.ic_directory_movies
            DirectoryType.DOWNLOAD.type -> R.drawable.ic_directory_download
            DirectoryType.DOCUMENTS.type -> R.drawable.ic_directory_document
            DirectoryType.ANDROID.type -> R.drawable.ic_directory_android
            DirectoryType.PICTURES.type -> R.drawable.ic_directory_pictures
            DirectoryType.DCIM.type -> R.drawable.ic_directory_dcim
            else -> R.drawable.ic_directory
        }
}
