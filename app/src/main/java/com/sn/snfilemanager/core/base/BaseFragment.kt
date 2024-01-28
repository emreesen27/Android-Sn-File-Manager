package com.sn.snfilemanager.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.invisible
import com.sn.snfilemanager.core.extensions.visible

abstract class BaseFragment<VBinding : ViewBinding, VModel : ViewModel> : Fragment() {

    private var progress: LinearProgressIndicator? = null
    private var actionMenu: ConstraintLayout? = null
    private var toolbar: Toolbar? = null

    open var useSharedViewModel: Boolean = false
    open var actionCancelCLick: (() -> Unit)? = null

    protected lateinit var viewModel: VModel
    protected abstract fun getViewModelClass(): Class<VModel>

    protected lateinit var binding: VBinding
    protected abstract fun getViewBinding(): VBinding

    protected abstract fun getActionBarStatus(): Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setActionBarStatus()
        getMenuResId()?.let { menuId ->
            initMenu(menuId)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = activity?.findViewById(R.id.toolbar)
        actionMenu = activity?.findViewById(R.id.action_menu)
        progress = activity?.findViewById(R.id.progress)
        setupViews()
        observeData()
        initActionCancel()
    }

    open fun setupViews() {}

    open fun observeData() {}

    open fun getMenuResId(): Int? {
        return null
    }

    open fun onMenuItemSelected(menuItemId: Int): Boolean {
        return false
    }

    fun navigate(directions: NavDirections) {
        findNavController().navigate(directions)
    }

    private fun setActionBarStatus() {
        setToolbarVisibility(getActionBarStatus())
    }

    fun invalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }

    fun getToolbar(): Toolbar? = toolbar

    fun showProgressDialog() {
        progress?.visible()
    }

    fun hideProgressDialog() {
        progress?.invisible()
    }

    fun setToolbarVisibility(value: Boolean) {
        activity?.findViewById<Toolbar>(R.id.toolbar)?.apply {
            if (value) visible() else gone()
        }
    }

    fun setActionMenuVisibility(value: Boolean) {
        actionMenu?.apply { if (value) visible() else gone() }
    }

    fun updateActionMenu(value: String) {
        actionMenu?.findViewById<MaterialTextView>(R.id.tv_selected_item)?.text = value
    }

    private fun initActionCancel() {
        actionMenu?.findViewById<ImageView>(R.id.iv_cancel)?.click {
            actionCancelCLick?.invoke()
        }
    }

    private fun initMenu(menuId: Int) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(menuId, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return onMenuItemSelected(menuItem.itemId)
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun init() {
        binding = getViewBinding()
        viewModel = if (useSharedViewModel) {
            ViewModelProvider(requireActivity())[getViewModelClass()]
        } else {
            ViewModelProvider(this)[getViewModelClass()]
        }
    }
}
