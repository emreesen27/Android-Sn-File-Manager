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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseFragment
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.observe
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.databinding.FragmentHomeBinding
import com.sn.snfilemanager.view.dialog.ConfirmationDialog
import com.sn.snfilemanager.view.dialog.permission.PermissionDialog
import com.sn.snfilemanager.view.dialog.permission.PermissionDialogType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    private var permissionDialog: PermissionDialog? = null
    private var confirmationDialog: ConfirmationDialog? = null

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

    override fun onMenuItemSelected(menuItemId: Int) =
        when (menuItemId) {
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

    private fun allowStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            routeFileAccessSettings()
        } else {
            if (viewModel.hasStorageRequestedPermissionBefore()) {
                routeAppSettings()
            } else {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        viewModel.setStoragePermissionRequested()
    }

    private fun allowNotificationPermission() {
        if (!viewModel.hasNotificationRequestedPermissionBefore()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            routeNotificationSettings()
        }
        viewModel.setNotificationPermissionRequested()
    }

    private fun initPermission() {
        val type =
            if (viewModel.hasStorageRequestedPermissionBefore()) PermissionDialogType.WARNING else PermissionDialogType.DEFAULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showPermissionDialog(type)
            } else {
                initNotificationPermission()
            }
        } else if (!checkStoragePermission(requireContext())) {
            showPermissionDialog(type)
        } else {
            initNotificationPermission()
        }
    }

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showPermissionDialog(PermissionDialogType.WARNING)
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showNotificationDialog()
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun routeFileAccessSettings() {
        val intent =
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${requireActivity().packageName}"),
            )
        startActivity(intent)
    }

    private fun routeAppSettings() {
        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${requireActivity().packageName}"),
            )
        startActivity(intent)
    }

    private fun routeNotificationSettings() {
        val notificationManager = NotificationManagerCompat.from(requireContext())
        if (!notificationManager.areNotificationsEnabled()) {
            val settingsIntent =
                Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                }
            context?.startActivity(settingsIntent)
        }
    }

    private fun showPermissionDialog(type: PermissionDialogType = PermissionDialogType.DEFAULT) {
        if (permissionDialog == null || permissionDialog?.isShowing == false) {
            permissionDialog =
                PermissionDialog(requireContext(), type).apply {
                    onAllow = { allowStoragePermission() }
                }
            permissionDialog?.show()
        }
    }

    private fun showNotificationDialog() {
        if (confirmationDialog == null || confirmationDialog?.isShowing == false) {
            confirmationDialog =
                ConfirmationDialog(
                    requireContext(),
                    getString(R.string.permission_warning_title),
                    getString(R.string.notification_permission_info),
                ).apply {
                    onSelected = { ok ->
                        if (ok) {
                            allowNotificationPermission()
                        }
                    }
                }
            confirmationDialog?.show()
        }
    }

    private fun checkStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initNotificationPermission() {
        if (!viewModel.notificationRuntimeRequested) {
            if (!checkNotificationPermission(requireContext())) {
                showNotificationDialog()
            }
            viewModel.notificationRuntimeRequested = true
        }
    }

    private fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun initMenuButtonListener() {
        with(binding) {
            ibImages.click {
                navigate(
                    HomeFragmentDirections.actionHomeMedia(
                        mediaType = MediaType.IMAGES,
                        title = getString(R.string.images)
                    )
                )
            }
            ibVideo.click {
                navigate(
                    HomeFragmentDirections.actionHomeMedia(
                        mediaType = MediaType.VIDEOS,
                        getString(R.string.videos)
                    )
                )
            }
            ibSound.click {
                navigate(
                    HomeFragmentDirections.actionHomeMedia(
                        mediaType = MediaType.AUDIOS,
                        title = getString(R.string.sounds)
                    )
                )
            }
            ibDocuments.click {
                navigate(
                    HomeFragmentDirections.actionHomeMedia(
                        mediaType = MediaType.FILES,
                        title = getString(R.string.documents)
                    )
                )
            }
            ibApk.click {
                navigate(
                    HomeFragmentDirections.actionHomeMedia(
                        mediaType = MediaType.FILES,
                        documentType = DocumentType.APK.name,
                        title = getString(R.string.apk_files)
                    ),
                )
            }
            ibArchives.click {
                navigate(
                    HomeFragmentDirections.actionHomeMedia(
                        mediaType = MediaType.FILES,
                        documentType = DocumentType.ARCHIVE.name,
                        title = getString(R.string.archives)
                    ),
                )
            }

            btnDownload.click {
                navigate(
                    HomeFragmentDirections.actionHomeFile(
                        storageArgs = RootPath.DOWNLOAD,
                        title = getString(R.string.downloads)
                    )
                )
            }
            btnFile.click {
                navigate(
                    HomeFragmentDirections.actionHomeFile(
                        storageArgs = RootPath.INTERNAL,
                        title = getString(R.string.folders)
                    )
                )
            }
            btnExternalFile.click {
                viewModel.availableExternalStorageLiveData.value?.let {
                    navigate(
                        HomeFragmentDirections.actionHomeFile(
                            storageArgs = RootPath.EXTERNAL,
                            title = getString(R.string.folders)
                        )
                    )
                }
            }
        }
    }
}
