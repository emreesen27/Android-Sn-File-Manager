package com.sn.snfilemanager.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.view.View
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.BaseFragment
import com.sn.snfilemanager.databinding.FragmentHomeBinding
import java.io.File
import java.text.CharacterIterator
import java.text.DecimalFormat
import java.text.StringCharacterIterator
import kotlin.math.abs


class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    //private val moreMenu by powerMenu<PowerMenuFactory>()

    override fun getViewModelClass() = HomeViewModel::class.java

    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = true

    override fun getMenuResId(): Int = R.menu.menu_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //clickMenu()
        //initListenerPowerMenuItem()
        initMenuButtonListener()


        Log.d("emre",humanReadableByteCountSI2(getFreeInternalMemory()).toString())

    }

    private fun humanReadableByteCountSI2(bytes: Long): String {
        if (bytes < 1000) {
            return "$bytes B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1000 && unitIndex < units.size - 1) {
            value /= 1000
            unitIndex++
        }
        val decimalFormat = DecimalFormat("#,##0.##")
        return "${decimalFormat.format(value)} ${units[unitIndex]}"
    }



    private fun getFreeInternalMemory(): Long {
        return getFreeMemory(Environment.getDataDirectory())
    }

    private fun getFreeMemory(path: File): Long {
        val stats = StatFs(path.getAbsolutePath())
        return stats.availableBlocksLong * stats.blockSizeLong
    }


    /*
    private fun clickMenu() {
        binding.ivMenu.setOnClickListener {
            moreMenu?.showAsDropDown(it)
        }
    }*/

    private fun initMenuButtonListener() {
        binding.ibImages.setOnClickListener {
            navigate(HomeFragmentDirections.actionHomeImage())
        }

        binding.btnFile.setOnClickListener {
            navigate(HomeFragmentDirections.actionHomeFile())
        }
    }

    

    /*
    private fun initListenerPowerMenuItem() {
        moreMenu?.setOnMenuItemClickListener { position, item ->
            moreMenu?.selectedPosition = position
            Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show()
        }
    }*/

    /*
    * class YourViewModel : ViewModel() {
    private val _formattedByteCount = MutableLiveData<String>()
    val formattedByteCount: LiveData<String>
        get() = _formattedByteCount

    fun calculateAndSetFormattedByteCount(byteCount: Long) {
        viewModelScope.launch {
            val formattedCount = withContext(Dispatchers.Default) {
                humanReadableByteCountSI(byteCount)
            }
            _formattedByteCount.value = formattedCount
        }
    }
}
    *
    * */

}