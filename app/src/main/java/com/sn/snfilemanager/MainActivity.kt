package com.sn.snfilemanager

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.sn.snfilemanager.databinding.ActivityMainBinding
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.AndroidEntryPoint
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
@RuntimePermissions
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setFirsScreen()

        /*
        searchFileWithPermissionCheck()
        searchFile()

        if (Environment.isExternalStorageManager()) {
            //todo when permission is granted
        } else {
            //request for the permission
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }*/

    }

    private fun checkFirsRun(): Boolean = mySharedPreferences.getBoolean(PrefsTag.FIRST_RUN)

    private fun setFirsScreen() {
        val destId =
            if (checkFirsRun()) R.id.action_loading_to_home else R.id.action_loading_to_start
        findNavController(R.id.base_nav_host).navigate(destId)
    }


    @NeedsPermission(WRITE_EXTERNAL_STORAGE)
    fun searchFile() {
        val path = Environment.getExternalStorageDirectory().absolutePath
        val spath = "/Download"
        val fullPath = File(path + File.separator + spath)
        val files = fullPath.listFiles()
        if (files != null) {
            Log.d("emre", files.toString())
        }
    }

    fun ListFile(root: File) {
        val fileList = ArrayList<String>()
        val listAllFiles = root.listFiles()
        for (currentFile in listAllFiles) {
            Log.d("emre", currentFile.absolutePath)
            fileList.add(currentFile.absolutePath)
        }
    }

}