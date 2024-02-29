package com.sn.snfilemanager.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogCreateDirectoryBinding
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CreateDirectoryDialog(
    private val path: String,
    private val onCreate: ((Path) -> Unit)? = null,
) :
    DialogFragment() {
    private val binding: DialogCreateDirectoryBinding by lazy {
        DialogCreateDirectoryBinding.inflate(layoutInflater)
    }

    companion object {
        const val TAG = "CREATE_DIRECTORY_DIALOG"
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
        )
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
        binding.etFolderName.requestFocus()
        binding.btnCancel.click { dismiss() }
        binding.btnCreate.click {
            val name = binding.etFolderName.text.toString()
            if (checkNameExists(name)) {
                binding.inputLayout.error = getString(R.string.folder_exists_warning)
            } else {
                onCreate?.invoke(Paths.get(path).resolve(name))
                dismiss()
            }
        }
        binding.etFolderName.addTextChangedListener { text ->
            binding.btnCreate.isEnabled = !text.isNullOrEmpty()
            binding.inputLayout.error = null
        }
    }

    private fun checkNameExists(name: String): Boolean {
        val targetPath = Paths.get(path).resolve(name)
        return Files.exists(targetPath)
    }
}
