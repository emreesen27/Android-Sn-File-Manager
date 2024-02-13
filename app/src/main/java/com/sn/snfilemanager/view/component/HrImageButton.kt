package com.sn.snfilemanager.view.component

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textview.MaterialTextView
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.gone
import com.sn.snfilemanager.core.extensions.visible

class HrImageButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : ConstraintLayout(context, attrs) {
        private var titleTextView: MaterialTextView
        private var subTitleTextView: MaterialTextView
        private var iconImageView: AppCompatImageView

        var title: String
            get() = titleTextView.text.toString()
            set(value) {
                titleTextView.text = value
            }

        var subTitle: String?
            get() = subTitleTextView.text.toString()
            set(value) {
                subTitleTextView.text = value
                checkSubTitle()
            }

        init {
            inflate(context, R.layout.layout_hr_image_button, this)

            titleTextView = findViewById(R.id.tv_menu_title)
            subTitleTextView = findViewById(R.id.tv_menu_sub_title)
            iconImageView = findViewById(R.id.iv_image)

            val attributes = context.obtainStyledAttributes(attrs, R.styleable.HrImageButton)

            title = attributes.getString(R.styleable.HrImageButton_title) ?: ""
            subTitle = attributes.getString(R.styleable.HrImageButton_subTitle) ?: ""
            iconImageView.setImageDrawable(attributes.getDrawable(R.styleable.HrImageButton_icon))

            attributes.recycle()
        }

        private fun checkSubTitle() {
            if (subTitle.isNullOrEmpty()) {
                subTitleTextView.gone()
            } else {
                subTitleTextView.visible()
            }
        }
    }
