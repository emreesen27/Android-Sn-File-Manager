package com.sn.snfilemanager.feature.media.presentation

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.navArgs
import com.idanatz.oneadapter.OneAdapter
import com.sn.mediastorepv.MediaScannerBuilder
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.MediaType
import com.sn.mediastorepv.util.MediaScanCallback
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.getNavigationResult
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.core.util.MimeTypes
import com.sn.snfilemanager.databinding.FragmentMediaBinding
import com.sn.snfilemanager.feature.filter.FilterBottomSheet
import com.sn.snfilemanager.feature.media.module.AudioItemModule
import com.sn.snfilemanager.feature.media.module.DocumentItemModule
import com.sn.snfilemanager.feature.media.module.ImageItemModule
import com.sn.snfilemanager.feature.media.module.MediaSelectionModule
import com.sn.snfilemanager.feature.media.module.VideoItemModule
import com.sn.snfilemanager.view.dialog.ConfirmationDialog
import com.sn.snfilemanager.view.dialog.ConflictDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaFragment : BaseFragment<FragmentMediaBinding, MediaViewModel>(),
    MediaSelectionModule.Selection {

    private var oneAdapter: OneAdapter? = null
    private val args: MediaFragmentArgs by navArgs()

    override var useSharedViewModel: Boolean = true
    override fun getViewModelClass() = MediaViewModel::class.java

    override fun getViewBinding() = FragmentMediaBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = true

    override fun getMenuResId() =
        if (args.documentType == DocumentType.APK.name) R.menu.menu_base else R.menu.menu_media

    override fun onMenuItemSelected(menuItemId: Int) = when (menuItemId) {
        R.id.action_filter -> {
            showFilterBottomSheet()
            true
        }

        R.id.action_search -> {
            initSearch()
            true
        }

        else -> super.onMenuItemSelected(menuItemId)
    }

    override var actionCancelCLick: (() -> Unit)? = {
        clearSelection()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setArguments(args)
        viewModel.getMedia()
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearFilteredList()
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
            observe(deleteMediaLiveData) { event ->
                event.getContentIfNotHandled()?.let { result ->
                    oneAdapter?.remove(result)
                    clearSelection()
                }
            }
            observe(moveMediaLiveData) { event ->
                event.getContentIfNotHandled()?.let { mediaList ->
                    oneAdapter?.modules?.itemSelectionModule?.actions?.clearSelection()
                    buildMediaScanner(mediaList)
                    hideProgressDialog()
                }
            }
            observe(conflictQuestionLiveData) { event ->
                event.getContentIfNotHandled()?.let { file ->
                    ConflictDialog(requireContext(), file.name).apply {
                        onSelected = { strategy: ConflictStrategy, isAll: Boolean ->
                            viewModel.conflictDialogDeferred.complete(Pair(strategy, isAll))
                        }
                        onDismiss = { clearSelection() }
                    }.show()
                }
            }
            observe(clearListLiveData) { event ->
                event.getContentIfNotHandled()?.let {
                    oneAdapter?.modules?.itemSelectionModule?.actions?.clearSelection()
                }
            }
            observe(progressLiveData) { event ->
                event.getContentIfNotHandled()?.let {
                    updateProgressDialog(it)
                }
            }
        }
    }

    override fun onStartSelection() {
        updateMenusOnSelection(true)
    }

    override fun onUpdateSelection(selectedCount: Int) {
        updateSelection(selectedCount)
    }

    override fun onEndSelection() {
        updateMenusOnSelection(false)
    }


    private fun handlePathSelected() {
        getNavigationResult("path")?.observe(viewLifecycleOwner) { path ->
            with(viewModel) {
                selectedPath = path
                moveMedia()
                oneAdapter?.modules?.itemSelectionModule?.actions?.clearSelection()
            }
        }
        getNavigationResult("no_selected")?.observe(viewLifecycleOwner) { msg ->
            clearSelection()
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                oneAdapter?.modules?.itemSelectionModule?.actions?.let { action ->
                    if (action.isSelectionActive()) {
                        clearSelection()
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
            tvDelete.click {
                ConfirmationDialog(
                    requireContext(),
                    getString(R.string.are_you_sure),
                    getString(R.string.delete_warning)
                ).apply {
                    onSelected = { selected ->
                        if (selected) {
                            viewModel.deleteMedia()
                        } else {
                            clearSelection()
                        }
                    }
                }.show()
            }
            tvCopy.click {
                viewModel.isCopy = true
                updateMenusOnSelection(false)
                navigatePathSelection()
            }
            tvShare.click { /* todo*/ }
            tvMove.click {
                viewModel.isCopy = false
                updateMenusOnSelection(false)
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
        setToolbarVisibility(!isSelectionActive)
        setActionMenuVisibility(isSelectionActive)
        if (isSelectionActive) {
            binding.bottomOperationsMenu.visible()
        } else {
            binding.bottomOperationsMenu.gone()
        }
    }

    private fun updateSelection(value: Int) {
        updateActionMenu(getString(R.string.selected_count, value))
    }

    private fun initSearch() {
        getToolbar()?.menu?.findItem(R.id.action_search)?.let { item ->
            val searchView = item.actionView as? SearchView
            searchView?.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.searchMedia(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.searchMedia(newText)
                    return true
                }
            })

            item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                    val action = oneAdapter?.modules?.itemSelectionModule?.actions
                    return if (action?.isSelectionActive() == true) {
                        clearSelection()
                        false
                    } else {
                        true
                    }
                }
            })
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