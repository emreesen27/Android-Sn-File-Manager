package com.sn.snfilemanager.feature.conflict

import com.sn.mediastorepv.data.ConflictStrategy

interface ConflictDialogListener {
    /**
     * Triggered when cancel button is clicked
     */
    fun onCancel()


    /**
     * Triggered when dialog closes
     */
    fun onDismiss()

    /**
     * Triggered when conflict strategy is selected
     */
    fun onConflictSelected(conflictStrategy: ConflictStrategy, isAll: Boolean)
}