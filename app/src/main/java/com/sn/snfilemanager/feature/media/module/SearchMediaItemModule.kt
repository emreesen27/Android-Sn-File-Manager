package com.sn.snfilemanager.feature.media.module

import com.bumptech.glide.Glide
import com.idanatz.oneadapter.external.modules.ItemModule
import com.idanatz.oneadapter.external.states.SelectionState
import com.idanatz.oneadapter.external.states.SelectionStateConfig
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.ItemSearchImageBinding
import com.sn.snfilemanager.providers.mediastore.MediaFile

class SearchMediaItemModule : ItemModule<MediaFile>() {

    interface Selected {
        fun onSelected(model: MediaFile, selected: Boolean)
    }

    var selected: Selected? = null

    init {
        config {
            layoutResource = R.layout.item_search_image
        }
        onBind { model, viewBinder, _ ->
            viewBinder.bindings(ItemSearchImageBinding::bind).run {
                Glide.with(viewBinder.rootView).load(model.uri).into(ivImage)
                tvImageName.text = model.name
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