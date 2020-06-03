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

package com.aosp.launcher.qsb;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.IconShape;
import com.android.launcher3.util.TransformingTouchDelegate;
import com.aosp.launcher.AospLauncher;

import org.zimmob.zimlx.globalsearch.SearchProvider;
import org.zimmob.zimlx.globalsearch.SearchProviderController;
import org.zimmob.zimlx.qsb.k;

import static org.zimmob.zimlx.util.ZimUtilsKt.round;

public abstract class AbstractQsbLayout extends FrameLayout implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener, SearchProviderController.OnProviderChangeListener {
    protected final static String GOOGLE_QSB = "com.google.android.googlequicksearchbox";
    protected final boolean mIsRtl;
    protected final AospLauncher mActivity;
    protected boolean mUseTwoBubbles;
    //public float micStrokeWidth;
    protected final Paint mMicStrokePaint;
    protected final Paint CV;
    protected final int qsbMicWidth;
    protected final TextPaint qsbHint;
    protected final int qsbTextSpacing;
    protected final boolean mLowPerformanceMode;
    protected final int twoBubbleGap;
    protected final int qsbMaxHintLength;
    private final TransformingTouchDelegate touchDelegate;
    public int mShadowMargin;
    protected FallbackAppsSearchView mFallback;
    protected String Dg;
    protected Bitmap Db;
    protected int Dc;
    protected int Dd;
    protected k Ds;
    protected boolean Dh;

    public AbstractQsbLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractQsbLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchDelegate = new TransformingTouchDelegate(this);
        mActivity = (AospLauncher) Launcher.getLauncher(context);
        mShadowMargin = getResources().getDimensionPixelSize(R.dimen.qsb_shadow_margin);
        qsbMicWidth = getResources().getDimensionPixelSize(R.dimen.qsb_mic_width);
        qsbTextSpacing = getResources().getDimensionPixelSize(R.dimen.qsb_text_spacing);
        twoBubbleGap = getResources().getDimensionPixelSize(R.dimen.qsb_two_bubble_gap);
        qsbMaxHintLength = getResources().getDimensionPixelSize(R.dimen.qsb_max_hint_length);
        qsbHint = new TextPaint();
        qsbHint.setTextSize((float) getResources().getDimensionPixelSize(R.dimen.qsb_hint_text_size));
        mIsRtl = Utilities.isRtl(getResources());
        mMicStrokePaint = new Paint(1);
        CV = new Paint(1);

        mLowPerformanceMode = Utilities.getZimPrefs(context).getLowPerformanceMode();
    }

    protected Drawable getIcon() {
        return getIcon(true);
    }

    protected Drawable getIcon(boolean colored) {
        SearchProvider provider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
        return provider.getIcon(colored);
    }

    public static float getCornerRadius(Context context, float defaultRadius) {
        float radius = round(Utilities.getZimPrefs(context).getSearchBarRadius());
        if (radius > 0f) {
            return radius;
        }
        TypedValue edgeRadius = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius);
        if (edgeRadius != null) {
            return edgeRadius.getDimension(context.getResources().getDisplayMetrics());
        } else {
            return defaultRadius;
        }
    }

    protected float getCornerRadius() {
        return getCornerRadius(getContext(),
                Utilities.pxFromDp(100, getResources().getDisplayMetrics()));
    }
}