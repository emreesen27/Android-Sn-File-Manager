package com.sn.snfilemanager.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogConflictBinding

class ConflictDialog(
    context: Context,
    private val fileName: String,
) : Dialog(context) {
    private val binding: DialogConflictBinding by lazy {
        DialogConflictBinding.inflate(layoutInflater)
    }

    var onSelected: ((ConflictStrategy, Boolean) -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    init {
        setOnDismissListener {
            onDismiss?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCancelable(false)
        setWindowProperty()
        initView()
    }

    private fun setWindowProperty() {
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
    }

    private fun initView() {
        with(binding) {
            tvFileName.text = fileName
            btnSkip.click {
                onSelected?.invoke(ConflictStrategy.SKIP, cbAll.isChecked)
                dismiss()
            }
            btnKeepBoth.click {
                onSelected?.invoke(ConflictStrategy.KEEP_BOTH, cbAll.isChecked)
                dismiss()
            }
            btnOverwrite.click {
                onSelected?.invoke(ConflictStrategy.OVERWRITE, cbAll.isChecked)
                dismiss()
            }
        }
    }
}
