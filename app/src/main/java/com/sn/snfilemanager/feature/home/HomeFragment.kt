package com.sn.snfilemanager.feature.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.databinding.FragmentHomeBinding
import com.sn.snfilemanager.view.dialog.permission.PermissionDialog
import com.sn.snfilemanager.view.dialog.permission.PermissionDialogType
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override fun getViewModelClass() = HomeViewModel::class.java

    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = true

    override fun getMenuResId(): Int = R.menu.menu_home

    override fun setupViews() {
        initMenuButtonListener()
    }

    override fun onResume() {
        super.onResume()
        initPermission()
    }

    override fun onMenuItemSelected(menuItemId: Int) = when (menuItemId) {
        R.id.settings -> {
            navigate(HomeFragmentDirections.actionSettings())
            true
        }

        R.id.about -> {
            true
        }

        else -> super.onMenuItemSelected(menuItemId)
    }

    override fun observeData() {
        observe(viewModel.availableStorageLiveData) { memory ->
            binding.btnFile.subTitle = getString(R.string.available_storage, memory)
        }
        observe(viewModel.availableExternalStorageLiveData) { memory ->
            binding.btnExternalFile.subTitle =
                memory?.let { getString(R.string.available_storage, it) }
                    ?: getString(R.string.no_external_storage)
        }
    }

    private fun allowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            routeFileAccessSettings()
        } else {
            if (viewModel.hasRequestedPermissionBefore())
                routeAppSettings()
            else
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        viewModel.setPermissionRequested()
    }

    private fun initPermission() {
        val type =
            if (viewModel.hasRequestedPermissionBefore()) PermissionDialogType.WARNING else PermissionDialogType.DEFAULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager())
                showPermissionDialog(type)
        } else if (!checkStoragePermission(requireContext())) {
            showPermissionDialog(type)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showPermissionDialog(PermissionDialogType.WARNING)
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun routeFileAccessSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:${requireActivity().packageName}")
        )
        startActivity(intent)
    }

    private fun routeAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${requireActivity().packageName}")
        )
        startActivity(intent)
    }

    private fun showPermissionDialog(type: PermissionDialogType = PermissionDialogType.DEFAULT) {
        PermissionDialog(requireContext(), type).apply {
            onAllow = { allowPermission() }
        }.show()
    }

    private fun checkStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initMenuButtonListener() {
        with(binding) {
            ibImages.click { navigate(HomeFragmentDirections.actionHomeImage(MediaType.IMAGES)) }
            ibVideo.click { navigate(HomeFragmentDirections.actionHomeImage(MediaType.VIDEOS)) }
            ibSound.click { navigate(HomeFragmentDirections.actionHomeImage(MediaType.AUDIOS)) }
            ibDocuments.click { navigate(HomeFragmentDirections.actionHomeImage(MediaType.FILES)) }
            ibApk.click { navigate(HomeFragmentDirections.actionHomeImage(MediaType.FILES, DocumentType.APK.name)) }
            ibArchives.click{ navigate(HomeFragmentDirections.actionHomeImage(MediaType.FILES, DocumentType.ARCHIVE.name)) }

            btnDownload.click { navigate(HomeFragmentDirections.actionHomeFile(RootPath.DOWNLOAD)) }
            btnFile.click { navigate(HomeFragmentDirections.actionHomeFile(RootPath.INTERNAL)) }
            btnExternalFile.click {
                viewModel.availableExternalStorageLiveData.value?.let {
                    navigate(HomeFragmentDirections.actionHomeFile(RootPath.EXTERNAL))
                }
            }
        }
    }
}