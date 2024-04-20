package com.sn.snfilemanager.view.dialog

import android.view.View
import androidx.core.widget.addTextChangedListener
import com.sn.mediastorepv.data.Media
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.databinding.DialogRenameFileBinding
import com.sn.snfilemanager.feature.files.data.FileModel
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class RenameFileDialog<T>(
    private val file: T,
    private val onRename: ((String) -> Unit)? = null,
) : BaseDialog<DialogRenameFileBinding>() {
    override fun getViewBinding() = DialogRenameFileBinding.inflate(layoutInflater)

    override val dialogTag: String
        get() = "RENAME_FILE_DIALOG"

    val nameExtractor: (T) -> String = { item ->
        when (item) {
            is FileModel -> item.absolutePath
            is Media -> item.data
            else -> throw IllegalArgumentException()
        }
    }

    override fun setupViews() {
        binding.btnCancel.click { dismiss() }
        val path = Paths.get(nameExtractor(file))

        if (path.isDirectory()) {
            binding.inputLayoutExt.gone()
        } else {
            binding.inputLayoutExt.visible()
            binding.etExt.setText(path.extension)
        }

        with(binding.etName) {
            requestFocus()
            setText(path.nameWithoutExtension)
            text?.length?.let { setSelection(0, it) }
        }

        binding.btnRename.isEnabled = !binding.etName.text.isNullOrEmpty() &&
            (binding.inputLayoutExt.visibility == View.GONE || !binding.etExt.text.isNullOrEmpty())

        binding.btnRename.click {
            val name = binding.etName.text.toString()
            val ext = binding.etExt.text.toString().takeIf { it.isNotBlank() }?.let { ".$it" } ?: ""
            val newName = if (ext.isNotEmpty()) "$name$ext" else name

            if (newName == path.name) {
                binding.inputLayout.error = getString(R.string.current_file_name_warning)
                return@click
            }

            if (isNameExists(newName)) {
                binding.inputLayout.error = getString(R.string.file_exists_warning)
            } else {
                onRename?.invoke(newName)
                dismiss()
            }
        }

        binding.etName.addTextChangedListener { text ->
            binding.btnRename.isEnabled =
                !text.isNullOrEmpty() && (
                    binding.inputLayoutExt.visibility == View.GONE ||
                        !binding.etExt.text.isNullOrEmpty()
                )
            binding.inputLayout.error = null
        }

        binding.etExt.addTextChangedListener { text ->
            binding.btnRename.isEnabled =
                !binding.etName.text.isNullOrEmpty() && !text.isNullOrEmpty()
            binding.tvInfo.visible()
        }
    }

    private fun isNameExists(name: String): Boolean {
        val targetPath = Paths.get(nameExtractor(file)).resolveSibling(name)
        return Files.exists(targetPath)
    }
}
