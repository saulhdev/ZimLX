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

package org.zimmob.zimlx.iconpack

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.android.launcher3.*
import com.android.launcher3.compat.LauncherAppsCompat
import com.android.launcher3.compat.UserManagerCompat
import com.android.launcher3.shortcuts.DeepShortcutManager
import com.android.launcher3.util.ComponentKey
import com.aosp.launcher.icons.ThirdPartyDrawableFactory
import com.aosp.launcher.icons.ThirdPartyIconProvider
import com.aosp.launcher.icons.calendar.DynamicCalendar
import com.aosp.launcher.icons.clock.DynamicClock
import org.zimmob.zimlx.util.getLauncherActivityInfo

class DefaultPack(context: Context) : IconPack(context, "") {

    val dynamicClockDrawer by lazy { DynamicClock(context) }

    private val appMap = HashMap<ComponentKey, Entry>().apply {
        val launcherApps = LauncherAppsCompat.getInstance(context)
        UserManagerCompat.getInstance(context).userProfiles.forEach { user ->
            launcherApps.getActivityList(null, user).forEach {
                put(ComponentKey(it.componentName, user), Entry(it, context))
            }
        }
    }
    override val entries get() = appMap.values.toList()

    init {
        executeLoadPack()
    }

    override val packInfo = IconPackList.DefaultPackInfo(context)

    override fun onDateChanged() {
        val model = LauncherAppState.getInstance(context).model
        UserManagerCompat.getInstance(context).userProfiles.forEach { user ->
            model.onPackageChanged(DynamicCalendar.CALENDAR, user)
            val shortcuts = DeepShortcutManager.getInstance(context).queryForPinnedShortcuts(DynamicCalendar.CALENDAR, user)
            if (!shortcuts.isEmpty()) {
                model.updatePinnedShortcuts(DynamicCalendar.CALENDAR, shortcuts, user)
            }
        }
    }

    override fun loadPack() {

    }

    override fun getEntryForComponent(key: ComponentKey) = appMap[key]

    override fun getIcon(entry: IconPackManager.CustomIconEntry, iconDpi: Int): Drawable? {
        return getIcon(ComponentKey(ComponentName(context, entry.packPackageName), Utilities.myUserHandle()), iconDpi)
    }

    fun getIcon(key: ComponentKey, iconDpi: Int): Drawable? {
        ensureInitialLoadComplete()

        val info = key.getLauncherActivityInfo(context) ?: return null
        //val component = key.componentName
        val originalIcon = info.getIcon(iconDpi).apply { mutate() }
        //var roundIcon: Drawable? = null

        return originalIcon
    }

    override fun getIcon(launcherActivityInfo: LauncherActivityInfo,
                         iconDpi: Int, flattenDrawable: Boolean,
                         customIconEntry: IconPackManager.CustomIconEntry?,
                         iconProvider: ThirdPartyIconProvider?): Drawable {
        ensureInitialLoadComplete()

        val key: ComponentKey
        val info: LauncherActivityInfo
        if (customIconEntry != null && !TextUtils.isEmpty(customIconEntry.icon)) {
            key = ComponentKey(ComponentName(context, customIconEntry.icon!!), Utilities.myUserHandle())
            info = key.getLauncherActivityInfo(context) ?: launcherActivityInfo
        } else {
            key = ComponentKey(launcherActivityInfo.componentName, launcherActivityInfo.user)
            info = launcherActivityInfo
        }
        val component = key.componentName
        val packageName = component.packageName
        val originalIcon = info.getIcon(iconDpi).apply { mutate() }
        if (iconProvider == null || (DynamicCalendar.CALENDAR != packageName && DynamicClock.DESK_CLOCK != component)) {
            var roundIcon: Drawable? = null
            iconProvider!!.getRoundIcon(component, iconDpi)?.let {
                roundIcon = it.apply { mutate() }
            }
            //val gen = AdaptiveIconGenerator(context, roundIcon ?: originalIcon)
            //return gen.result
        }
        return originalIcon
        //return iconProvider.getDynamicIcon(info, iconDpi, flattenDrawable)
    }

    override fun getIcon(shortcutInfo: ShortcutInfo, iconDpi: Int): Drawable? {
        ensureInitialLoadComplete()

        val drawable = DeepShortcutManager.getInstance(context).getShortcutIconDrawable(shortcutInfo, iconDpi)
        //val gen = AdaptiveIconGenerator(context, drawable)
        //return gen.result
        return drawable
    }

    override fun newIcon(icon: Bitmap, itemInfo: ItemInfoWithIcon,
                         customIconEntry: IconPackManager.CustomIconEntry?,
                         drawableFactory: ThirdPartyDrawableFactory): FastBitmapDrawable {
        ensureInitialLoadComplete()

        if (Utilities.ATLEAST_OREO && itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            val component = if (customIconEntry?.icon != null) {
                ComponentKey(ComponentName(context, customIconEntry.packPackageName), Utilities.myUserHandle()).componentName
            } else {
                itemInfo.targetComponent
            }
            if (DynamicClock.DESK_CLOCK == component) {
                return dynamicClockDrawer.drawIcon(itemInfo)
            }
        }

        return FastBitmapDrawable(icon)
    }

    override fun supportsMasking(): Boolean = false

    class Entry(private val app: LauncherActivityInfo, context: Context) : IconPack.Entry() {

        override val displayName by lazy { app.label.toString() }
        override val identifierName = ComponentKey(app.componentName, app.user).toString()
        override val isAvailable = true
        val customAdaptiveIcon by lazy { CustomAdaptiveIcon(context) }

        override fun drawableForDensity(density: Int): Drawable {
            return customAdaptiveIcon.wrap(app.getIcon(density)!!)
        }

        override fun toCustomEntry() = IconPackManager.CustomIconEntry("", ComponentKey(app.componentName, app.user).toString())
    }
}