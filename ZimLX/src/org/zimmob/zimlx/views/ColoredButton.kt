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

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.widget.Button
import com.android.launcher3.Utilities
import org.zimmob.zimlx.getTabRipple


class ColoredButton(context: Context, attrs: AttributeSet) : Button(context, attrs) {

    var color: Int = 0

    private var defaultColor = currentTextColor

    init {
        color = Utilities.getZimPrefs(context).accentColor
        //CustomFontManager.getInstance(context).loadCustomFont(this, attrs)
    }

    fun reset() {
        color = Utilities.getZimPrefs(context).accentColor
        setTextColor()
        setRippleColor()
    }

    private fun setTextColor() {
        val stateList = ColorStateList(arrayOf(
                intArrayOf(R.attr.state_selected),
                intArrayOf()),
                intArrayOf(
                        color,
                        defaultColor))
        setTextColor(stateList)
    }

    private fun setRippleColor() {
        background = RippleDrawable(getTabRipple(context, color), null, null)
    }
}
