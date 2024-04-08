package com.sn.snfilemanager.feature.media.presentation

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.navArgs
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.Media
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.openFile
import com.sn.snfilemanager.core.extensions.openFileWithOtherApp
import com.sn.snfilemanager.core.extensions.shareFiles
import com.sn.snfilemanager.core.extensions.warningToast
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.databinding.FragmentMediaBinding
import com.sn.snfilemanager.feature.filter.FilterBottomSheet
import com.sn.snfilemanager.feature.media.adapter.MediaItemAdapter
import com.sn.snfilemanager.feature.pathpicker.presentation.PathPickerFragment
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
        checkActionMenuStatus()
        when (item?.itemId) {
            R.id.action_copy -> {
                viewModel.isCopy = true
                showPathSelectionDialog()
            }

            R.id.action_move -> {
                viewModel.isCopy = false
                showPathSelectionDialog()
            }

            R.id.action_delete -> {
                actionDelete()
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

            R.id.action_select_all -> {
                adapter?.selectAll()
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

    override fun <T> jobOnCompleted(
        jobType: JobType,
        data: List<T>?,
    ) {
        when (jobType) {
            JobType.COPY -> {}

            JobType.DELETE -> {
                activity?.runOnUiThread {
                    data?.filterIsInstance<Media>()?.let { adapter?.removeItems(it) }
                }
            }
            JobType.CREATE -> {}
        }
        activity?.runOnUiThread { context?.infoToast(getString(R.string.completed)) }
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
                        onDismiss = { actionMode?.finish() }
                    }.show()
                }
            }
            observe(viewModel.startMoveJobLiveData) { event ->
                event.getContentIfNotHandled()?.let { data ->
                    if (data.first.isNotEmpty()) {
                        actionMode?.finish()
                        startCopyService(data.first, data.second)
                    }
                }
            }
            observe(viewModel.startDeleteJobLiveData) { event ->
                event.getContentIfNotHandled()?.let { data ->
                    actionMode?.finish()
                    startDeleteService(data)
                }
            }
            observe(viewModel.pathConflictLiveData) { event ->
                event.getContentIfNotHandled()?.let {
                    context?.warningToast(getString(R.string.path_conflict_warning))
                }
            }
        }
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
                    viewModel.deleteMedia()
                } else {
                    actionMode?.finish()
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

    private fun startDeleteService(operationItemList: List<Media>) {
        JobService.deleteMedia(
            operationItemList,
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

    private fun showPathSelectionDialog() {
        PathPickerFragment(pathCallback = { path ->
            if (path.isNullOrEmpty()) {
                actionMode?.finish()
                context?.infoToast(getString(R.string.path_not_selected))
            } else {
                viewModel.moveMedia(Paths.get(path))
            }
        }).show(
            childFragmentManager,
            DetailDialog.TAG,
        )
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
                            actionMode?.finish()
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
                        viewModel.addSelectedItem(model, selected)
                        checkActionMenuStatus()
                    },
                    selectionCallback = this@MediaFragment,
                )
        }
        binding.recyclerView.adapter = adapter
    }
}
