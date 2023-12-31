package com.sn.snfilemanager.feature.pathpicker.presentation

import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.idanatz.oneadapter.OneAdapter
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.setNavigationResult
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.databinding.FragmentPathPickerBinding
import com.sn.snfilemanager.feature.files.data.toFileModel
import com.sn.snfilemanager.feature.pathpicker.module.DirectoryItemModule
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PathPickerFragment : BaseFragment<FragmentPathPickerBinding, PathPickerViewModel>() {

    private lateinit var oneAdapter: OneAdapter

    override fun getViewModelClass() = PathPickerViewModel::class.java

    override fun getViewBinding() = FragmentPathPickerBinding.inflate(layoutInflater)

    override fun getActionBarStatus() = true

    override fun getMenuResId() = R.menu.menu_path_picker

    override fun onMenuItemSelected(menuItemId: Int) = when (menuItemId) {
        R.id.action_done -> {
            actionMoveFile()
            true
        }

        R.id.action_new_folder -> {
            true
        }

        else -> super.onMenuItemSelected(menuItemId)
    }

    override fun setupViews() {
        initAdapter()
        handleBackPressed()
        updateList(viewModel.getStoragePath(RootPath.INTERNAL))
    }


    private fun actionMoveFile() {
        setNavigationResult(viewModel.currentPath.toString(), "path")
        findNavController().popBackStack()
    }

    private fun updateList(directoryPath: String) {
        with(viewModel) {
            currentPath = directoryPath
            updateDirectoryList(directoryPath)
            oneAdapter.setItems(getDirectoryList(directoryPath).map { it.toFileModel() })
        }
    }

    private fun initAdapter() {
        oneAdapter = OneAdapter(binding.recycler) {
            itemModules += DirectoryItemModule().apply {
                onClick = { file ->
                    if (file.isDirectory) {
                        updateList(file.absolutePath)
                    }
                }
            }
        }
    }

    private fun handleBackPressed() {
        val directoryList = viewModel.getDirectoryList()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (directoryList.size > 1) {
                    directoryList.removeAt(directoryList.lastIndex)
                    updateList(directoryList.last())
                } else {
                    isEnabled = false
                    setNavigationResult(getString(R.string.path_not_selected), "no_selected")
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}