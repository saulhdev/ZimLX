/*
 * 2020 Zim Launcher
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

package org.zimmob.zimlx;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Process;
import android.os.UserHandle;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.DrawableFactory;
import com.aosp.launcher.icons.clock.DynamicClock;

public class DynamicDrawableFactory extends DrawableFactory {
    private DynamicClock mDynamicClockDrawer;
    private Context mContext;

    public DynamicDrawableFactory(Context context) {
        mDynamicClockDrawer = new DynamicClock(context);
        mContext = context;
    }

    public FastBitmapDrawable newIcon(ItemInfoWithIcon info) {
        if (info == null || info.itemType != 0 ||
                !DynamicClock.DESK_CLOCK.equals(info.getTargetComponent()) ||
                !info.user.equals(Process.myUserHandle())) {
            assert info != null;
            return super.newIcon(mContext, info);
        }
        FastBitmapDrawable dVar = mDynamicClockDrawer.drawIcon(info);
        dVar.setIsDisabled(info.isDisabled());
        return dVar;
    }

    public FastBitmapDrawable newIcon(ItemInfoWithIcon icon, ActivityInfo info) {
        if (DynamicClock.DESK_CLOCK.getPackageName().equals(info.packageName) &&
                (!Utilities.ATLEAST_NOUGAT || UserHandle.getUserHandleForUid(info.applicationInfo.uid).equals(Process.myUserHandle()))) {
            return mDynamicClockDrawer.drawIcon(icon);
        }
        return super.newIcon(mContext, icon);
    }
}
