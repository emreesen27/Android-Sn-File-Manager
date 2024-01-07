package com.sn.snfilemanager.view.dialog.detail

import android.content.Context
import com.idanatz.oneadapter.external.modules.ItemModule
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.ItemDetailBinding

class DetailItemModule(context: Context) : ItemModule<Detail>() {
    init {
        config {
            layoutResource = R.layout.item_detail
        }
        onBind { model, viewBinder, _ ->
            viewBinder.bindings(ItemDetailBinding::bind).run {
                tvTitle.text = model.title.asString(context)
                tvValue.text = model.value
            }
        }
    }
}