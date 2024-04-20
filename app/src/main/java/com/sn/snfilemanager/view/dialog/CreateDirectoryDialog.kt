package com.sn.snfilemanager.view.dialog

import androidx.core.widget.addTextChangedListener
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogCreateDirectoryBinding
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CreateDirectoryDialog(
    private val path: String,
    private val onCreate: ((Path) -> Unit)? = null,
) : BaseDialog<DialogCreateDirectoryBinding>() {
    override val dialogTag: String
        get() = "CREATE_DIRECTORY_DIALOG"

    override fun getViewBinding() = DialogCreateDirectoryBinding.inflate(layoutInflater)

    override fun setupViews() {
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
