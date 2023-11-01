package com.sn.snfilemanager.feature.search.presentation

import androidx.appcompat.widget.SearchView
import com.idanatz.oneadapter.OneAdapter
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.hideKeyboard
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.databinding.FragmentSearchBinding
import com.sn.snfilemanager.feature.media.presentation.MediaViewModel
import com.sn.snfilemanager.feature.search.module.SearchMediaItemModule
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding, MediaViewModel>() {

    private var oneAdapter: OneAdapter? = null

    override var useSharedViewModel: Boolean = true

    override fun getViewModelClass() = MediaViewModel::class.java

    override fun getViewBinding() = FragmentSearchBinding.inflate(layoutInflater)

    override fun getActionBarStatus() = false

    override fun setupViews() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        initSearchListener()
        initAdapter()
    }

    override fun observeData() {
        observe(viewModel.searchMediaLiveData) { imageList ->
            imageList?.let { oneAdapter?.setItems(it) }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearSearchMediaResult()
    }


    private fun initSearchListener() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.search.hideKeyboard()
                viewModel.searchMedia(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.recycler.gone()
                    viewModel.clearSearchMediaResult()
                } else {
                    binding.recycler.visible()
                    viewModel.searchMedia(newText)
                }
                return true
            }
        })
    }

    private fun initAdapter() {
        if (oneAdapter == null) {
            oneAdapter = OneAdapter(binding.recycler) {
                itemModules += SearchMediaItemModule()
            }
        }
    }
}