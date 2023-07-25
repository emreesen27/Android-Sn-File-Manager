package com.sn.snfilemanager

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.idanatz.oneadapter.OneAdapter
import com.sn.snfilemanager.databinding.BottomSheetSearchBinding
import com.sn.snfilemanager.extensions.gone
import com.sn.snfilemanager.extensions.hideKeyboard
import com.sn.snfilemanager.extensions.observe
import com.sn.snfilemanager.extensions.visible
import com.sn.snfilemanager.feature.images.presentation.ImageListViewModel
import com.sn.snfilemanager.feature.images.presentation.SearchImageItemModule

class SearchBottomSheet : BottomSheetDialogFragment() {

    private val imageListViewModel: ImageListViewModel by viewModels({ requireParentFragment() })

    private val binding: BottomSheetSearchBinding by lazy {
        BottomSheetSearchBinding.inflate(layoutInflater)
    }

    private lateinit var oneAdapter: OneAdapter

    var onSearchClick: ((String) -> Unit)? = null

    companion object {
        const val TAG = "SearchBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = imageListViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        initAdapter()
        initSearchListener()
        observeData()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        imageListViewModel.clearSearchImagesResult()
    }


    private fun observeData() {
        observe(imageListViewModel.searchImagesLiveData) { imageList ->
            imageList?.let { oneAdapter.setItems(it) }
        }
    }

    private fun initAdapter() {
        oneAdapter = OneAdapter(binding.rvImageSearch) {
            itemModules += SearchImageItemModule().apply {
                selected = null
            }
        }
    }


    private fun initSearchListener() {
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                imageListViewModel.searchImage(query)
                binding.svSearch.hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.rvImageSearch.gone()
                    imageListViewModel.clearSearchImagesResult()
                } else {
                    binding.rvImageSearch.visible()
                    imageListViewModel.searchImage(newText)
                }
                return true
            }
        })
    }


}