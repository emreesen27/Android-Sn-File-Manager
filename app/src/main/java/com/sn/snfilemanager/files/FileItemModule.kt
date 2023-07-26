package com.sn.snfilemanager.files

import android.content.Context
import com.idanatz.oneadapter.external.event_hooks.ClickEventHook
import com.idanatz.oneadapter.external.modules.ItemModule
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.ItemFileBinding
import com.sn.snfilemanager.extensions.gone
import com.sn.snfilemanager.extensions.loadWithGlide
import com.sn.snfilemanager.extensions.visible

class FileItemModule(val context: Context) :
    ItemModule<FileModel>() {

    var onClick: ((FileModel) -> Unit)? = null

    init {
        config {
            layoutResource = R.layout.item_file
        }
        onBind { model, viewBinder, _ ->
            viewBinder.bindings(ItemFileBinding::bind).run {
                tvFileName.text = model.name
                tvFileInfo.text = if (model.isDirectory) context.getString(
                    R.string.child_count,
                    model.lastModified,
                    model.childCount
                ) else {
                    context.getString(R.string.file_size, model.lastModified, model.size)
                }
                updateUIForModel(model, this)
            }
        }
        eventHooks += ClickEventHook<FileModel>().apply {
            onClick { model, _, _ ->
                onClick?.invoke(model)
            }
        }
    }

    private fun updateUIForModel(model: FileModel, binding: ItemFileBinding) {
        with(binding) {
            ivPlay.gone()
            if (model.isDirectory) {
                ivFileImage.setImageResource(R.drawable.ic_folder)
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
}