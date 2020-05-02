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

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.android.launcher3.*
import com.android.launcher3.util.ComponentKey
import org.zimmob.zimlx.override.AppInfoProvider
import org.zimmob.zimlx.override.CustomInfoProvider
import org.zimmob.zimlx.util.asNonEmpty
import org.zimmob.zimlx.util.runOnMainThread
import org.zimmob.zimlx.util.zimPrefs
import java.util.*

class IconPackManager(private val context: Context) {

    val prefs = context.zimPrefs
    private val appInfoProvider = AppInfoProvider.getInstance(context)
    val defaultPack = DefaultPack(context)
    val uriPack = UriIconPack(context)
    var dayOfMonth = 0
        set(value) {
            if (value != field) {
                field = value
                onDateChanged()
            }
        }

    val packList = IconPackList(context, this)
    val defaultPackProvider = PackProvider(privateObj, "")

    private val listeners: MutableSet<() -> Unit> = mutableSetOf()

    init {
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            }
        }, IntentFilter(Intent.ACTION_DATE_CHANGED).apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            if (!Utilities.ATLEAST_NOUGAT) {
                addAction(Intent.ACTION_TIME_TICK)
            }
        }, null, Handler(LauncherModel.getWorkerLooper()))
    }

    private fun onDateChanged() {
        packList.onDateChanged()
    }

    private fun getIconPackInternal(name: String, put: Boolean = true, load: Boolean = false): IconPack? {
        if (name == defaultPack.packPackageName) return defaultPack
        if (name == uriPack.packPackageName) return uriPack
        return if (isPackProvider(context, name)) {
            packList.getPack(name, put).apply {
                if (load) {
                    ensureInitialLoadComplete()
                }
            }
        } else null
    }

    fun getIconPack(name: String, put: Boolean = true, load: Boolean = false): IconPack? {
        return getIconPackInternal(name, put, load)
    }

    fun getIconPack(packProvider: PackProvider, put: Boolean = true, load: Boolean = false): IconPack {
        return getIconPackInternal(packProvider.name, put, load)!!
    }

    @SuppressLint("WrongConstant")
    fun getIcon(launcherActivityInfo: LauncherActivityInfo,
                iconDpi: Int, flattenDrawable: Boolean, itemInfo: ItemInfo?,
                iconProvider: ZimIconProvider?): Drawable {
        val customEntry = CustomInfoProvider.forItem<ItemInfo>(context, itemInfo)?.getIcon(itemInfo!!)
                ?: appInfoProvider.getCustomIconEntry(launcherActivityInfo)
        val customPack = customEntry?.run {
            getIconPackInternal(packPackageName)
        }
        if (customPack != null) {
            customPack.getIcon(launcherActivityInfo, iconDpi,
                    flattenDrawable, customEntry, iconProvider)?.let { icon -> return icon }
        }
        packList.iterator().forEach { pack ->
            pack.getIcon(launcherActivityInfo, iconDpi,
                    flattenDrawable, null, iconProvider)?.let { return it }
        }
        return defaultPack.getIcon(launcherActivityInfo, iconDpi, flattenDrawable, null, iconProvider)
    }

    fun getIcon(shortcutInfo: ShortcutInfo, iconDpi: Int): Drawable? {
        packList.iterator().forEach { pack ->
            pack.getIcon(shortcutInfo, iconDpi)?.let { return it }
        }
        return defaultPack.getIcon(shortcutInfo, iconDpi)
    }

    fun newIcon(icon: Bitmap, itemInfo: ItemInfoWithIcon, drawableFactory: ZimDrawableFactory): FastBitmapDrawable {
        val key = itemInfo.targetComponent?.let { ComponentKey(it, itemInfo.user) }
        val customEntry = CustomInfoProvider.forItem<ItemInfo>(context, itemInfo)?.getIcon(itemInfo)
                ?: key?.let { appInfoProvider.getCustomIconEntry(it) }
        val customPack = customEntry?.run { getIconPackInternal(packPackageName) }
        if (customPack != null) {
            customPack.newIcon(icon, itemInfo, customEntry, drawableFactory)?.let { return it }
        }
        packList.iterator().forEach { pack ->
            pack.newIcon(icon, itemInfo, customEntry, drawableFactory)?.let { return it }
        }
        return defaultPack.newIcon(icon, itemInfo, customEntry, drawableFactory)
    }

    fun maskSupported(): Boolean = packList.appliedPacks.any { it.supportsMasking() }

    fun getEntryForComponent(component: ComponentKey): IconPack.Entry? {
        packList.iterator().forEach {
            val entry = it.getEntryForComponent(component)
            if (entry != null) return entry
        }
        return defaultPack.getEntryForComponent(component)
    }

    fun getPackProviders(): Set<PackProvider> {
        val pm = context.packageManager
        val packs = HashSet<PackProvider>()
        ICON_INTENTS.forEach { intent ->
            pm.queryIntentActivities(Intent(intent), PackageManager.GET_META_DATA).forEach {
                packs.add(PackProvider(privateObj, it.activityInfo.packageName))
            }
        }
        return packs
    }

    fun getPackProviderInfos(): HashMap<String, IconPackInfo> {
        val pm = context.packageManager
        val packs = HashMap<String, IconPackInfo>()
        ICON_INTENTS.forEach { intent ->
            pm.queryIntentActivities(Intent(intent), PackageManager.GET_META_DATA).forEach {
                packs[it.activityInfo.packageName] = IconPackInfo.fromResolveInfo(it, pm)
            }
        }
        return packs
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
        if (!packList.appliedPacks.isEmpty()) {
            listener.invoke()
        }
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    fun onPacksUpdated() {
        context.zimPrefs.reloadIcons()
        runOnMainThread { listeners.forEach { it.invoke() } }
    }

    data class CustomIconEntry(val packPackageName: String, val icon: String? = null, val arg: String? = null) {

        fun toPackString(): String {
            return "$packPackageName"
        }

        override fun toString(): String {
            return "$packPackageName|${icon ?: ""}|${arg ?: ""}"
        }

        companion object {
            fun fromString(string: String): CustomIconEntry {
                return fromNullableString(string)!!
            }

            fun fromNullableString(string: String?): CustomIconEntry? {
                if (string == null) return null
                if (string.contains("|")) {
                    val parts = string.split("|")
                    if (parts[0].contains("/")) return parseLegacy(string)
                    if (parts.size == 1) {
                        return CustomIconEntry(parts[0])
                    }
                    return CustomIconEntry(parts[0], parts[1].asNonEmpty(), parts[2].asNonEmpty())
                }
                return parseLegacy(string)
            }

            private fun parseLegacy(string: String): CustomIconEntry? {
                val parts = string.split("/")
                val icon = TextUtils.join("/", parts.subList(1, parts.size))
                if (parts[0] == "zimUriPack" && !icon.isNullOrBlank()) {
                    val iconParts = icon.split("|")
                    return CustomIconEntry(parts[0], iconParts[0].asNonEmpty(), iconParts[1].asNonEmpty())
                }
                return CustomIconEntry(parts[0], icon.asNonEmpty())
            }
        }
    }

    data class IconPackInfo(val packageName: String, val icon: Drawable, val label: CharSequence) {
        companion object {
            fun fromResolveInfo(info: ResolveInfo, pm: PackageManager) = IconPackInfo(info.activityInfo.packageName, info.loadIcon(pm), info.loadLabel(pm))
        }
    }

    class PackProvider(obj: Any, val name: String) : Parcelable {

        constructor(parcel: Parcel) : this(privateObj, parcel.readString()!!)

        init {
            if (obj !== privateObj) {
                UnsupportedOperationException("Cannot create PackProvider outside of IconPackManager")
            }
        }

        override fun equals(other: Any?): Boolean {
            return (other as? PackProvider)?.name == name
        }

        override fun hashCode() = name.hashCode()

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PackProvider> {
            override fun createFromParcel(parcel: Parcel): PackProvider {
                return PackProvider(parcel)
            }

            override fun newArray(size: Int): Array<PackProvider?> {
                return arrayOfNulls(size)
            }
        }
    }

    interface OnPackChangeListener {
        fun onPackChanged()
    }

    companion object {

        const val TAG = "IconPackManager"
        private val privateObj = Object()

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: IconPackManager? = null

        fun getInstance(context: Context): IconPackManager {
            if (INSTANCE == null) {
                INSTANCE = IconPackManager(context.applicationContext)
            }
            return INSTANCE!!
        }

        val ICON_INTENTS = arrayOf(
                "com.novalauncher.THEME",
                "org.adw.launcher.THEMES",
                "org.adw.launcher.icons.ACTION_PICK_ICON",
                "com.anddoes.launcher.THEME",
                "com.teslacoilsw.launcher.THEME",
                "com.fede.launcher.THEME_ICONPACK",
                "com.gau.go.launcherex.theme",
                "com.dlto.atom.launcher.THEME",
                "net.oneplus.launcher.icons.ACTION_PICK_ICON",
                "org.zimmob.zimlx.ICONPACK")

        internal fun isPackProvider(context: Context, packageName: String?): Boolean {
            if (packageName != null && !packageName.isEmpty()) {
                return ICON_INTENTS.firstOrNull {
                    context.packageManager.queryIntentActivities(
                            Intent(it).setPackage(packageName), PackageManager.GET_META_DATA).iterator().hasNext()
                } != null
            }
            return false
        }
    }
}
