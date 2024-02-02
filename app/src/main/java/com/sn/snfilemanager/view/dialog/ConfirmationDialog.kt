package com.sn.snfilemanager.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogConfirmationBinding

class ConfirmationDialog(
    context: Context,
    private val title: String,
    private val question: String,
) : Dialog(context) {
    var onSelected: ((Boolean) -> Unit)? = null

    private val binding: DialogConfirmationBinding by lazy {
        DialogConfirmationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCancelable(false)
        setWindowProperty()

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

    private fun setWindowProperty() {
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
    }
}
