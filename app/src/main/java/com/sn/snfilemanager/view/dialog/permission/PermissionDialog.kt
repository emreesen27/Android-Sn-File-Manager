package com.sn.snfilemanager.view.dialog.permission

import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogPermissionBinding

class PermissionDialog(
    private val type: PermissionDialogType,
) : BaseDialog<DialogPermissionBinding>() {
    var onAllow: (() -> Unit)? = null

    override fun getViewBinding() = DialogPermissionBinding.inflate(layoutInflater)

    override var setCancelable: Boolean = false
    override val dialogTag: String
        get() = "PERMISSION_DIALOG"

    override fun setupViews() {
        val values = getValuesByType()
        binding.tvPermissionInfo.text = values.first
        binding.btnAllow.apply {
            text = values.second
            click {
                onAllow?.invoke()
                dismiss()
            }
        }
    }

    private fun getValuesByType(): Pair<String, String> =
        if (type == PermissionDialogType.DEFAULT) {
            Pair(
                requireContext().getString(R.string.permission_message),
                requireContext().getString(R.string.click_to_allow),
            )
        } else {
            Pair(
                requireContext().getString(R.string.permission_warning_message),
                requireContext().getString(R.string.go_to_settings),
            )
        }
}
