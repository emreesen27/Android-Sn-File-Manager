package com.sn.snfilemanager.view.dialog

import android.content.DialogInterface
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogConflictBinding

class ConflictDialog(
    private val fileName: String,
) : BaseDialog<DialogConflictBinding>() {
    var onSelected: ((ConflictStrategy, Boolean) -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    override fun getViewBinding() = DialogConflictBinding.inflate(layoutInflater)

    override var setCancelable: Boolean = false
    override val dialogTag: String
        get() = "CONFLICT_DIALOG"

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }

    override fun setupViews() {
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
