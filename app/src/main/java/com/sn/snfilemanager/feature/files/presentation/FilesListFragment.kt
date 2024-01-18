package com.sn.snfilemanager.feature.files.presentation

import android.view.View
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.sn.filetaskpv.FileConflictStrategy
import com.sn.mediastorepv.MediaScannerBuilder
import com.sn.mediastorepv.util.MediaScanCallback
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.getMimeType
import com.sn.snfilemanager.core.extensions.getNavigationResult
import com.sn.snfilemanager.core.extensions.getUrisForFile
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.extensions.openFile
import com.sn.snfilemanager.core.extensions.openFileWithOtherApp
import com.sn.snfilemanager.core.extensions.removeKey
import com.sn.snfilemanager.core.extensions.shareFiles
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.databinding.FragmentFilesListBinding
import com.sn.snfilemanager.feature.files.adapter.FileItemAdapter
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.feature.files.data.toFileModel
import com.sn.snfilemanager.view.component.breadcrumb.BreadCrumbItemClickListener
import com.sn.snfilemanager.view.component.breadcrumb.BreadItem
import com.sn.snfilemanager.view.dialog.ConfirmationDialog
import com.sn.snfilemanager.view.dialog.ConflictDialog
import com.sn.snfilemanager.view.dialog.detail.DetailDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.nio.file.Paths

