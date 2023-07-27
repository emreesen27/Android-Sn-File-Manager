package com.sn.snfilemanager.ui

import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.BaseFragment
import com.sn.snfilemanager.databinding.FragmentHomeBinding
import com.sn.snfilemanager.extensions.observe
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    //private val moreMenu by powerMenu<PowerMenuFactory>()

    override fun getViewModelClass() = HomeViewModel::class.java

    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)

    override fun getActionBarStatus(): Boolean = true

    override fun getMenuResId(): Int = R.menu.menu_home

    override fun setupViews() {
        initMenuButtonListener()
    }

    override fun observeData() {
        observe(viewModel.availableStorageLiveData) { memory ->
            binding.btnFile.subTitle = getString(R.string.available_storage, memory)
        }
        observe(viewModel.availableExternalStorageLiveData) { memory ->
            binding.btnExternalFile.subTitle = getString(R.string.available_storage, memory)
        }
    }


    /*
    private fun clickMenu() {
        binding.ivMenu.setOnClickListener {
            moreMenu?.showAsDropDown(it)
        }
    }*/

    private fun initMenuButtonListener() {
        with(binding) {
            ibImages.setOnClickListener { navigate(HomeFragmentDirections.actionHomeImage()) }
            btnFile.setOnClickListener {
                navigate(HomeFragmentDirections.actionHomeFile(StorageType.INTERNAL))
            }
            btnExternalFile.setOnClickListener {
                navigate(HomeFragmentDirections.actionHomeFile(StorageType.EXTERNAL))
            }
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