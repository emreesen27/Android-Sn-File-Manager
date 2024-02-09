package com.sn.snfilemanager.view.dialog.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.databinding.DialogDetailBinding

class DetailDialog<T>(
    private val context: Context,
    private val itemList: MutableList<T>,
) : DialogFragment() {
    private var adapter: DetailItemAdapter? = null
    private val viewModel: DetailDialogViewModel by viewModels()
    private val binding: DialogDetailBinding by lazy {
        DialogDetailBinding.inflate(layoutInflater)
    }

    companion object {
        const val TAG = "DETAIL_DIALOG"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
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
