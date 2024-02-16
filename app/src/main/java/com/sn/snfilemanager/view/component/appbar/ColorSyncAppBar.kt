package com.sn.snfilemanager.view.component.appbar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.sn.snfilemanager.R

class ColorSyncAppBar : AppBarLayout {
    var colorOnChange: ColorOnChange? = null
    private var adjustStatusBarColor: Boolean = false
    private var defaultStatusBarColor: Int = resources.getColor(R.color.bg_color, context.theme)
    private var currentColor: Int = resources.getColor(R.color.bg_color, context.theme)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        adjustStatusBarColor(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        adjustStatusBarColor(context, attrs)
    }

    constructor(context: Context) : super(context)

    private fun adjustStatusBarColor(
        context: Context,
        attrs: AttributeSet,
    ) {
        val activity = getActivityFromContext(context) ?: return
        val styledAttributes =
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ColorSyncAppBar,
                0,
                0,
            )

        try {
            adjustStatusBarColor =
                styledAttributes.getBoolean(
                    R.styleable.ColorSyncAppBar_adjustStatusBarColorOnLift, false,
                )
            defaultStatusBarColor =
                styledAttributes.getColor(
                    R.styleable.ColorSyncAppBar_defaultStatusBarColor,
                    resources.getColor(R.color.bg_color, context.theme),
                )

            activity.window.statusBarColor = defaultStatusBarColor

            if (adjustStatusBarColor) {
                addLiftOnScrollListener { _, backgroundColor ->
                    activity.window.statusBarColor = backgroundColor
                    currentColor = backgroundColor
                    colorOnChange?.colorOnChange(backgroundColor)
                }
            }
        } finally {
            styledAttributes.recycle()
        }
    }

    fun sync(reset: Boolean = false) {
        val activity = getActivityFromContext(context)
        activity?.window?.statusBarColor = if (reset) defaultStatusBarColor else currentColor
    }

    fun getCurrentColor(): Int = currentColor

    private fun getActivityFromContext(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivityFromContext(context.baseContext)
            else -> null
        }
    }
}
