package com.sn.snfilemanager.feature.media.module

import com.bumptech.glide.Glide
import com.idanatz.oneadapter.external.modules.ItemModule
import com.idanatz.oneadapter.external.states.SelectionState
import com.idanatz.oneadapter.external.states.SelectionStateConfig
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.invisible
import com.sn.snfilemanager.core.extensions.setMargins
import com.sn.snfilemanager.core.extensions.toHumanReadableByteCount
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.core.util.FileExtension
import com.sn.snfilemanager.databinding.ItemAudioBinding
import com.sn.snfilemanager.providers.mediastore.MediaFile

class DocumentItemModule : ItemModule<MediaFile>() {

    var onSelected: ((MediaFile, Boolean) -> Unit)? = null

    init {
        config {
            layoutResource = R.layout.item_audio
        }
        onBind { model, viewBinder, metaData ->
            viewBinder.bindings(ItemAudioBinding::bind).run {

                Glide.with(viewBinder.rootView)
                    .load(model.ext?.let { FileExtension.getIconResourceId(it) }).into(ivImage)

                tvFileName.text = model.name
                tvFileSize.text = model.size.toHumanReadableByteCount()

                if (metaData.isSelected) {
                    ivSelected.visible()
                    ivImage.setMargins(50)
                } else {
                    ivSelected.invisible()
                    ivImage.setMargins(0)
                }
            }
        }
        states += SelectionState<MediaFile>().apply {
            config {
                enabled = true
                selectionTrigger = SelectionStateConfig.SelectionTrigger.LongClick
            }
            onSelected { model, selectedItem ->
                onSelected?.invoke(model, selectedItem)
            }
        }
    }

}