/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */


package org.zimmob.zimlx.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.launcher3.Insettable
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.views.OptionsPopupView
import kotlinx.android.synthetic.zim.options_view.view.*
import org.zimmob.zimlx.ZimPreferences
import org.zimmob.zimlx.util.forEachChild
import org.zimmob.zimlx.util.isVisible
import org.zimmob.zimlx.util.zimPrefs

class OptionsPanel(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
        Insettable, View.OnClickListener, ZimPreferences.OnPreferenceChangeListener {

    private val launcher = Launcher.getLauncher(context)

    override fun onFinishInflate() {
        super.onFinishInflate()

        forEachChild { it.setOnClickListener(this) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.zimPrefs.addOnPreferenceChangeListener("pref_lockDesktop", this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.zimPrefs.removeOnPreferenceChangeListener("pref_lockDesktop", this)
    }

    override fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean) {
        widget_button.isVisible = !prefs.lockDesktop
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.wallpaper_button -> OptionsPopupView.startWallpaperPicker(v)
            R.id.widget_button -> OptionsPopupView.onWidgetsClicked(v)
            R.id.settings_button -> OptionsPopupView.startSettings(v)
        }
    }

    interface OnPreferenceChangeListener {

        fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean)
    }

    override fun setInsets(insets: Rect) {
        val deviceProfile = launcher.deviceProfile

        layoutParams = (layoutParams as FrameLayout.LayoutParams).also { lp ->
            lp.width = deviceProfile.availableWidthPx
            lp.height = (deviceProfile.availableHeightPx * (1 - deviceProfile.workspaceOptionsShrinkFactor)).toInt()
            lp.bottomMargin = insets.bottom
        }
    }
}