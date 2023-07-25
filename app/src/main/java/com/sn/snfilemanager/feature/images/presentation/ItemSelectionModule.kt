package com.sn.snfilemanager.feature.images.presentation

import com.idanatz.oneadapter.external.modules.ItemSelectionModule
import com.idanatz.oneadapter.external.modules.ItemSelectionModuleConfig

class ItemSelectionModule : ItemSelectionModule() {

    interface Selection {
        fun onStartSelection()
        fun onUpdateSelection(selectedCount: Int)
        fun onEndSelection()
    }

    var selection: Selection? = null

    init {
        config {
            selectionType = ItemSelectionModuleConfig.SelectionType.Multiple
        }
        onStartSelection {
            selection?.onStartSelection()
        }
        onUpdateSelection { selectedCount ->
            selection?.onUpdateSelection(selectedCount)
        }
        onEndSelection {
            selection?.onEndSelection()
        }
    }
}