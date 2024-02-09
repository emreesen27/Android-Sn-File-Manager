package com.sn.snfilemanager.core.util

import android.view.View

class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeCLick: (View) -> Unit,
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTimeClicked >= defaultInterval) {
            lastTimeClicked = currentTime
            onSafeCLick(v)
        }
    }
}
