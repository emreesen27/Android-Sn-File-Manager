package com.sn.snfilemanager.feature.start

import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.BuildConfig
import com.sn.snfilemanager.databinding.FragmentStartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartFragment : BaseFragment<FragmentStartBinding, StartViewModel>() {

    override fun getViewModelClass() = StartViewModel::class.java

    override fun getViewBinding() = FragmentStartBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = false

    override fun setupViews() {
        setVersionText()
        clickContinue()
        clicksPolicy()
    }

    private fun setVersionText() {
        binding.mtvVersion.text = BuildConfig.VERSION_NAME
    }

    private fun clickContinue() {
        binding.btnContinue.setOnClickListener {
            viewModel.saveFirsRun()
            navigate(StartFragmentDirections.actionStartToHome())
        }
    }

    // Todo click implementation
    private fun clicksPolicy() {
        binding.mtvPrivacyPolicy.setOnClickListener { }
        binding.mtvOpenSourcePolicy.setOnClickListener { }
    }

}