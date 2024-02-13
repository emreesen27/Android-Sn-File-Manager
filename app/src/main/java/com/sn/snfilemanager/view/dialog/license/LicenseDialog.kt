package com.sn.snfilemanager.view.dialog.license

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogLicenseBinding

class LicenseDialog : DialogFragment() {
    companion object {
        const val TAG = "LICENSE_DIALOG"
    }

    private val binding: DialogLicenseBinding by lazy {
        DialogLicenseBinding.inflate(layoutInflater)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded)
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
        binding.recycler.adapter = LicenseAdapter(requireContext())
        binding.ivClose.click { dismiss() }
    }
}
