package org.zimmob.zimlx.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import androidx.preference.AndroidResources
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import org.zimmob.zimlx.applyColor
import org.zimmob.zimlx.colors.ColorEngine

open class ZlSwitchPreference(context: Context, attrs: AttributeSet?)
    : SwitchPreference(context, attrs), ColorEngine.OnColorChangeListener {

    private var checkableView: View? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        checkableView = holder?.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET)

        ColorEngine.getInstance(context).addColorChangeListeners(this, arrayOf(ColorEngine.Resolvers.ACCENT))
    }

    override fun onColorChange(resolver: String, color: Int) {
        if (resolver == ColorEngine.Resolvers.ACCENT && checkableView is Switch) {
            (checkableView as Switch).applyColor(color)
        }
    }
}