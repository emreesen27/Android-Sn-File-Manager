package com.sn.snfilemanager.view.dialog.permission

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click

class PermissionDialog(
    context: Context,
    private val type: PermissionDialogType,
) : Dialog(context) {
    var onAllow: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_permission)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)

        setCancelable(false)

        val values = getValuesByType()
        findViewById<MaterialTextView>(R.id.tv_permission_info).text = values.first
        findViewById<MaterialButton>(R.id.btn_allow).apply {
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
                context.getString(R.string.permission_message),
                context.getString(R.string.click_to_allow),
            )
        } else {
            Pair(
                context.getString(R.string.permission_warning_message),
                context.getString(R.string.go_to_settings),
            )
        }
}
