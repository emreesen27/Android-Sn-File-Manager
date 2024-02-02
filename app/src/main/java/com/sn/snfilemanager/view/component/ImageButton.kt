package com.sn.snfilemanager.view.component

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textview.MaterialTextView
import com.sn.snfilemanager.R

class ImageButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : ConstraintLayout(context, attrs) {
        private var textView: MaterialTextView
        private var imageView: AppCompatImageView

        private var text: String
            get() = textView.text.toString()
            set(value) {
                textView.text = value
            }

        init {
            inflate(context, R.layout.layout_image_button, this)

            textView = findViewById(R.id.tv_text)
            imageView = findViewById(R.id.iv_image)

            val attributes = context.obtainStyledAttributes(attrs, R.styleable.ImageButton)

            text = attributes.getString(R.styleable.ImageButton_btnText) ?: ""
            imageView.setImageDrawable(attributes.getDrawable(R.styleable.ImageButton_btnImage))

            attributes.recycle()
        }
    }
