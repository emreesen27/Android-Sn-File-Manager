package com.sn.snfilemanager.view.dialog.detail

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogDetailBinding

class DetailDialog(
    context: Context,
    private val itemList: MutableList<Detail>
) : Dialog(context) {

    var onDismiss: (() -> Unit)? = null
    private var adapter: DetailItemAdapter? = null
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
        adapter = DetailItemAdapter(context)
        adapter?.setItems(itemList)
        binding.recyclerDetail.adapter = adapter
    }

    private fun setWindowProperty() {
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
    }

}
