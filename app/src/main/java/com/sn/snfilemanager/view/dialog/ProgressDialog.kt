package com.sn.snfilemanager.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.sn.snfilemanager.databinding.DialogProgressBinding

class ProgressDialog(
    context: Context
) : Dialog(context) {

    private val binding: DialogProgressBinding by lazy {
        DialogProgressBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCancelable(false)
        setWindowProperty()
    }

    fun setProgressValue(value: Int) {
        binding.progress.progress = value
        binding.tvProgress.text = value.toString()
    }

    private fun setWindowProperty() {
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }
    }
}