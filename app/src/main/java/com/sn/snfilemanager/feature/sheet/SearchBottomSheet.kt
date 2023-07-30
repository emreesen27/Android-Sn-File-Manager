package com.sn.snfilemanager.feature.sheet

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
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.BottomSheetSearchBinding
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.hideKeyboard
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.feature.media.presentation.MediaViewModel
import com.sn.snfilemanager.feature.media.module.SearchMediaItemModule

class SearchBottomSheet : BottomSheetDialogFragment() {

    private val mediaViewModel: MediaViewModel by viewModels({ requireParentFragment() })

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
        binding.vm = mediaViewModel
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
        mediaViewModel.clearSearchMediaResult()
    }


    private fun observeData() {
        observe(mediaViewModel.searchMediaLiveData) { imageList ->
            imageList?.let { oneAdapter.setItems(it) }
        }
    }

    private fun initAdapter() {
        oneAdapter = OneAdapter(binding.rvImageSearch) {
            itemModules += SearchMediaItemModule().apply {
                selected = null
            }
        }
    }


    private fun initSearchListener() {
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mediaViewModel.searchMedia(query)
                binding.svSearch.hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.rvImageSearch.gone()
                    mediaViewModel.clearSearchMediaResult()
                } else {
                    binding.rvImageSearch.visible()
                    mediaViewModel.searchMedia(newText)
                }
                return true
            }
        })
    }


}