package com.sn.snfilemanager

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.sn.snfilemanager.core.Config
import com.sn.snfilemanager.core.extensions.observe
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
        setSupportActionBar(binding.toolbar)
        initObserve()
        initDestinationListener()
        initConfig()
    }

    private fun initDestinationListener() {
        findNavController(R.id.base_nav_host).addOnDestinationChangedListener { _, destination, arguments ->
            val title = when (destination.id) {
                R.id.settings_fragment -> {
                    getString(R.string.settings)
                }

                R.id.home_fragment -> {
                    getString(R.string.home)
                }

                else -> arguments?.getString("title") ?: getString(R.string.app_name)
            }
            supportActionBar?.title = title
        }
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
