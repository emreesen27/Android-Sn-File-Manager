package com.sn.snfilemanager.view.dialog

import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogConfirmationBinding

class ConfirmationDialog(
    private val title: String,
    private val question: String,
) : BaseDialog<DialogConfirmationBinding>() {
    var onSelected: ((Boolean) -> Unit)? = null

    override val dialogTag: String
        get() = "CONFIRMATION_DIALOG"

    override fun getViewBinding() = DialogConfirmationBinding.inflate(layoutInflater)

    override var setCancelable: Boolean = false

    override fun setupViews() {
        binding.tvTitle.text = title
        binding.tvQuestion.text = question

        binding.btnYes.click {
            onSelected?.invoke(true)
            dismiss()
        }
        binding.btnNo.click {
            onSelected?.invoke(false)
            dismiss()
        }
    }
}
