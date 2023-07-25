package com.sn.snfilemanager.files

import android.os.Environment
import androidx.activity.OnBackPressedCallback
import com.idanatz.oneadapter.OneAdapter
import com.sn.snfilemanager.core.BaseFragment
import com.sn.snfilemanager.databinding.FragmentFilesListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilesListFragment : BaseFragment<FragmentFilesListBinding, FilesListViewModel>() {

    private lateinit var oneAdapter: OneAdapter

    override fun getViewModelClass() = FilesListViewModel::class.java

    override fun getViewBinding() = FragmentFilesListBinding.inflate(layoutInflater)

    override fun getActionBarStatus() = false

    override fun setupViews() {
        initAdapter()
        handleBackPressed()
        updateFileList(Environment.getExternalStorageDirectory().absolutePath)

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