@AndroidEntryPoint
class FilesListFragment : BaseFragment<FragmentFilesListBinding, FilesListViewModel>(),
    FileItemAdapter.SelectionCallback {

    private val args: FilesListFragmentArgs by navArgs()
    private var adapter: FileItemAdapter? = null

    override fun getViewModelClass() = FilesListViewModel::class.java

    override fun getViewBinding() = FragmentFilesListBinding.inflate(layoutInflater)

    override fun getActionBarStatus() = true

    override fun setupViews() {
        initAdapter()
        handleBackPressed()
        handlePathSelected()
        initOperationsMenuClicks()
        initBreadListener()
        initFirstList()
    }

    override fun onStartSelection() {
        updateMenusOnSelection(true)
    }

    override fun onEndSelection() {
        updateMenusOnSelection(false)
    }

    override fun onUpdateSelection(selectedSize: Int) {
        updateActionMenu(getString(R.string.selected_count, selectedSize))
    }

    override fun observeData() {
        observe(viewModel.conflictQuestionLiveData) { event ->
            event.getContentIfNotHandled()?.let { data ->
                ConflictDialog(requireContext(), data.name).apply {
                    onSelected = { strategy: Int, isAll: Boolean ->
                        FileConflictStrategy.getByValue(strategy)?.let {
                            viewModel.conflictDialogDeferred.complete(Pair(it, isAll))
                        }
                    }
                    onDismiss = { clearSelection() }
                }.show()
            }
        }
        observe(viewModel.moveLiveData) { event ->
            event.getContentIfNotHandled()?.let { data ->
                viewModel.currentPath?.let { path ->
                    viewModel.getFilesList(path).map { it.toFileModel() }
                }?.let { adapter?.setItems(it) }
                hideProgressDialog()
                clearSelection()

                buildMediaScanner(data)
            }
        }
        observe(viewModel.progressLiveData) { event ->
            event.getContentIfNotHandled()?.let { progress ->
                updateProgressDialog(progress)
            }
        }
        observe(viewModel.deleteLiveData) { event ->
            event.getContentIfNotHandled()?.let { items ->
                adapter?.removeItems(items)
                clearSelection()
            }
        }
    }

    private fun initFirstList() {
        if (!viewModel.firstInit) {
            updateFileList(viewModel.getStoragePath(args.storageArgs))
            binding.breadcrumbBar.addBreadCrumbItem(
                BreadItem(
                    getString(R.string.internal),
                    null
                )
            )
        }
    }

    private fun updateFileList(path: String) {
        with(viewModel) {
            firstInit = true
            updateDirectoryList(path)
            adapter?.setItems(getFilesList(path).map { it.toFileModel() })
        }
    }

    private fun addPathItem(item: BreadItem) {
        item.path?.let {
            if (File(it).isDirectory) {
                binding.breadcrumbBar.addBreadCrumbItem(item)
            }
        }
    }

    private fun initBreadListener() {
        binding.breadcrumbBar.setListener(object : BreadCrumbItemClickListener {
            override fun onItemClick(item: BreadItem, position: Int) {
                val path =
                    if (position == 0 || item.path == null) viewModel.getStoragePath(args.storageArgs) else item.path
                updateFileList(path)
                binding.breadcrumbBar.removeItemsWithRange(position)
                viewModel.updateDirectoryListWithPos(position)
            }
        })
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

    private fun initOperationsMenuClicks() {
        with(binding.layoutMenu) {
            tvMove.click {
                viewModel.isCopy = false
                updateMenusOnSelection(false)
                navigatePathSelection()
            }
            tvCopy.click {
                viewModel.isCopy = true
                updateMenusOnSelection(false)
                navigatePathSelection()
            }
            tvDelete.click {
                ConfirmationDialog(
                    requireContext(),
                    getString(R.string.are_you_sure),
                    getString(R.string.delete_warning)
                ).apply {
                    onSelected = { selected ->
                        if (selected) {
                            viewModel.deleteFilesAndDirectories()
                        } else {
                            clearSelection()
                        }
                    }
                }.show()
            }
            tvShare.click {
                val files = viewModel.getSelectedItemToFiles()
                val uris = context?.getUrisForFile(files)
                uris?.let {
                    context?.shareFiles(it)
                }
            }
            tvMore.click {
                showPopupMenu(it)
            }
        }
    }

    private fun setShareStatus() {
        if (selectionIsActive()) {
            if (viewModel.selectedItemsContainsFolder()) {
                binding.layoutMenu.tvShare.gone()
            } else {
                binding.layoutMenu.tvShare.visible()
            }
        }
    }

    private fun initAdapter() {
        if (adapter == null) {
            adapter = FileItemAdapter(
                requireContext(),
                onSelected = { model, selected ->
                    viewModel.addSelectedItem(model, selected)
                    setShareStatus()
                },
                onClick = { model ->
                    if (model.isDirectory) {
                        updateFileList(model.absolutePath)
                        addPathItem(BreadItem(model.name, model.absolutePath))
                    } else {
                        openFile(model)
                    }
                },
                selectionCallback = this@FilesListFragment
            )
        }
        binding.rcvFiles.adapter = adapter
        binding.rcvFiles.itemAnimator = null
    }

    private fun clearSelection() {
        adapter?.finishSelectionAndReset()
        viewModel.clearSelectionList()
    }

    private fun openFile(file: FileModel) {
        file.absolutePath.getMimeType()?.let { mime ->
            context?.openFile(file.absolutePath, mime)
        }
    }

    private fun navigatePathSelection() {
        navigate(FilesListFragmentDirections.actionPathPicker())
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.menu_more, popup.menu)

        if (!viewModel.isSingleItemSelected() || viewModel.selectedItemsContainsFolder()) {
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
            selectedItem.absolutePath.getMimeType()
                ?.let { context?.openFileWithOtherApp(selectedItem.absolutePath, it) }
        }
    }

    private fun handlePathSelected() {
        getNavigationResult("path")?.observe(viewLifecycleOwner) { path ->
            viewModel.moveFilesAndDirectories(Paths.get(path))
            removeKey("path")
        }
        getNavigationResult("no_selected")?.observe(viewLifecycleOwner) { msg ->
            clearSelection()
            context?.infoToast(msg)
            removeKey("no_selected")
        }
    }

    private fun buildMediaScanner(mediaList: List<Pair<String, String?>>) {
        MediaScannerBuilder()
            .addContext(requireContext())
            .addMediaList(mediaList)
            .addCallback(object : MediaScanCallback {
                override fun onMediaScanned(filePath: String) {
                    println("media scanned${filePath}")
                }
            }).build().scanMediaFiles()
    }

    private fun selectionIsActive(): Boolean = adapter?.selectionIsActive() ?: false

    private fun handleBackPressed() {
        val directoryList = viewModel.getDirectoryList()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                adapter?.selectionIsActive()?.let { isActive ->
                    if (isActive) {
                        clearSelection()
                    } else {
                        if (directoryList.size > 1) {
                            directoryList.removeAt(directoryList.lastIndex)
                            updateFileList(directoryList.last())
                            binding.breadcrumbBar.removeLastBreadCrumbItem()
                        } else {
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}
