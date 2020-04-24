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

package org.zimmob.zimlx.settings.controllers

import android.content.Context
import androidx.annotation.Keep
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.android.launcher3.R
import org.zimmob.zimlx.override.IconShapeOverride
import org.zimmob.zimlx.settings.PreferenceController
import org.zimmob.zimlx.util.zimPrefs

@Keep
class IconShapeOverrideController(context: Context) : PreferenceController(context) {

    override val isVisible = IconShapeOverride.isSupported(context)

}
