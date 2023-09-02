package com.sn.snfilemanager.feature.media.presentation

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.idanatz.oneadapter.OneAdapter
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.getNavigationResult
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.core.util.MimeTypes
import com.sn.snfilemanager.databinding.FragmentMediaBinding
import com.sn.snfilemanager.feature.media.module.*
import com.sn.snfilemanager.feature.sheet.FilterBottomSheet
import com.sn.snfilemanager.feature.sheet.SearchBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaFragment : BaseFragment<FragmentMediaBinding, MediaViewModel>(),
    MediaSelectionModule.Selection {

    private var oneAdapter: OneAdapter? = null
    private val args: MediaFragmentArgs by navArgs()

    override fun getViewModelClass() = MediaViewModel::class.java

    override fun getViewBinding() = FragmentMediaBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = true

    override fun getMenuResId() = if (args.isApkFile) R.menu.menu_base else R.menu.menu_media

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setArguments(args)
        viewModel.getMedia()
    }

    override fun setupViews() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        initAdapter()
        initOperationsMenuClicks()
        handleBackPressed()
        handlePathSelected()
    }

    override fun observeData() {
        observe(viewModel.getMediaLiveData) { data ->
            oneAdapter?.setItems(data)
        }
        observe(viewModel.deleteMediaLiveData) { result ->
            if (result != null) {
                oneAdapter?.remove(result)
                clearSelection()
                updateMenusOnSelection(false)
            }
        }
        observe(viewModel.moveMediaLiveData) { result ->
            oneAdapter?.remove(result)
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

    private fun handlePathSelected() {
        getNavigationResult("path")?.observe(viewLifecycleOwner) { path ->
            viewModel.moveMedia(path)
        }
    }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                oneAdapter?.modules?.itemSelectionModule?.actions?.let { action ->
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
            b.setOnClickListener {
                updateMenusOnSelection(false)
                oneAdapter?.modules?.itemSelectionModule?.actions?.clearSelection()
                navigatePathSelection()
            }
        }
    }

    private fun navigatePathSelection() {
        navigate(MediaFragmentDirections.actionMediaPicker())
    }


    private fun clearSelection() {
        oneAdapter?.modules?.itemSelectionModule?.actions?.clearSelection()
        viewModel.clearSelectionList()
    }


    private fun updateMenusOnSelection(isSelectionActive: Boolean) {
        getToolbar()?.menu?.apply {
            findItem(R.id.action_search)?.isVisible = !isSelectionActive
            findItem(R.id.action_filter)?.isVisible = !isSelectionActive
            findItem(R.id.action_cancel)?.isVisible = isSelectionActive
        }
        if (isSelectionActive) {
            binding.bottomOperationsMenu.visible()
        } else {
            binding.bottomOperationsMenu.gone()
        }
    }

    private fun updateToolbarTitleOnSelection(selectedCount: Int) {
        val selectionModule = oneAdapter?.modules?.itemSelectionModule
        getToolbar()?.title = selectionModule?.actions?.run {
            if (isSelectionActive()) {
                getString(R.string.selected_count, selectedCount)
            } else {
                getString(R.string.app_name)
            }
        }
    }

    private fun showFilterBottomSheet() {
        getMimeByMediaType()?.let { type ->
            FilterBottomSheet.newInstance(type).apply {
                onFilterApplyClick = { filters ->
                    viewModel.applyFilter(filters)
                }
            }.show(childFragmentManager, FilterBottomSheet.TAG)
        }
    }

    private fun getMimeByMediaType() =
        when (args.mediaType) {
            MediaType.IMAGES -> MimeTypes.IMAGES
            MediaType.VIDEOS -> MimeTypes.VIDEOS
            MediaType.AUDIOS -> MimeTypes.AUDIOS
            MediaType.FILES -> if (args.isApkFile.not()) MimeTypes.DOCUMENT else null
            else -> null
        }

    private fun showSearchBottomSheet() {
        SearchBottomSheet().apply {
            onSearchClick = { query ->
                viewModel.searchMedia(query)
            }
        }.show(
            childFragmentManager,
            SearchBottomSheet.TAG
        )
    }

    private fun getItemModule() =
        when (args.mediaType) {
            MediaType.IMAGES -> ImageItemModule().apply {
                onSelected = { model, _ -> viewModel.addSelectedItem(model) }
            }
            MediaType.VIDEOS -> VideoItemModule().apply {
                onSelected = { model, _ -> viewModel.addSelectedItem(model) }
            }
            MediaType.AUDIOS -> AudioItemModule().apply {
                onSelected = { model, _ -> viewModel.addSelectedItem(model) }
            }
            MediaType.FILES -> DocumentItemModule().apply {
                onSelected = { model, _ -> viewModel.addSelectedItem(model) }
            }
            else -> null
        }

    private fun initAdapter() {
        if (oneAdapter == null) {
            oneAdapter = OneAdapter(binding.recyclerView) {
                itemModules += getItemModule()!! //Todo null case module
                itemSelectionModule = MediaSelectionModule().apply {
                    selection = this@MediaFragment
                }
            }
        }
    }
}