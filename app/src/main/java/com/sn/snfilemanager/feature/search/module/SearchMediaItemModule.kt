package com.sn.snfilemanager.feature.search.module

import com.bumptech.glide.Glide
import com.idanatz.oneadapter.external.modules.ItemModule
import com.idanatz.oneadapter.external.states.SelectionState
import com.idanatz.oneadapter.external.states.SelectionStateConfig
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.core.util.FileExtension
import com.sn.snfilemanager.databinding.ItemSearchImageBinding
import com.sn.snfilemanager.providers.mediastore.MediaFile

class SearchMediaItemModule : ItemModule<MediaFile>() {

    var onSelected: ((MediaFile, Boolean) -> Unit)? = null

    init {
        config {
            layoutResource = R.layout.item_search_image
        }
        onBind { model, viewBinder, _ ->
            viewBinder.bindings(ItemSearchImageBinding::bind).run {
                tvImageName.text = model.name
                updateUIForModel(model, this)
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


    // Todo check loadWithGlide
    private fun updateUIForModel(model: MediaFile, binding: ItemSearchImageBinding) {
        with(binding) {
            ivPlay.gone()
            val iconResId = model.ext?.let { FileExtension.getIconResourceId(it) }
            if (iconResId != 0) {
                iconResId?.let { ivImage.setImageResource(it) }
            } else if (FileExtension.isVideoExtension(model.ext)) {
                ivPlay.visible()
                //ivImage.loadWithGlide(model.data)
                Glide.with(binding.root).load(model.uri).into(ivImage)
            } else {
                Glide.with(binding.root).load(model.uri).into(ivImage)
                //ivImage.loadWithGlide(model.)
            }
        }
    }

}