package com.sn.snfilemanager.core.extensions

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sn.snfilemanager.R

fun ImageView.loadWithGlide(
    videoPath: Any,
    readyOrFailed: ((e: GlideException?) -> Unit)? = null,
) {
    Glide.with(context)
        .asBitmap()
        .load(videoPath)
        // .fitCenter()
        .placeholder(R.drawable.layer_placeholder)
        .error(R.drawable.layer_broken_placeholder)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .addListener(
            object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    readyOrFailed?.invoke(e)
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    readyOrFailed?.invoke(null)
                    return false
                }
            },
        )
        .into(this)
}
