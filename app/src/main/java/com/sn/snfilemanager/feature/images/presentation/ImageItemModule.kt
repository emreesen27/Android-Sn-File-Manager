package com.sn.snfilemanager.feature.images.presentation

import com.bumptech.glide.Glide
import com.idanatz.oneadapter.external.modules.ItemModule
import com.idanatz.oneadapter.external.states.SelectionState
import com.idanatz.oneadapter.external.states.SelectionStateConfig
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.ItemImageBinding
import com.sn.snfilemanager.extensions.invisible
import com.sn.snfilemanager.extensions.setMargins
import com.sn.snfilemanager.extensions.visible
import com.sn.snfilemanager.media.MediaFile


class ImageItemModule : ItemModule<MediaFile>() {

    interface Selected {
        fun onSelected(model: MediaFile, selected: Boolean)
    }

    var selected: Selected? = null

    init {
        config {
            layoutResource = R.layout.item_image
        }
        onBind { model, viewBinder, metaData ->
            viewBinder.bindings(ItemImageBinding::bind).run {
                Glide.with(viewBinder.rootView).load(model.uri).into(ivImage)

                if (metaData.isSelected) {
                    ivSelected.visible()
                    ivImage.setBackgroundResource(R.drawable.border_selected)
                    ivImage.setMargins(50)
                } else {
                    ivSelected.invisible()
                    ivImage.setBackgroundResource(0)
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
                selected?.onSelected(model, selectedItem)
            }
        }
    }

}