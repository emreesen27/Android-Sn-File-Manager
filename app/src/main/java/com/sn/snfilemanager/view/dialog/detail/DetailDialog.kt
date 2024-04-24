package com.sn.snfilemanager.view.dialog.detail

import android.content.Context
import androidx.fragment.app.viewModels
import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.databinding.DialogDetailBinding

class DetailDialog<T>(
    private val context: Context,
    private val itemList: MutableList<T>,
) : BaseDialog<DialogDetailBinding>() {
    private val viewModel: DetailDialogViewModel by viewModels()
    private var adapter: DetailItemAdapter? = null

    override fun getViewBinding() = DialogDetailBinding.inflate(layoutInflater)

    override val dialogTag: String
        get() = "DETAIL_DIALOG"

    override fun setupViews() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = true
        initAdapter()
        initObserve()
        binding.tvOk.click { dismiss() }
        viewModel.createDetailItem(itemList)
    }

    private fun initObserve() {
        observe(viewModel.detailItemLiveData) {
            adapter?.setItems(it)
        }
    }

    private fun initAdapter() {
        adapter = DetailItemAdapter(context)
        binding.recyclerDetail.adapter = adapter
    }
}
