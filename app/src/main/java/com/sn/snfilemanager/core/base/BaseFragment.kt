package com.sn.snfilemanager.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VBinding : ViewBinding, VModel : ViewModel> : Fragment() {
    open var useSharedViewModel: Boolean = false

    protected lateinit var viewModel: VModel

    protected abstract fun getViewModelClass(): Class<VModel>

    protected lateinit var binding: VBinding

    protected abstract fun getViewBinding(): VBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        getToolbar()?.let { toolbar ->
            getMenuResId()?.let { menuId ->
                initMenu(menuId, toolbar)
            }
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }

    open fun setupViews() {}

    open fun observeData() {}

    open fun getMenuResId(): Int? {
        return null
    }

    open fun getToolbar(): Toolbar? {
        return null
    }

    open fun onMenuItemSelected(menuItemId: Int): Boolean {
        return false
    }

    fun navigate(directions: NavDirections) {
        findNavController().navigate(directions)
    }

    fun invalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }

    private fun initMenu(
        menuId: Int,
        toolbar: Toolbar,
    ) {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater,
                ) {
                    menuInflater.inflate(menuId, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return onMenuItemSelected(menuItem.itemId)
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.CREATED,
        )
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
    }

    private fun init() {
        binding = getViewBinding()
        viewModel =
            if (useSharedViewModel) {
                ViewModelProvider(requireActivity())[getViewModelClass()]
            } else {
                ViewModelProvider(this)[getViewModelClass()]
            }
    }
}
