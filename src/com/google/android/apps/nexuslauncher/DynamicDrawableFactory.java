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

package com.google.android.apps.nexuslauncher;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;

import androidx.annotation.RequiresApi;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.util.ComponentKey;

import org.zimmob.zimlx.icons.calendar.DateChangeReceiver;
import org.zimmob.zimlx.icons.calendar.DynamicCalendar;
import org.zimmob.zimlx.icons.clock.CustomClock;
import org.zimmob.zimlx.icons.clock.DynamicClock;

import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;

public class DynamicDrawableFactory extends DrawableFactory {
    private final DynamicClock mDynamicClockDrawer;
    private final CustomClock mCustomClockDrawer;
    private final DateChangeReceiver mCalendars;
    private Context mContext;

    public DynamicDrawableFactory(Context context) {
        mContext = context;
        if (Utilities.ATLEAST_OREO) {
            mDynamicClockDrawer = new DynamicClock(context);
            mCustomClockDrawer = new CustomClock(context);
        } else {
            mDynamicClockDrawer = null;
            mCustomClockDrawer = null;
        }
        mCalendars = new DateChangeReceiver(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public FastBitmapDrawable newIcon(Context context, ItemInfoWithIcon info) {
        /*if (info == null || info.itemType != 0 ||
                !DynamicClock.DESK_CLOCK.equals(info.getTargetComponent()) ||
                !info.user.equals(Process.myUserHandle())) {
            return super.newIcon(info);
        }
        FastBitmapDrawable dVar = mDynamicClockDrawer.drawIcon(info);
        dVar.setIsDisabled(info.isDisabled());
        return dVar;*/
        if (info != null && info.getTargetComponent() != null && info.itemType == ITEM_TYPE_APPLICATION) {
            ComponentKey key = new ComponentKey(info.getTargetComponent(), info.user);

            //mCalendars.setIsDynamic(key, (resolver != null && resolver.isCalendar())
            //        || info.getTargetComponent().getPackageName().equals(DynamicCalendar.CALENDAR));

            if (Utilities.ATLEAST_OREO) {
                /*if (resolver != null) {
                    if (resolver.isClock()) {
                        Drawable drawable = resolver.getIcon(0, () -> null);
                        if (drawable != null) {
                            FastBitmapDrawable fb = mCustomClockDrawer.drawIcon(
                                    info, drawable, resolver.clockData());
                            fb.setIsDisabled(info.isDisabled());
                            return fb;
                        }
                    }
                } else*/
                if (info.getTargetComponent().equals(DynamicClock.DESK_CLOCK)) {
                    return mDynamicClockDrawer.drawIcon(info);
                }
            }
        }
        return super.newIcon(context, info);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public FastBitmapDrawable newIcon(BitmapInfo icon, ActivityInfo info) {
        /*if (DynamicClock.DESK_CLOCK.getPackageName().equals(info.packageName) &&
                (!Utilities.ATLEAST_NOUGAT || UserHandle.getUserHandleForUid(info.applicationInfo.uid).equals(Process.myUserHandle()))) {
            return mDynamicClockDrawer.drawIcon(info);
        }*/
        return super.newIcon(icon, info);
    }
}
