package com.sn.snfilemanager.feature.conflict

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.idanatz.oneadapter.OneAdapter
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogConflictBinding
import com.sn.snfilemanager.providers.mediastore.MediaFile


class ConflictDialog(
    context: Context,
    private val list: MutableList<MediaFile>,
    private val listener: ConflictDialogListener
) : Dialog(context) {

    private val binding: DialogConflictBinding by lazy {
        DialogConflictBinding.inflate(layoutInflater)
    }
    private var oneAdapter: OneAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCancelable(false)
        setWindowProperty()
        initAdapter()
        initView()
        setItems()
        initRecyclerLayoutChangeListener()
    }

    private fun setItems() {
        oneAdapter?.setItems(list)
    }

    private fun setWindowProperty() {
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
    }

    private fun initView() {
        binding.btnCancel.click {
            listener.onCancel()
            dismiss()
        }
        binding.btnApply.click {
            listener.onApply(list)
            dismiss()
        }
    }

    private fun updateList(mediaFile: MediaFile, strategy: ConflictStrategy?) {
        list.find { it.name == mediaFile.name }?.apply {
            conflict = strategy ?: ConflictStrategy.OVERWRITE
        }
    }

    private fun initAdapter() {
        if (oneAdapter == null) {
            oneAdapter = OneAdapter(binding.recycler) {
                itemModules += ConflictItemModule().apply {
                    onSelected = { mediaFile, strategy ->
                        updateList(mediaFile, strategy)
                    }
                }
            }
        }
    }

    private fun initRecyclerLayoutChangeListener() {
        with(binding.recycler) {
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val layoutManager = layoutManager
                val adapter = adapter

                if (layoutManager != null && adapter != null) {
                    val itemCount = adapter.itemCount
                    val height = calculateRecyclerViewHeight(itemCount)
                    val newLayoutParams = window?.attributes
                    newLayoutParams?.height = height
                    window?.attributes = newLayoutParams
                }
            }
        }
    }

    private fun calculateRecyclerViewHeight(itemCount: Int): Int {
        val itemHeightInDp = context.resources.getDimension(com.intuit.sdp.R.dimen._50sdp)
        val minHeightInDp = context.resources.getDimension(com.intuit.sdp.R.dimen._300sdp)
        val maxHeightInDp = context.resources.getDimension(com.intuit.sdp.R.dimen._500sdp)
        val calculatedHeightInDp = itemCount * itemHeightInDp

        return when {
            calculatedHeightInDp < minHeightInDp -> minHeightInDp.toInt()
            calculatedHeightInDp > maxHeightInDp -> maxHeightInDp.toInt()
            else -> calculatedHeightInDp.toInt()
        }
    }

}