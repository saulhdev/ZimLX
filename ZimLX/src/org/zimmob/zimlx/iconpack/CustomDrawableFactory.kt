/*
 * Copyright (c) 2020 Zim Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.zimmob.zimlx.iconpack

import android.content.Context
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.ItemInfoWithIcon
import com.android.launcher3.Utilities
import com.android.launcher3.WorkspaceItemInfo
import com.android.launcher3.graphics.DrawableFactory
import com.aosp.launcher.icons.calendar.DateChangeReceiver
import com.aosp.launcher.icons.clock.CustomClock
import com.aosp.launcher.icons.clock.DynamicClock

class CustomDrawableFactory(context: Context) : DrawableFactory() {
    private val iconPackManager = IconPackManager.getInstance(context)
    private var mDynamicClockDrawer: DynamicClock? = null
    var mCustomClockDrawer: CustomClock? = null
    private var mCalendars: DateChangeReceiver? = null

    init {
        if (Utilities.ATLEAST_OREO) {
            mDynamicClockDrawer = DynamicClock(context)
            mCustomClockDrawer = CustomClock(context)
        } else {
            mDynamicClockDrawer = null
            mCustomClockDrawer = null
        }
        mCalendars = DateChangeReceiver(context)
    }

    override fun newIcon(context: Context?, info: ItemInfoWithIcon?): FastBitmapDrawable? {
        /*if (info != null && info.targetComponent != null && info.itemType == Favorites.ITEM_TYPE_APPLICATION) {
            val key = ComponentKey(info.targetComponent, info.user)
            val resolver = mManager!!.resolve(key)

            mCalendars!!.setIsDynamic(key, resolver != null && resolver.isCalendar
                    || info.targetComponent!!.packageName == DynamicCalendar.CALENDAR)
            if (Utilities.ATLEAST_OREO) {
                if (resolver != null) {
                    if (resolver.isClock) {
                        val drawable = resolver.getIcon(0) { null }
                        if (drawable != null) {
                            val fb = mCustomClockDrawer!!.drawIcon(
                                    info, drawable, resolver.clockData())
                            fb.setIsDisabled(info.isDisabled)
                            return fb
                        }
                    }
                } else if (info.targetComponent == DynamicClock.DESK_CLOCK) {
                    return mDynamicClockDrawer!!.drawIcon(info)
                }
            }
        }*/
        return info?.let {
            iconPackManager.newIcon((info as? WorkspaceItemInfo)?.customIcon ?: info.iconBitmap,
                    it, this).also { it.setIsDisabled(info.isDisabled) }
        }
    }
}