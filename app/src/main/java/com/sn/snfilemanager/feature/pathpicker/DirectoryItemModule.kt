package com.sn.snfilemanager.feature.pathpicker

import com.idanatz.oneadapter.external.event_hooks.ClickEventHook
import com.idanatz.oneadapter.external.modules.ItemModule
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.util.DirectoryType
import com.sn.snfilemanager.databinding.ItemDirectoryBinding
import com.sn.snfilemanager.feature.files.data.FileModel


class DirectoryItemModule() : ItemModule<FileModel>() {

    var onClick: ((FileModel) -> Unit)? = null

    init {
        config {
            layoutResource = R.layout.item_directory
        }
        onBind { model, viewBinder, _ ->
            viewBinder.bindings(ItemDirectoryBinding::bind).run {
                tvDirectoryName.text = model.name
                ivDirectoryImage.setImageResource(getDirectoryIcon(model.name))
            }
        }
        eventHooks += ClickEventHook<FileModel>().apply {
            onClick { model, _, _ ->
                onClick?.invoke(model)
            }
        }
    }

    private fun getDirectoryIcon(name: String) = when (name) {
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