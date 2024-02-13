package com.sn.snfilemanager

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.util.Config
import com.sn.snfilemanager.databinding.ActivityMainBinding
import com.sn.snfilemanager.feature.settings.SettingsUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initObserve()
        initConfig()
    }

    private fun initObserve() {
        observe(vm.firstRunLiveData) { event ->
            event.getContentIfNotHandled()?.let { firstRun ->
                setFirsScreen(firstRun)
            }
        }
    }

    private fun initConfig() {
        Config.hiddenFile = SettingsUtils.resolveHiddenFiles(this)
    }

    private fun setFirsScreen(firstRun: Boolean) {
        val destId =
            if (firstRun) R.id.action_loading_to_home else R.id.action_loading_to_start
        findNavController(R.id.base_nav_host).navigate(destId)
    }
}
