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

package org.zimmob.zimlx

import android.content.Context
import android.util.TypedValue
import com.android.launcher3.R
import org.zimmob.zimlx.util.SingletonHolder

class ZimConfig(context: Context) {

    val defaultEnableBlur = context.resources.getBoolean(R.bool.config_default_enable_blur)
    val defaultBlurStrength = TypedValue().apply {
        context.resources.getValue(R.dimen.config_default_blur_strength, this, true)
    }.float
    val defaultIconPacks = context.resources.getStringArray(R.array.config_default_icon_packs)
    val enableLegacyTreatment = context.resources.getBoolean(R.bool.config_enable_legacy_treatment)
    val enableColorizedLegacyTreatment = context.resources.getBoolean(R.bool.config_enable_colorized_legacy_treatment)
    val enableWhiteOnlyTreatment = context.resources.getBoolean(R.bool.config_enable_white_only_treatment)
    val hideStatusBar = context.resources.getBoolean(R.bool.config_hide_statusbar)
    val enableSmartspace = context.resources.getBoolean(R.bool.config_enable_smartspace)
    val defaultSearchProvider: String = context.resources.getString(
            R.string.config_default_search_provider)
    val defaultColorResolver: String = context.resources.getString(
            R.string.config_default_color_resolver)

    val minibarItems = setOf("11", "12", "13", "14", "15", "16", "17", "18")

    companion object : SingletonHolder<ZimConfig, Context>(
            ensureOnMainThread(useApplicationContext(::ZimConfig)))
}