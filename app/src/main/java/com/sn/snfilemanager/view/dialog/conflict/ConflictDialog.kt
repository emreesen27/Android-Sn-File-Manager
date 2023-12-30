package com.sn.snfilemanager.view.dialog.conflict

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
    private val listener: ConflictDialogListener
) : Dialog(context) {

    private val binding: DialogConflictBinding by lazy {
        DialogConflictBinding.inflate(layoutInflater)
    }

    init {
        setOnDismissListener {
            listener.onDismiss()
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
                listener.onConflictSelected(ConflictStrategy.SKIP, cbAll.isChecked)
                dismiss()
            }
            btnKeepBoth.click {
                listener.onConflictSelected(ConflictStrategy.KEEP_BOTH, cbAll.isChecked)
                dismiss()
            }
            btnOverwrite.click {
                listener.onConflictSelected(ConflictStrategy.OVERWRITE, cbAll.isChecked)
                dismiss()
            }
        }
    }

}