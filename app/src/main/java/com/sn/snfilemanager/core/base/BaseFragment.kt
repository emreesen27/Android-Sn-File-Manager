package com.sn.snfilemanager.core.base

import android.os.Bundle
import android.view.*
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
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.visible
import com.sn.snfilemanager.view.dialog.progress.ProgressDialog

abstract class BaseFragment<VBinding : ViewBinding, VModel : ViewModel> : Fragment() {

    private var progressDialog: ProgressDialog? = null
    private var toolbar: Toolbar? = null
    open var useSharedViewModel: Boolean = false

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
        toolbar = requireActivity().findViewById(R.id.toolbar)
        progressDialog = ProgressDialog(requireContext())
        setupViews()
        observeData()
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
        if (getActionBarStatus()) {
            setToolbarVisibility(true)
            (requireActivity() as? AppCompatActivity)?.let { activity ->
                activity.setSupportActionBar(activity.findViewById(R.id.toolbar))
            }
        } else {
            setToolbarVisibility(false)
        }
    }

    fun invalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }

    fun getToolbar(): Toolbar? = toolbar

    fun updateProgressDialog(value: Int) {
        if (progressDialog?.isShowing == false) {
            progressDialog?.show()
        }
        progressDialog?.setProgressValue(value)
    }

    fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun setToolbarVisibility(value: Boolean) {
        activity?.findViewById<Toolbar>(R.id.toolbar)?.apply {
            if (value) visible() else gone()
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
