package com.sn.snfilemanager.core.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Observe live data
 * */
fun <T> LifecycleOwner.observe(
    liveData: LiveData<T>?,
    observer: (T) -> Unit,
) {
    liveData?.observe(this, Observer(observer))
}
