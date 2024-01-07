package com.sn.snfilemanager.view.dialog.detail

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.idanatz.oneadapter.OneAdapter
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogDetailBinding

class DetailDialog(
    context: Context,
    private val itemList: MutableList<Detail>
) : Dialog(context) {

    var onDismiss: (() -> Unit)? = null
    private var oneAdapter: OneAdapter? = null
    private val binding: DialogDetailBinding by lazy {
        DialogDetailBinding.inflate(layoutInflater)
    }

    init {
        setOnDismissListener {
            onDismiss?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setWindowProperty()
        setCancelable(true)
        initAdapter()
        binding.tvOk.click { dismiss() }
    }


    private fun initAdapter() {
        oneAdapter = OneAdapter(binding.recyclerDetail) {
            itemModules += DetailItemModule(context)
        }.apply {
            setItems(itemList)
        }
    }

    private fun setWindowProperty() {
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
    }

}
