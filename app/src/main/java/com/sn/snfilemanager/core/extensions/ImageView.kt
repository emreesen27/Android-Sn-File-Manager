package com.sn.snfilemanager.core.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

fun ImageView.loadWithGlide(videoPath: Any) {
    Glide.with(context)
        .asBitmap()
        .load(videoPath)
        .fitCenter()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(this)
}
