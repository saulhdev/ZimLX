/*
 * 2020 Zim Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zimmob.zimlx.icons.clock;

import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.TimeZone;

public class ClockLayers {
    private final Calendar mCurrentTime;
    AdaptiveIconDrawable mDrawable;
    LayerDrawable mLayerDrawable;
    int mHourIndex;
    int mMinuteIndex;
    int mSecondIndex;
    int mDefaultHour;
    int mDefaultMinute;
    int mDefaultSecond;
    float scale;
    float offset;
    Bitmap bitmap;

    ClockLayers() {
        mCurrentTime = Calendar.getInstance();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public final void setDrawable(Drawable drawable) {
        mDrawable = (AdaptiveIconDrawable) drawable;
        mLayerDrawable = (LayerDrawable) mDrawable.getForeground();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public ClockLayers clone() {
        ClockLayers ret = null;
        if (mDrawable == null) {
            return null;
        }
        ClockLayers clone = new ClockLayers();
        clone.scale = scale;
        clone.offset = offset;
        clone.mHourIndex = mHourIndex;
        clone.mMinuteIndex = mMinuteIndex;
        clone.mSecondIndex = mSecondIndex;
        clone.mDefaultHour = mDefaultHour;
        clone.mDefaultMinute = mDefaultMinute;
        clone.mDefaultSecond = mDefaultSecond;
        clone.setDrawable(mDrawable.getConstantState().newDrawable());
        clone.bitmap = bitmap;
        if (clone.mLayerDrawable != null) {
            ret = clone;
        }
        return ret;
    }

    boolean updateAngles() {
        mCurrentTime.setTimeInMillis(System.currentTimeMillis());

        int hour = (mCurrentTime.get(Calendar.HOUR) + (12 - mDefaultHour)) % 12;
        int minute = (mCurrentTime.get(Calendar.MINUTE) + (60 - mDefaultMinute)) % 60;
        int second = (mCurrentTime.get(Calendar.SECOND) + (60 - mDefaultSecond)) % 60;

        boolean hasChanged = false;
        if (mHourIndex != -1 && mLayerDrawable.getDrawable(mHourIndex).setLevel(hour * 60 + mCurrentTime.get(Calendar.MINUTE))) {
            hasChanged = true;
        }
        if (mMinuteIndex != -1 && mLayerDrawable.getDrawable(mMinuteIndex).setLevel(minute + mCurrentTime.get(Calendar.HOUR) * 60)) {
            hasChanged = true;
        }
        if (mSecondIndex != -1 && mLayerDrawable.getDrawable(mSecondIndex).setLevel(second * 10)) {
            hasChanged = true;
        }
        return hasChanged;
    }

    void setTimeZone(TimeZone timeZone) {
        mCurrentTime.setTimeZone(timeZone);
    }
}