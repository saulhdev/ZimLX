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

package org.zimmob.zimlx.iconpack;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.icons.FixedScaleDrawable;
import com.android.launcher3.icons.IconNormalizer;
import com.android.launcher3.icons.LauncherIcons;

public class CustomAdaptiveIcon extends Drawable {
    private final Context mContext;
    private final AdaptiveIconDrawable mWrapper;

    public CustomAdaptiveIcon(Context context) {
        mContext = context;
        mWrapper = Utilities.ATLEAST_OREO
                ? (AdaptiveIconDrawable) context.getDrawable(
                R.drawable.adaptive_icon_drawable_wrapper).mutate()
                : null;
    }

    @TargetApi(26)
    public Drawable wrap(Drawable icon) {
        return wrap(icon, Color.WHITE);
    }

    @TargetApi(26)
    public Drawable wrap(Drawable icon, int backgroundColor) {
        if (Utilities.ATLEAST_OREO && !(icon instanceof AdaptiveIconDrawable)) {
            boolean[] outShape = new boolean[1];
            mWrapper.setBounds(0, 0, 1, 1);

            LauncherIcons icons = LauncherIcons.obtain(mContext);
            IconNormalizer normalizer = icons.getNormalizer();
            float scale = normalizer.getScale(icon, null, mWrapper.getIconMask(), outShape);
            icons.recycle();

            if (!outShape[0]) {
                FixedScaleDrawable fsd = ((FixedScaleDrawable) mWrapper.getForeground());
                fsd.setDrawable(icon);
                fsd.setScale(scale);
                ((ColorDrawable) mWrapper.getBackground()).setColor(backgroundColor);
                return mWrapper;
            }
        }
        return icon;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
