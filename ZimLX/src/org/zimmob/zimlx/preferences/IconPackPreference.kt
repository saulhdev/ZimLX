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

package org.zimmob.zimlx.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.R
import org.zimmob.zimlx.iconpack.DefaultPack
import org.zimmob.zimlx.iconpack.IconPackManager

class IconPackPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    private val ipm = IconPackManager.getInstance(context)
    private val packList = ipm.packList

    init {
        layoutResource = R.layout.pref_with_preview_icon
        fragment = IconPackFragment::class.java.name
    }

    override fun onAttached() {
        super.onAttached()

        ipm.addListener(this::updatePreview)
    }

    override fun onDetached() {
        super.onDetached()

        ipm.removeListener(this::updatePreview)
    }

    private fun updatePreview() {
        try {
            summary = if (packList.currentPack() is DefaultPack) {
                packList.currentPack().displayName
            } else {
                packList.appliedPacks
                        .filter { it !is DefaultPack }
                        .joinToString(", ") { it.displayName }
            }
            icon = packList.currentPack().displayIcon
        } catch (ignored: IllegalStateException) {
            //TODO: Fix updating pref when scrolled down
        }
    }
}
