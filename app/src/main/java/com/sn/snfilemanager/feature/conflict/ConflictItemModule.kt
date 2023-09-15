package com.sn.snfilemanager.feature.conflict

import com.idanatz.oneadapter.external.modules.ItemModule
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.ItemConflictBinding
import com.sn.snfilemanager.providers.mediastore.MediaFile

class ConflictItemModule : ItemModule<MediaFile>() {

    var onSelected: ((MediaFile, ConflictStrategy?) -> Unit)? = null

    init {
        config {
            layoutResource = R.layout.item_conflict
        }
        onBind { model, viewBinder, _ ->
            viewBinder.bindings(ItemConflictBinding::bind).run {
                tvFileName.text = model.name
                radioGroup.setOnCheckedChangeListener { _, id ->
                    onSelected?.invoke(model, getSelection(id))
                }
            }
        }
    }

    private fun getSelection(id: Int): ConflictStrategy? =
        when (id) {
            R.id.rb_skip -> {
                ConflictStrategy.SKIP
            }
            R.id.rb_keep -> {
                ConflictStrategy.KEEP_BOTH
            }
            R.id.rb_overwrite -> {
                ConflictStrategy.OVERWRITE
            }
            else -> null
        }
}