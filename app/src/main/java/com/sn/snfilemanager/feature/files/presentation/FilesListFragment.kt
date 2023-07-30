package com.sn.snfilemanager.feature.files.presentation

import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.idanatz.oneadapter.OneAdapter
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.databinding.FragmentFilesListBinding
import com.sn.snfilemanager.feature.files.data.toFileModel
import com.sn.snfilemanager.feature.files.module.FileItemModule
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilesListFragment : BaseFragment<FragmentFilesListBinding, FilesListViewModel>() {

    private val args: FilesListFragmentArgs by navArgs()
    private lateinit var oneAdapter: OneAdapter

    override fun getViewModelClass() = FilesListViewModel::class.java

    override fun getViewBinding() = FragmentFilesListBinding.inflate(layoutInflater)

    override fun getActionBarStatus() = false

    override fun setupViews() {
        initAdapter()
        handleBackPressed()
        updateFileList(viewModel.getStoragePath(args.storageArgs))

    }

    private fun updateFileList(directoryPath: String) {
        with(viewModel) {
            updateDirectoryList(directoryPath)
            oneAdapter.setItems(getFilesList(directoryPath).map { it.toFileModel() })
        }
    }

    private fun initAdapter() {
        oneAdapter = OneAdapter(binding.rcvFiles) {
            itemModules += FileItemModule(requireContext()).apply {
                onClick = { file ->
                    if (file.isDirectory) {
                        updateFileList(file.absolutePath)
                    } else {
                        // dosya ise açılabilir veya işlenebilir
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
                    updateFileList(directoryList.last())
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}
