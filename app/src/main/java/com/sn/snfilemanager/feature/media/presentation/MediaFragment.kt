package com.sn.snfilemanager.feature.media.presentation

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.navArgs
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.Media
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.getNavigationResult
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.openFile
import com.sn.snfilemanager.core.extensions.openFileWithOtherApp
import com.sn.snfilemanager.core.extensions.removeKey
import com.sn.snfilemanager.core.extensions.shareFiles
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.databinding.FragmentMediaBinding
import com.sn.snfilemanager.feature.filter.FilterBottomSheet
import com.sn.snfilemanager.feature.media.adapter.MediaItemAdapter
import com.sn.snfilemanager.job.JobService
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobType
import com.sn.snfilemanager.view.dialog.ConfirmationDialog
import com.sn.snfilemanager.view.dialog.ConflictDialog
import com.sn.snfilemanager.view.dialog.detail.DetailDialog
import dagger.hilt.android.AndroidEntryPoint
import java.nio.file.Path
import java.nio.file.Paths


@AndroidEntryPoint
class MediaFragment : BaseFragment<FragmentMediaBinding, MediaViewModel>(),
    MediaItemAdapter.SelectionCallback, JobCompletedCallback {

    private var adapter: MediaItemAdapter? = null
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
                    adapter?.setItems(data)
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
            observe(viewModel.startMoveJobLiveData) { event ->
                event.getContentIfNotHandled()?.let { data ->
                    startCopyService(data.first, data.second)
                }
            }
        }
    }

    override fun onStartSelection() {
        updateMenusOnSelection(true)
    }

    override fun onEndSelection() {
        updateMenusOnSelection(false)
    }

    override fun onUpdateSelection(selectedSize: Int) {
        updateSelection(selectedSize)
    }

    override fun scannedOnCompleted() {
        viewModel.getMedia()

    }

    override fun jobOnCompleted(jobType: JobType) {
        when (jobType) {
            JobType.COPY -> {
                activity?.runOnUiThread {
                    clearSelection()
                }
            }

            JobType.DELETE -> {
                activity?.runOnUiThread {
                    adapter?.removeItems(viewModel.getSelectedItem())
                    clearSelection()
                }
            }
        }
    }

    private fun handlePathSelected() {
        getNavigationResult("path")?.observe(viewLifecycleOwner) { path ->
            viewModel.moveMedia(Paths.get(path))
            adapter?.finishSelectionAndReset()
            removeKey("path")
        }
        getNavigationResult("no_selected")?.observe(viewLifecycleOwner) { msg ->
            clearSelection()
            context?.infoToast(msg)
            removeKey("no_selected")
        }
    }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (selectionIsActive()) {
                    clearSelection()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun initOperationsMenuClicks() {
        with(binding.layoutMenu) {
            tvDelete.click {
                ConfirmationDialog(
                    requireContext(),
                    getString(R.string.are_you_sure),
                    getString(R.string.delete_warning)
                ).apply {
                    onSelected = { selected ->
                        if (selected) {
                            startDeleteService()
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
            tvShare.click {
                val uris = viewModel.getSelectedItem().map { it.uri }
                context?.shareFiles(uris)
            }
            tvMove.click {
                viewModel.isCopy = false
                updateMenusOnSelection(false)
                navigatePathSelection()
            }
            tvMore.click {
                showPopupMenu(it)
            }
        }
    }

    private fun startCopyService(operationItemList: List<Media>, destinationPath: Path) {
        JobService.copyMedia(
            operationItemList,
            destinationPath,
            viewModel.isCopy,
            this@MediaFragment,
            requireContext(),
        )
    }

    private fun startDeleteService() {
        JobService.deleteMedia(
            viewModel.getSelectedItem(),
            this@MediaFragment,
            requireContext()
        )
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.menu_more, popup.menu)

        if (!viewModel.isSingleItemSelected()) {
            popup.menu.removeItem(R.id.open_with)
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.detail -> showDetailDialog()
                R.id.open_with -> openWith()
            }
            true
        }
        popup.setOnDismissListener {}
        popup.show()
    }

    private fun showDetailDialog() {
        DetailDialog(requireContext(), viewModel.getSelectedItem()).show(
            childFragmentManager,
            DetailDialog.TAG
        )
    }

    private fun openWith() {
        viewModel.getSelectedItem().firstOrNull()?.let { selectedItem ->
            context?.openFileWithOtherApp(selectedItem.data, selectedItem.mimeType)
        }
    }

    private fun navigatePathSelection() {
        navigate(MediaFragmentDirections.actionPathPicker())
    }

    private fun clearSelection() {
        adapter?.finishSelectionAndReset()
        viewModel.clearSelectionList()
    }


    private fun updateMenusOnSelection(isSelectionActive: Boolean) {
        setToolbarVisibility(!isSelectionActive)
        setActionMenuVisibility(isSelectionActive)
        if (isSelectionActive) {
            binding.layoutMenu.container.visible()
        } else {
            binding.layoutMenu.container.gone()
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
                    return if (selectionIsActive()) {
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
        viewModel.getMimeByMediaType()?.let { type ->
            FilterBottomSheet.newInstance(type).apply {
                onFilterApplyClick = { filters ->
                    viewModel.applyFilter(filters)
                }
            }.show(childFragmentManager, FilterBottomSheet.TAG)
        }
    }

    private fun openFile(model: Media) {
        if (selectionIsActive().not()) {
            context?.openFile(model.data, model.mimeType)
        }
    }

    private fun selectionIsActive(): Boolean = adapter?.selectionIsActive() ?: false
    private fun initAdapter() {
        if (adapter == null) {
            adapter = MediaItemAdapter(
                onClick = { model -> openFile(model) },
                onSelected = { model, selected -> viewModel.addSelectedItem(model, selected) },
                selectionCallback = this@MediaFragment
            )
        }
        binding.recyclerView.adapter = adapter
    }
}