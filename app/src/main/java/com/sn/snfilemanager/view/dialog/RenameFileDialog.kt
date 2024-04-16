package com.sn.snfilemanager.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.sn.mediastorepv.data.Media
import com.sn.snfilemanager.R
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
) : DialogFragment() {
    private val binding: DialogRenameFileBinding by lazy {
        DialogRenameFileBinding.inflate(layoutInflater)
    }

    val nameExtractor: (T) -> String = { item ->
        when (item) {
            is FileModel -> item.absolutePath
            is Media -> item.data
            else -> throw IllegalArgumentException()
        }
    }

    companion object {
        const val TAG = "RENAME_FILE_DIALOG"
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
