package com.sn.snfilemanager

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import com.skydoves.powermenu.CircularEffect
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.skydoves.powermenu.kotlin.createPowerMenu


class PowerMenuFactory : PowerMenu.Factory() {
    override fun create(context: Context, lifecycle: LifecycleOwner): PowerMenu {
        return createPowerMenu(context) {
            addItemList(
                listOf(
                    PowerMenuItem(title = context.resources?.getString(R.string.settings)),
                    PowerMenuItem(title = context.resources?.getString(R.string.about))
                )
            )
            setAutoDismiss(true)
            setLifecycleOwner(lifecycle)
            setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
            setCircularEffect(CircularEffect.BODY)
            setMenuRadius(10f)
            setMenuShadow(10f)
            setTextColorResource(R.color.first_text_color)
            ResourcesCompat.getFont(context, R.font.adamina)?.let { setTextTypeface(it) }
            setTextSize(15)
            setTextGravity(Gravity.CENTER)
            setMenuColor(Color.WHITE)
            setSelectedMenuColorResource(R.color.white)
            setSelectedTextColor(
                context.resources.getColor(
                    R.color.first_text_color,
                    context.theme
                )
            )
        }
    }
}
