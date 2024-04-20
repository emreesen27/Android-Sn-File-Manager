package com.sn.snfilemanager.view.dialog.license

import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.databinding.DialogLicenseBinding

class LicenseDialog : BaseDialog<DialogLicenseBinding>() {
    override fun getViewBinding() = DialogLicenseBinding.inflate(layoutInflater)

    override val dialogTag: String
        get() = "LICENSE_DIALOG"

    override fun setupViews() {
        binding.recycler.adapter = LicenseAdapter(requireContext())
        binding.ivClose.click { dismiss() }
    }
}
