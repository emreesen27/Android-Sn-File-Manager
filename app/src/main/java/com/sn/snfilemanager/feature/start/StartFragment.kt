package com.sn.snfilemanager.feature.start

import android.content.Intent
import android.net.Uri
import com.sn.snfilemanager.BuildConfig
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.startActivitySafely
import com.sn.snfilemanager.core.util.Constant.OPEN_SOURCE_LICENSE
import com.sn.snfilemanager.core.util.Constant.PRIVACY_URL
import com.sn.snfilemanager.databinding.FragmentStartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartFragment : BaseFragment<FragmentStartBinding, StartViewModel>() {
    override fun getViewModelClass() = StartViewModel::class.java

    override fun getViewBinding() = FragmentStartBinding.inflate(layoutInflater)

    override fun setupViews() {
        setVersionText()
        clickContinue()
        clicksPolicy()
    }

    private fun setVersionText() {
        binding.mtvVersion.text = BuildConfig.VERSION_NAME
    }

    private fun clickContinue() {
        binding.btnContinue.click {
            viewModel.saveFirsRun()
            navigate(StartFragmentDirections.actionStartToHome())
        }
    }

    private fun clicksPolicy() {
        binding.mtvPrivacyPolicy.click { openUrl(PRIVACY_URL) }
        binding.mtvOpenSourcePolicy.click { openUrl(OPEN_SOURCE_LICENSE) }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context?.startActivitySafely(intent)
    }
}
