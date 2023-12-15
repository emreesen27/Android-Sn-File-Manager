package com.sn.snfilemanager.feature.media.presentation

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.idanatz.oneadapter.OneAdapter
import com.sn.mediastorepv.MediaScannerBuilder
import com.sn.mediastorepv.data.MediaType
import com.sn.mediastorepv.util.MediaScanCallback
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.*
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.core.util.MimeTypes
import com.sn.snfilemanager.databinding.FragmentMediaBinding
import com.sn.snfilemanager.feature.conflict.ConflictDialog
import com.sn.snfilemanager.feature.conflict.ConflictDialogListener
import com.sn.snfilemanager.feature.media.module.*
import com.sn.snfilemanager.feature.filter.FilterBottomSheet
import com.sn.snfilemanager.providers.mediastore.MediaFile
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaFragment : BaseFragment<FragmentMediaBinding, MediaViewModel>(),
    MediaSelectionModule.Selection, ConflictDialogListener {

    private var oneAdapter: OneAdapter? = null
    private val args: MediaFragmentArgs by navArgs()

    override var useSharedViewModel: Boolean = true
    override fun getViewModelClass() = MediaViewModel::class.java

    override fun getViewBinding() = FragmentMediaBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = true

    override fun getMenuResId() =
        if (args.documentType == DocumentType.APK.name) R.menu.menu_base else R.menu.menu_media

    override fun onMenuItemSelected(menuItemId: Int) = when (menuItemId) {
        R.id.action_search -> {
            //showSearchBottomSheet()
            navigate(MediaFragmentDirections.actionMediaSearch())
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
        viewModel.run {
            observe(getMediaLiveData) { event ->
                event.getContentIfNotHandled()?.let { data ->
                    oneAdapter?.setItems(data)
                }
            }
            observe(deleteMediaLiveData) { result ->
                if (result != null) {
                    oneAdapter?.remove(result)
                    clearSelection()
                    updateMenusOnSelection(false)
                }
            }
            observe(moveMediaLiveData) { event ->
                event.getContentIfNotHandled()?.let { mediaList ->
                    buildMediaScanner(mediaList)
                }
            }
            observe(conflictMediaLiveData) { event ->
                event.getContentIfNotHandled()?.let { list ->
                    ConflictDialog(requireContext(), list, this@MediaFragment).apply {
                    }.also {
                        it.show()
                    }
                }
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

    override fun onApply(newList: MutableList<MediaFile>) {
        with(viewModel) {
            updateSelectionList(newList)
            moveMedia()
        }
    }

    override fun onCancel() {
        with(viewModel) {
            clearConflictList()
            clearSelectionList()
        }
    }

    private fun handlePathSelected() {
        getNavigationResult("path")?.observe(viewLifecycleOwner) { path ->
            with(viewModel) {
                selectedPath = path
                moveMedia(true)
            }
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

    private fun buildMediaScanner(mediaList: List<Pair<String, String>>) {
        MediaScannerBuilder()
            .addContext(requireContext())
            .addMediaList(mediaList)
            .addCallback(object : MediaScanCallback {
                override fun onMediaScanned(filePath: String) {
                    viewModel.getMedia()
                }
            }).build().scanMediaFiles()
    }

    private fun initOperationsMenuClicks() {
        with(binding) {
            tvDelete.click { viewModel.deleteMedia() }
            tvCopy.click { /* todo*/ }
            tvShare.click { /* todo*/ }
            tvMove.click {
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
            MediaType.FILES -> if (args.documentType == DocumentType.ARCHIVE.name) MimeTypes.ARCHIVES else MimeTypes.DOCUMENTS
            else -> null
        }

    private fun getItemModule() =
        when (args.mediaType) {
            MediaType.IMAGES -> ImageItemModule().apply {
                onSelected = { model, selected -> viewModel.addSelectedItem(model, selected) }
            }

            MediaType.VIDEOS -> VideoItemModule().apply {
                onSelected = { model, selected -> viewModel.addSelectedItem(model, selected) }
            }

            MediaType.AUDIOS -> AudioItemModule().apply {
                onSelected = { model, selected -> viewModel.addSelectedItem(model, selected) }
            }

            MediaType.FILES -> DocumentItemModule().apply {
                onSelected = { model, selected -> viewModel.addSelectedItem(model, selected) }
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