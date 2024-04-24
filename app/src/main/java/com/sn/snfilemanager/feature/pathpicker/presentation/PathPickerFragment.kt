package com.sn.snfilemanager.feature.pathpicker.presentation

import android.view.KeyEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.databinding.FragmentPathPickerBinding
import com.sn.snfilemanager.feature.files.data.toFileModel
import com.sn.snfilemanager.feature.pathpicker.adapter.DirectoryItemAdapter
import com.sn.snfilemanager.view.component.breadcrumb.BreadCrumbItemClickListener
import com.sn.snfilemanager.view.component.breadcrumb.BreadItem
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PathPickerFragment(private val pathCallback: ((String?) -> Unit)? = null) :
    BaseDialog<FragmentPathPickerBinding>() {
    private var adapter: DirectoryItemAdapter? = null
    private val viewModel: PathPickerViewModel by viewModels()

    override fun getViewBinding() = FragmentPathPickerBinding.inflate(layoutInflater)

    override val dialogTag: String
        get() = "PATH_PICKER_DIALOG"

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
        )
    }

    override fun setupViews() {
        initAdapter()
        handleBackPressed()
        initBreadListener()
        initFirstList()
        initButtonClick()
    }

    private fun initFirstList() {
        binding.breadcrumbBar.addBreadCrumbItem(BreadItem(getString(R.string.internal), null))
        updateList(viewModel.getStoragePath(RootPath.INTERNAL))
    }

    private fun updateList(directoryPath: String) {
        with(viewModel) {
            currentPath = directoryPath
            updateDirectoryList(directoryPath)
            val item = getDirectoryList(directoryPath).map { it.toFileModel() }
            if (item.isEmpty()) binding.tvEmpty.visible() else binding.tvEmpty.gone()
            adapter?.setItems(item)
        }
    }

    private fun initAdapter() {
        adapter =
            DirectoryItemAdapter(onClick = { file ->
                if (file.isDirectory) {
                    updateList(file.absolutePath)
                    addPathItem(BreadItem(file.name, file.absolutePath))
                }
            })
        binding.recycler.adapter = adapter
    }

    private fun addPathItem(item: BreadItem) {
        item.path?.let {
            if (File(it).isDirectory) {
                binding.breadcrumbBar.addBreadCrumbItem(item)
            }
        }
    }

    private fun initBreadListener() {
        binding.breadcrumbBar.setListener(
            object : BreadCrumbItemClickListener {
                override fun onItemClick(
                    item: BreadItem,
                    position: Int,
                ) {
                    val path =
                        if (position == 0 || item.path == null) {
                            viewModel.getStoragePath(RootPath.INTERNAL)
                        } else {
                            item.path
                        }
                    updateList(path)
                    binding.breadcrumbBar.removeItemsWithRange(position)
                    viewModel.updateDirectoryListWithPos(position)
                }
            },
        )
    }

    private fun initButtonClick() {
        binding.btnCancel.click { dismiss() }
        binding.btnConfirm.click {
            pathCallback?.invoke(viewModel.currentPath)
            dismiss()
        }
    }

    private fun handleBackPressed() {
        val directoryList = viewModel.getDirectoryList()
        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (directoryList.size > 1) {
                    directoryList.removeAt(directoryList.lastIndex)
                    updateList(directoryList.last())
                    binding.breadcrumbBar.removeLastBreadCrumbItem()
                } else {
                    dialog.dismiss()
                }
            }
            true
        }
    }
}
