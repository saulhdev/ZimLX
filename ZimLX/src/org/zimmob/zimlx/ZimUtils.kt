package org.zimmob.zimlx

import android.R
import android.content.res.ColorStateList
import android.view.View
import android.widget.Switch
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import com.android.launcher3.util.Themes

class ZimUtils {
    fun Switch.applyColor(color: Int) {
        val colorForeground = Themes.getAttrColor(context, android.R.attr.colorForeground)
        val alphaDisabled = Themes.getAlpha(context, android.R.attr.disabledAlpha)
        val switchThumbNormal = context.resources.getColor(androidx.preference.R.color.switch_thumb_normal_material_light)
        val switchThumbDisabled = context.resources.getColor(androidx.appcompat.R.color.switch_thumb_disabled_material_light)
        val thstateList = ColorStateList(arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()),
                intArrayOf(
                        switchThumbDisabled,
                        color,
                        switchThumbNormal))
        val trstateList = ColorStateList(arrayOf(
                intArrayOf(-R.attr.state_enabled),
                intArrayOf(R.attr.state_checked),
                intArrayOf()),
                intArrayOf(
                        ColorUtils.setAlphaComponent(colorForeground, alphaDisabled),
                        color,
                        colorForeground))
        DrawableCompat.setTintList(thumbDrawable, thstateList)
        DrawableCompat.setTintList(trackDrawable, trstateList)
    }
}

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }
