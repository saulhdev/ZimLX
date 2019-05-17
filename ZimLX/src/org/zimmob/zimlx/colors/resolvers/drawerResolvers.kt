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

package org.zimmob.zimlx.colors.resolvers

import androidx.annotation.Keep
import androidx.core.graphics.ColorUtils
import com.android.launcher3.R
import com.android.launcher3.util.Themes
import org.zimmob.zimlx.ZimLauncher
import org.zimmob.zimlx.ZimPreferences
import org.zimmob.zimlx.colors.ColorEngine
import org.zimmob.zimlx.colors.WallpaperColorResolver
import org.zimmob.zimlx.theme.ThemeManager

@Keep
class DrawerQsbAutoResolver(config: Config) : ColorEngine.ColorResolver(config), ZimPreferences.OnPreferenceChangeListener {

    private val isDark get() = ThemeManager.getInstance(engine.context).isDark
    private val lightResolver = DrawerQsbLightResolver(Config("DrawerQsbAutoResolver@Light", engine, { _, _ ->
        if (!isDark) notifyChanged()
    }))
    private val darkResolver = DrawerQsbDarkResolver(Config("DrawerQsbAutoResolver@Dark", engine, { _, _ ->
        if (isDark) notifyChanged()
    }))

    override fun startListening() {
        super.startListening()
        ZimPreferences.getInstanceNoCreate().addOnPreferenceChangeListener(this, "pref_launcherTheme")
    }

    override fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean) {
        notifyChanged()
    }

    override fun stopListening() {
        super.stopListening()
        ZimPreferences.getInstanceNoCreate().removeOnPreferenceChangeListener(this, "pref_launcherTheme")
    }

    override fun resolveColor() = if (isDark) darkResolver.resolveColor() else lightResolver.resolveColor()

    override fun getDisplayName() = engine.context.resources.getString(R.string.theme_based)
}

@Keep
class DrawerQsbLightResolver(config: Config) : WallpaperColorResolver(config), ZimPreferences.OnPreferenceChangeListener {

    private val isDark get() = ThemeManager.getInstance(engine.context).isDark
    val launcher = ZimLauncher.getLauncher(engine.context)

    override fun startListening() {
        super.startListening()
        ZimPreferences.getInstanceNoCreate().addOnPreferenceChangeListener(this, "pref_launcherTheme")
    }

    override fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean) {
        notifyChanged()
    }

    override fun stopListening() {
        super.stopListening()
        ZimPreferences.getInstanceNoCreate().removeOnPreferenceChangeListener(this, "pref_launcherTheme")
    }

    override fun resolveColor() = engine.context.resources.getColor(
            if (isDark)
                R.color.qsb_background_drawer_dark
            else
                R.color.qsb_background_drawer_default
    ).let {
        ColorUtils.compositeColors(ColorUtils
                .compositeColors(it, Themes.getAttrColor(launcher, R.attr.allAppsScrimColor)),
                colorInfo.mainColor)
    }

    override fun getDisplayName() = engine.context.resources.getString(R.string.theme_light)
}

@Keep
class DrawerQsbDarkResolver(config: Config) : WallpaperColorResolver(config) {

    val color = engine.context.resources.getColor(R.color.qsb_background_drawer_dark_bar)
    val launcher = ZimLauncher.getLauncher(engine.context)

    override fun resolveColor() = ColorUtils.compositeColors(ColorUtils
            .compositeColors(color, Themes.getAttrColor(launcher, R.attr.allAppsScrimColor)),
            colorInfo.mainColor)

    override fun getDisplayName() = engine.context.resources.getString(R.string.theme_dark)
}