package com.sn.snfilemanager.feature.conflict

import com.sn.snfilemanager.providers.mediastore.MediaFile

interface ConflictDialogListener {
    /**
     * Triggered when cancel button is clicked
     */
    fun onCancel()

    /**
     * Triggered when apply button is clicked
     */
    fun onApply(newList: MutableList<MediaFile>)
}