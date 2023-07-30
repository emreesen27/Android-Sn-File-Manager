package com.sn.snfilemanager.feature.media.module

import com.idanatz.oneadapter.external.modules.ItemSelectionModule
import com.idanatz.oneadapter.external.modules.ItemSelectionModuleConfig

class MediaSelectionModule : ItemSelectionModule() {

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