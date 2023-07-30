package com.sn.snfilemanager.feature.media.module

import com.bumptech.glide.Glide
import com.idanatz.oneadapter.external.modules.ItemModule
import com.idanatz.oneadapter.external.states.SelectionState
import com.idanatz.oneadapter.external.states.SelectionStateConfig
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.ItemMediaBinding
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.invisible
import com.sn.snfilemanager.core.extensions.setMargins
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.providers.mediastore.MediaFile
import com.sn.snfilemanager.providers.mediastore.MediaType


class MediaItemModule : ItemModule<MediaFile>() {

    interface Selected {
        fun onSelected(model: MediaFile, selected: Boolean)
    }

    var selected: Selected? = null
    var mediaType: MediaType? = null

    init {
        config {
            layoutResource = R.layout.item_media
        }
        onBind { model, viewBinder, metaData ->
            viewBinder.bindings(ItemMediaBinding::bind).run {
                if (mediaType == MediaType.VIDEOS) ivPlay.visible() else ivPlay.gone()
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