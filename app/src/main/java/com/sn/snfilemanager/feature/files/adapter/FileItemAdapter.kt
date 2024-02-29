package com.sn.snfilemanager.feature.files.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.invisible
import com.sn.snfilemanager.core.extensions.loadWithGlide
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.core.util.DirectoryType
import com.sn.snfilemanager.core.util.FileExtension
import com.sn.snfilemanager.databinding.ItemFileBinding
import com.sn.snfilemanager.feature.files.data.FileModel

class FileItemAdapter(
    private val context: Context,
    private val onSelected: ((FileModel, Boolean) -> Unit)? = null,
    private val onClick: ((FileModel) -> Unit)? = null,
    private val selectionCallback: SelectionCallback? = null,
) : RecyclerView.Adapter<FileItemAdapter.FileViewHolder>() {
    private val selectedItems: MutableList<FileModel> = mutableListOf()
    private var isSelectionModeActive = false
    private var fileItems: MutableList<FileModel> = mutableListOf()

    fun setItems(newItems: MutableList<FileModel>) {
        val diffResult = DiffUtil.calculateDiff(FileDiffCallback(fileItems, newItems))
        fileItems = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun addItem(newItems: FileModel) {
        fileItems.add(newItems)
        notifyItemInserted(fileItems.size)
    }

    fun removeItems(filesToRemove: List<FileModel>) {
        for (fileToRemove in filesToRemove) {
            val position = fileItems.indexOf(fileToRemove)
            if (position != RecyclerView.NO_POSITION) {
                fileItems = fileItems.toMutableList().apply { removeAt(position) }
                notifyItemRemoved(position)
            }
        }
    }

    fun finishSelectionAndReset() {
        for (selectedItem in selectedItems) {
            val position = fileItems.indexOf(selectedItem)
            if (position != RecyclerView.NO_POSITION) {
                selectedItem.isSelected = false
                notifyItemChanged(position)
            }
        }
        selectedItems.clear()
        isSelectionModeActive = false
        selectionCallback?.onEndSelection()
    }

    fun getItems(): MutableList<FileModel> = fileItems

    fun getSelectedItems(): MutableList<FileModel> = selectedItems

    fun selectionIsActive(): Boolean = isSelectionModeActive

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FileItemAdapter.FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FileItemAdapter.FileViewHolder,
        position: Int,
    ) {
        holder.bind(fileItems[position])
    }

    override fun getItemCount(): Int {
        return fileItems.size
    }

    inner class FileViewHolder(private val binding: ItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.click {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isSelectionModeActive) {
                        toggleSelection(fileItems[position])
                    } else {
                        onClick?.invoke(fileItems[position])
                    }
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (!isSelectionModeActive) {
                        startSelection(fileItems[position])
                    }
                }
                true
            }
        }

        fun bind(fileModel: FileModel) {
            if (fileModel.isSelected) {
                binding.ivSelected.visible()
            } else {
                binding.ivSelected.invisible()
            }

            binding.tvFileName.text = fileModel.name
            binding.tvFileInfo.text =
                if (fileModel.isDirectory) {
                    context.getString(
                        R.string.child_modified,
                        fileModel.lastModified,
                    )
                } else {
                    context.getString(
                        R.string.file_size,
                        fileModel.lastModified,
                        fileModel.readableSize,
                    )
                }
            updateUIForModel(fileModel, binding)
        }
    }

    private fun updateUIForModel(
        model: FileModel,
        binding: ItemFileBinding,
    ) {
        with(binding) {
            ivPlay.gone()
            if (model.isDirectory) {
                ivFileImage.setImageResource(getDirectoryIcon(model.name))
            } else {
                val iconResId = FileExtension.getIconResourceId(model.extension)
                if (iconResId != 0) {
                    ivFileImage.setImageResource(iconResId)
                } else if (FileExtension.isVideoExtension(model.extension)) {
                    ivPlay.visible()
                    ivFileImage.loadWithGlide(model.absolutePath)
                } else {
                    ivFileImage.loadWithGlide(model.absolutePath)
                }
            }
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

    private fun startSelection(fileModel: FileModel) {
        selectionCallback?.onStartSelection()
        isSelectionModeActive = true
        selectedItems.clear()
        toggleSelection(fileModel)
    }

    private fun toggleSelection(fileModel: FileModel) {
        val position = fileItems.indexOf(fileModel)
        if (selectedItems.contains(fileModel)) {
            if (selectedItems.size == 1) {
                finishSelectionAndReset()
                onSelected?.invoke(fileModel, false)
                return
            }
            selectedItems.remove(fileModel)
            fileModel.isSelected = false
        } else {
            selectedItems.add(fileModel)
            fileModel.isSelected = true
        }
        selectionCallback?.onUpdateSelection(selectedItems.size)
        onSelected?.invoke(fileModel, selectedItems.contains(fileModel))
        notifyItemChanged(position)
    }

    interface SelectionCallback {
        fun onStartSelection()

        fun onUpdateSelection(selectedSize: Int)

        fun onEndSelection()
    }
}
