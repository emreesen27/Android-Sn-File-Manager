package com.sn.snfilemanager.view.dialog.sort

import androidx.fragment.app.viewModels
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.util.SortCriterion
import com.sn.snfilemanager.core.util.SortOrder
import com.sn.snfilemanager.databinding.DialogSortBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SortDialog(
    private var isMedia: Boolean = false,
    private val onConfirm: ((Pair<SortCriterion, SortOrder>) -> Unit)? = null,
) : BaseDialog<DialogSortBinding>() {
    override fun getViewBinding() = DialogSortBinding.inflate(layoutInflater)

    override val dialogTag: String
        get() = "SORT_DIALOG"

    private val vm: SortDialogViewModel by viewModels()
    private var criterion = SortCriterion.LAST_MODIFIED
    private var sortOrder = SortOrder.DESCENDING

    override fun setupViews() {
        setPrefsTag()
        initFirstValues()
        initRadioGroupListener()
        initButtonListener()
    }

    private fun setPrefsTag() {
        vm.setPrefsTag(isMedia)
    }

    private fun initFirstValues() {
        val (loadedCriterion, loadedSortOrder) = vm.getSortData()
        criterion = loadedCriterion
        sortOrder = loadedSortOrder

        with(binding) {
            when (criterion) {
                SortCriterion.NAME -> rbName.isChecked = true
                SortCriterion.LAST_MODIFIED -> rbModified.isChecked = true
                SortCriterion.EXTENSION -> rbExtension.isChecked = true
            }
            when (sortOrder) {
                SortOrder.ASCENDING -> rbAscending.isChecked = true
                SortOrder.DESCENDING -> rbDescending.isChecked = true
            }
        }
    }

    private fun initRadioGroupListener() {
        with(binding) {
            rgCriteria.setOnCheckedChangeListener { _, checkedId ->
                criterion =
                    when (checkedId) {
                        R.id.rb_name -> SortCriterion.NAME
                        R.id.rb_modified -> SortCriterion.LAST_MODIFIED
                        R.id.rb_extension -> SortCriterion.EXTENSION
                        else -> criterion
                    }
            }
            rgSortOrder.setOnCheckedChangeListener { _, checkedId ->
                sortOrder =
                    when (checkedId) {
                        R.id.rb_ascending -> SortOrder.ASCENDING
                        R.id.rb_descending -> SortOrder.DESCENDING
                        else -> sortOrder
                    }
            }
        }
    }

    private fun initButtonListener() {
        with(binding) {
            btnCancel.click { dismiss() }
            btnConfirm.click {
                vm.putSortData(criterion, sortOrder)
                onConfirm?.invoke(Pair(criterion, sortOrder))
                dismiss()
            }
        }
    }
}
