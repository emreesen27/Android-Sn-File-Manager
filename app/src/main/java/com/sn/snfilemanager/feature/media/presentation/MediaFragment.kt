package com.sn.snfilemanager.feature.media.presentation

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.navArgs
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.Media
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.getNavigationResult
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.openFile
import com.sn.snfilemanager.core.extensions.openFileWithOtherApp
import com.sn.snfilemanager.core.extensions.removeKey
import com.sn.snfilemanager.core.extensions.shareFiles
import com.sn.snfilemanager.core.extensions.warningToast
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.databinding.FragmentMediaBinding
import com.sn.snfilemanager.feature.filter.FilterBottomSheet
import com.sn.snfilemanager.feature.media.adapter.MediaItemAdapter
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobService
import com.sn.snfilemanager.job.JobType
import com.sn.snfilemanager.view.dialog.ConfirmationDialog
import com.sn.snfilemanager.view.dialog.ConflictDialog
import com.sn.snfilemanager.view.dialog.detail.DetailDialog
import dagger.hilt.android.AndroidEntryPoint
import java.nio.file.Path
import java.nio.file.Paths

@AndroidEntryPoint
class MediaFragment :
    BaseFragment<FragmentMediaBinding, MediaViewModel>(),
    MediaItemAdapter.SelectionCallback,
    JobCompletedCallback,
    ActionMode.Callback {
    private var adapter: MediaItemAdapter? = null
    private val args: MediaFragmentArgs by navArgs()
    private var actionMode: ActionMode? = null

    override var useSharedViewModel: Boolean = true

    override fun getViewModelClass() = MediaViewModel::class.java

    override fun getViewBinding() = FragmentMediaBinding.inflate(layoutInflater)

    override fun getMenuResId() = if (args.documentType == DocumentType.APK.name) R.menu.menu_base else R.menu.menu_media

    override fun getToolbar() = binding.toolbar

    override fun onMenuItemSelected(menuItemId: Int) =
        when (menuItemId) {
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

    override fun onCreateActionMode(
        mode: ActionMode?,
        menu: Menu?,
    ): Boolean {
        actionMode = mode
        mode?.menuInflater?.inflate(R.menu.menu_action, menu)
        activity?.actionBar?.setDisplayHomeAsUpEnabled(false)
        return true
    }

    override fun onPrepareActionMode(
        mode: ActionMode?,
        menu: Menu?,
    ): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = null
        clearSelection()
    }

    override fun onActionItemClicked(
        mode: ActionMode?,
        item: MenuItem?,
    ): Boolean {
        when (item?.itemId) {
            R.id.action_copy -> {
                viewModel.isCopy = true
                actionMode?.finish()
                navigatePathSelection()
            }

            R.id.action_delete -> {
                actionMode?.finish()
                actionDelete()
            }

            R.id.action_move -> {
                viewModel.isCopy = false
                actionMode?.finish()
                navigatePathSelection()
            }

            R.id.action_share -> {
                actionShare()
            }

            R.id.action_open_with -> {
                actionOpenWith()
            }

            R.id.action_detail -> {
                actionDetail()
            }
        }

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setArguments(args)
        viewModel.getMedia()
        setTitle()
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearFilteredList()
    }

    override fun setupViews() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        initAdapter()
        handleBackPressed()
        handlePathSelected()
    }

    override fun onStartSelection() {
        activity?.startActionMode(this)
    }

    override fun onEndSelection() {
        actionMode?.finish()
    }

    override fun onUpdateSelection(selectedSize: Int) {
        actionMode?.title = selectedSize.toString()
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

    override fun observeData() {
        viewModel.run {
            observe(getMediaLiveData) { event ->
                event.getContentIfNotHandled()?.let { data ->
                    adapter?.setItems(data)
                }
            }
            observe(conflictQuestionLiveData) { event ->
                event.getContentIfNotHandled()?.let { mediaName ->
                    ConflictDialog(requireContext(), mediaName).apply {
                        onSelected = { strategy: ConflictStrategy, isAll: Boolean ->
                            viewModel.conflictDialogDeferred.complete(Pair(strategy, isAll))
                        }
                        onDismiss = { clearSelection() }
                    }.show()
                }
            }
            observe(viewModel.startMoveJobLiveData) { event ->
                event.getContentIfNotHandled()?.let { data ->
                    if (data.first.isNotEmpty()) {
                        startCopyService(data.first, data.second)
                    }
                }
            }
            observe(viewModel.pathConflictLiveData) { event ->
                event.getContentIfNotHandled()?.let {
                    context?.warningToast(getString(R.string.path_conflict_warning))
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
        val callback =
            object : OnBackPressedCallback(true) {
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

    private fun actionShare() {
        val uris = viewModel.getSelectedItem().map { it.uri }
        context?.shareFiles(uris)
    }

    private fun actionDelete() {
        ConfirmationDialog(
            requireContext(),
            getString(R.string.are_you_sure),
            getString(R.string.delete_warning),
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

    private fun startCopyService(
        operationItemList: List<Media>,
        destinationPath: Path,
    ) {
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
            requireContext(),
        )
    }

    private fun actionDetail() {
        DetailDialog(requireContext(), viewModel.getSelectedItem()).show(
            childFragmentManager,
            DetailDialog.TAG,
        )
    }

    private fun actionOpenWith() {
        viewModel.getSelectedItem().firstOrNull()?.let { selectedItem ->
            context?.openFileWithOtherApp(selectedItem.data, selectedItem.mimeType)
        }
    }

    private fun checkActionMenuStatus() {
        actionMode?.menu?.findItem(R.id.action_open_with)?.isVisible =
            viewModel.isSingleItemSelected()
    }

    private fun navigatePathSelection() {
        navigate(MediaFragmentDirections.actionPathPicker())
    }

    private fun clearSelection() {
        adapter?.finishSelectionAndReset()
        viewModel.clearSelectionList()
    }

    private fun initSearch() {
        binding.toolbar.menu?.findItem(R.id.action_search)?.let { item ->
            val searchView = item.actionView as? SearchView
            searchView?.setOnQueryTextListener(
                object :
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.let { viewModel.searchMedia(it) }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchMedia(newText)
                        return true
                    }
                },
            )

            item.setOnActionExpandListener(
                object : MenuItem.OnActionExpandListener {
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
                },
            )
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

    private fun setTitle() {
        binding.toolbar.title =
            when (args.mediaType) {
                MediaType.IMAGES -> getString(R.string.images)
                MediaType.VIDEOS -> getString(R.string.videos)
                MediaType.AUDIOS -> getString(R.string.sounds)
                MediaType.FILES ->
                    if (args.documentType != null && args.documentType == DocumentType.APK.name) {
                        getString(
                            R.string.apk_files,
                        )
                    } else {
                        getString(R.string.archives)
                    }

                else -> getString(R.string.app_name)
            }
    }

    private fun initAdapter() {
        if (adapter == null) {
            adapter =
                MediaItemAdapter(
                    onClick = { model -> openFile(model) },
                    onSelected = { model, selected ->
                        checkActionMenuStatus()
                        viewModel.addSelectedItem(model, selected)
                    },
                    selectionCallback = this@MediaFragment,
                )
        }
        binding.recyclerView.adapter = adapter
    }
}
