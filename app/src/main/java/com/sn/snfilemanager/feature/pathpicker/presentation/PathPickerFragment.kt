package com.sn.snfilemanager.feature.pathpicker.presentation

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.databinding.FragmentPathPickerBinding
import com.sn.snfilemanager.feature.files.data.toFileModel
import com.sn.snfilemanager.feature.pathpicker.adapter.DirectoryItemAdapter
import com.sn.snfilemanager.view.component.breadcrumb.BreadCrumbItemClickListener
import com.sn.snfilemanager.view.component.breadcrumb.BreadItem
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PathPickerFragment(private val pathCallback: ((String?) -> Unit)? = null) : DialogFragment() {
    private var adapter: DirectoryItemAdapter? = null
    private val viewModel: PathPickerViewModel by viewModels()
    private val binding: FragmentPathPickerBinding by lazy {
        FragmentPathPickerBinding.inflate(layoutInflater)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
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
            adapter?.setItems(getDirectoryList(directoryPath).map { it.toFileModel() })
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
