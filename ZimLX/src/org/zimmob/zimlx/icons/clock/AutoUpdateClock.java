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

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Utilities;

import java.util.TimeZone;

public class AutoUpdateClock extends FastBitmapDrawable implements Runnable {
    private ClockLayers mLayers;

    AutoUpdateClock(ItemInfoWithIcon info, ClockLayers layers) {
        super(info);
        mLayers = layers;
    }

    private void rescheduleUpdate() {
        long millisInSecond = 1000L;
        unscheduleSelf(this);
        long uptimeMillis = SystemClock.uptimeMillis();
        scheduleSelf(this, uptimeMillis - uptimeMillis % millisInSecond + millisInSecond);
    }

    // Used only by Google Clock
    void updateLayers(ClockLayers layers) {
        mLayers = layers;
        if (mLayers != null) {
            mLayers.mDrawable.setBounds(getBounds());
        }
        invalidateSelf();
    }

    void setTimeZone(TimeZone timeZone) {
        if (mLayers != null) {
            mLayers.setTimeZone(timeZone);
            invalidateSelf();
        }
    }

    @Override
    public void drawInternal(Canvas canvas, Rect rect) {
        if (mLayers == null || !Utilities.ATLEAST_OREO) {
            super.drawInternal(canvas, rect);
        } else {
            canvas.drawBitmap(mLayers.bitmap, null, rect, mPaint);
            mLayers.updateAngles();
            canvas.scale(mLayers.scale, mLayers.scale,
                    rect.exactCenterX() + mLayers.offset,
                    rect.exactCenterY() + mLayers.offset);
            canvas.clipPath(mLayers.mDrawable.getIconMask());
            mLayers.mDrawable.getForeground().draw(canvas);
            rescheduleUpdate();
        }
    }

    @Override
    protected void onBoundsChange(final Rect bounds) {
        super.onBoundsChange(bounds);
        if (mLayers != null) {
            mLayers.mDrawable.setBounds(bounds);
        }
    }

    @Override
    public void run() {
        if (mLayers.updateAngles()) {
            invalidateSelf();
        } else {
            rescheduleUpdate();
        }
    }
}
