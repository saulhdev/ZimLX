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

package org.zimmob.zimlx.override

import android.content.Context
import com.android.launcher3.FolderInfo
import com.android.launcher3.R
import org.zimmob.zimlx.ensureOnMainThread
import org.zimmob.zimlx.iconpack.IconPackManager
import org.zimmob.zimlx.useApplicationContext
import org.zimmob.zimlx.util.SingletonHolder
import org.zimmob.zimlx.zimPrefs

class FolderInfoProvider(context: Context) : CustomInfoProvider<FolderInfo>(context) {

    private val prefs = context.zimPrefs

    override fun getTitle(info: FolderInfo): String {
        return info.title.toString()
    }

    override fun getDefaultTitle(info: FolderInfo): String {
        return context.getString(R.string.folder_hint_text)
    }

    override fun getCustomTitle(info: FolderInfo): String? {
        return info.title.toString()
    }

    override fun setTitle(info: FolderInfo, title: String?) {
        info.setTitle(title ?: "")
    }

    override fun setIcon(info: FolderInfo, entry: IconPackManager.CustomIconEntry?) {
        prefs.customAppIcon[info.toComponentKey()] = entry
    }

    override fun getIcon(info: FolderInfo): IconPackManager.CustomIconEntry? {
        return prefs.customAppIcon[info.toComponentKey()]
    }

    override fun supportsIcon() = true

    override fun setSwipeUpAction(info: FolderInfo, action: String?) {
        info.setSwipeUpAction(context, action)
    }

    override fun getSwipeUpAction(info: FolderInfo): String? {
        return info.swipeUpAction
    }

    companion object : SingletonHolder<FolderInfoProvider, Context>(ensureOnMainThread(
            useApplicationContext(::FolderInfoProvider)))
}
