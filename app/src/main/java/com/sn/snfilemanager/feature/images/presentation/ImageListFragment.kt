package com.sn.snfilemanager.feature.images.presentation

import androidx.activity.OnBackPressedCallback
import com.idanatz.oneadapter.OneAdapter
import com.sn.snfilemanager.FilterBottomSheet
import com.sn.snfilemanager.R
import com.sn.snfilemanager.SearchBottomSheet
import com.sn.snfilemanager.core.BaseFragment
import com.sn.snfilemanager.databinding.FragmentImageListBinding
import com.sn.snfilemanager.extensions.gone
import com.sn.snfilemanager.extensions.observe
import com.sn.snfilemanager.extensions.visible
import com.sn.snfilemanager.media.MediaFile
import com.sn.snfilemanager.media.MimeTypes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageListFragment : BaseFragment<FragmentImageListBinding, ImageListViewModel>(),
    ItemSelectionModule.Selection, ImageItemModule.Selected {

    private lateinit var oneAdapter: OneAdapter

    override fun getViewModelClass() = ImageListViewModel::class.java

    override fun getViewBinding() = FragmentImageListBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = true

    override fun getMenuResId() = R.menu.menu_images

    override fun onMenuItemSelected(menuItemId: Int) = when (menuItemId) {
        R.id.action_search -> {
            showSearchBottomSheet()
            true
        }
        R.id.action_cancel -> {
            clearSelection()
            true
        }
        R.id.action_filter -> {
            showFilterBottomSheet()
            true
        }
        else -> super.onMenuItemSelected(menuItemId)
    }

    override fun setupViews() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        initAdapter()
        initOperationsMenuClicks()
        handleBackPressed()
    }

    override fun observeData() {
        observe(viewModel.getImagesLiveData) { data ->
            oneAdapter.setItems(data)
        }
        observe(viewModel.deleteImagesLiveData) { result ->
            if (result != null) {
                oneAdapter.remove(result)
                clearSelection()
                updateMenusOnSelection(false)
            }
        }
    }

    override fun onStartSelection() {
        updateMenusOnSelection(true)
    }

    override fun onUpdateSelection(selectedCount: Int) {
        updateToolbarTitleOnSelection(selectedCount)
    }

    override fun onEndSelection() {
        updateMenusOnSelection(false)
    }

    override fun onSelected(model: MediaFile, selected: Boolean) {
        viewModel.addSelectedItem(model)
    }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                oneAdapter.modules.itemSelectionModule?.actions?.let { action ->
                    if (action.isSelectionActive()) {
                        clearSelection()
                        updateMenusOnSelection(false)
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    //Todo buraya bakılacak menu tasarlanınca
    private fun initOperationsMenuClicks() {
        with(binding) {
            a.setOnClickListener {
                viewModel.deleteMedia()
            }
        }
    }

    private fun clearSelection(): Boolean? =
        oneAdapter.modules.itemSelectionModule?.actions?.clearSelection()


    private fun updateMenusOnSelection(isSelectionActive: Boolean) {
        getToolbar()?.menu?.apply {
            findItem(R.id.action_search)?.isVisible = !isSelectionActive
            findItem(R.id.action_filter)?.isVisible = !isSelectionActive
            findItem(R.id.action_cancel)?.isVisible = isSelectionActive
        }
        if (isSelectionActive)
            binding.bottomOperationsMenu.visible()
        else
            binding.bottomOperationsMenu.gone()
    }

    private fun updateToolbarTitleOnSelection(selectedCount: Int) {
        val selectionModule = oneAdapter.modules.itemSelectionModule
        getToolbar()?.title = selectionModule?.actions?.run {
            if (isSelectionActive()) {
                getString(R.string.selected_count, selectedCount)
            } else {
                getString(R.string.app_name)
            }
        }
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet.newInstance(MimeTypes.IMAGE).apply {
            onFilterApplyClick = { filters ->
                viewModel.applyFilter(filters)
            }
        }.show(childFragmentManager, FilterBottomSheet.TAG)
    }

    private fun showSearchBottomSheet() {
        SearchBottomSheet().apply {
            onSearchClick = { query ->
                viewModel.searchImage(query)
            }
        }.show(
            childFragmentManager,
            SearchBottomSheet.TAG
        )
    }

    private fun initAdapter() {
        oneAdapter = OneAdapter(binding.recyclerView) {
            itemModules += ImageItemModule().apply {
                selected = this@ImageListFragment
            }
            itemSelectionModule = ItemSelectionModule().apply {
                selection = this@ImageListFragment
            }
        }
    }
}