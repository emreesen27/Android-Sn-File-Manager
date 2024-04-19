package com.sn.snfilemanager.view.dialog

import com.sn.snfilemanager.core.base.BaseDialog
import com.sn.snfilemanager.databinding.DialogRootSelectorBinding

class RootSelectorDialog : BaseDialog<DialogRootSelectorBinding>() {
    override fun getViewBinding() = DialogRootSelectorBinding.inflate(layoutInflater)

    override val dialogTag: String
        get() = "ROOT_SELECTOR_DIALOG"
}
