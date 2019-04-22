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
import android.content.SharedPreferences
import android.graphics.Rect
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.graphics.IconShapeOverride
import org.zimmob.zimlx.folder.FolderShape

class SettingsSearchLayout(context: Context, attrs: AttributeSet?) : InsettableFrameLayout(context, attrs), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onFinishInflate() {
        super.onFinishInflate()
        Utilities.getPrefs(context).registerOnSharedPreferenceChangeListener(this)
        //updateRadius()
    }

    fun updateRadius() {
        val edgeRadius = FolderShape.sInstance.mAttrs.get(R.attr.qsbEdgeRadius)
        if (edgeRadius != null) {
            findViewById<CardView>(R.id.search_bar).radius = edgeRadius.getDimension(resources.displayMetrics)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == IconShapeOverride.KEY_PREFERENCE) {
            updateRadius()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Utilities.getPrefs(context).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun setInsets(insets: Rect) {
        setPadding(0, insets.top, 0, 0)
        super.setInsets(Rect(insets.left, 0, insets.right, insets.bottom))
    }
}
