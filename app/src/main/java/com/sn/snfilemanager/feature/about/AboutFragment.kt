package com.sn.snfilemanager.feature.about

import androidx.navigation.fragment.findNavController
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.openUrl
import com.sn.snfilemanager.core.util.Constant.GITHUB_URL
import com.sn.snfilemanager.core.util.Constant.PRIVACY_URL
import com.sn.snfilemanager.databinding.FragmentAboutBinding
import com.sn.snfilemanager.view.dialog.license.LicenseDialog

class AboutFragment : BaseFragment<FragmentAboutBinding, AboutViewModel>() {
    override fun getViewModelClass() = AboutViewModel::class.java

    override fun getViewBinding() = FragmentAboutBinding.inflate(layoutInflater)

    override fun setupViews() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        initClicks()
    }

    private fun initClicks() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.btnGithub.click { context?.openUrl(GITHUB_URL) }
        binding.btnLicense.click { showLicensesDialog() }
        binding.btnPrivacy.click { context?.openUrl(PRIVACY_URL) }
    }

    private fun showLicensesDialog() {
        LicenseDialog().show(childFragmentManager, LicenseDialog.TAG)
    }
}
