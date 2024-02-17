package com.sn.snfilemanager.view.component.appbar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.sn.snfilemanager.R

class ColorSyncAppBar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppBarLayout(context, attrs, defStyleAttr) {
        private var activity: Activity? = null
        private var toolbar: MaterialToolbar? = null
        private var adjustStatusBarColor: Boolean = false
        private val defaultStatusBarColor: Int = getColor(context, R.color.bg_color)
        private val defaultBarStatus: Boolean = !isDark(context)
        private var currentBarStatus: Boolean = true
        private var currentColor: Int = defaultStatusBarColor

        init {
            activity = getActivityFromContext(context)
            attrs?.let { adjustBarColor(context, it) }
        }

        override fun onViewAdded(child: android.view.View?) {
            super.onViewAdded(child)
            if (child is MaterialToolbar) {
                toolbar = child
            }
        }

        private fun adjustBarColor(
            context: Context,
            attrs: AttributeSet,
        ) {
            val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.ColorSyncAppBar)
            try {
                activity?.let { activity ->
                    adjustStatusBarColor =
                        styledAttributes.getBoolean(
                            R.styleable.ColorSyncAppBar_adjustStatusBarColorOnLift, false,
                        )
                    setLightBarAppearance(activity, defaultBarStatus)
                    activity.window.statusBarColor = defaultStatusBarColor
                    if (adjustStatusBarColor) {
                        addLiftOnScrollListener { _, backgroundColor ->
                            activity.window.statusBarColor = backgroundColor
                            currentColor = backgroundColor
                            adjustToolbarColors(activity, backgroundColor == defaultStatusBarColor)
                        }
                    }
                }
            } finally {
                styledAttributes.recycle()
            }
        }

        private fun adjustToolbarColors(
            activity: Activity,
            isReset: Boolean,
        ) {
            val textColor: Int
            if (isReset) {
                textColor = getColor(context, R.color.first_text_color)
                setLightBarAppearance(activity, !isDark(context))
            } else {
                textColor = getColor(context, R.color.white)
                setLightBarAppearance(activity, false)
            }
            toolbar?.let { bar ->
                bar.setTitleTextColor(textColor)
                bar.overflowIcon?.setTint(textColor)

                for (i in 0 until bar.menu.size()) {
                    bar.menu.getItem(i).iconTintList = ColorStateList.valueOf(textColor)
                }
            }
        }

        private fun setLightBarAppearance(
            activity: Activity,
            value: Boolean,
        ) {
            currentBarStatus = value
            WindowInsetsControllerCompat(
                activity.window,
                activity.window.decorView,
            ).isAppearanceLightStatusBars = value
        }

        private fun isDark(context: Context): Boolean {
            return context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }

        private fun getActivityFromContext(context: Context): Activity? {
            var currentContext = context
            while (currentContext is ContextWrapper) {
                if (currentContext is Activity) {
                    return currentContext
                }
                currentContext = currentContext.baseContext
            }
            return null
        }

        fun sync() {
            activity?.let { activity ->
                activity.window?.statusBarColor = currentColor
                setLightBarAppearance(
                    activity,
                    currentBarStatus,
                )
            }
        }
    }
