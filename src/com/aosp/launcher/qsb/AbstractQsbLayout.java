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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    protected final Paint CV;
    protected final TextPaint qsbHint;
    protected final int qsbTextSpacing;
    protected final boolean mLowPerformanceMode;
    protected final int twoBubbleGap;
    protected final int qsbMaxHintLength;
    public int mShadowMargin;
    protected FallbackAppsSearchView mFallback;
    protected String Dg;
    protected Bitmap Db;
    protected int Dc;
    protected int Dd;
    protected k Ds;

    public AbstractQsbLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractQsbLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivity = (AospLauncher) Launcher.getLauncher(context);
        mShadowMargin = getResources().getDimensionPixelSize(R.dimen.qsb_shadow_margin);
        qsbTextSpacing = getResources().getDimensionPixelSize(R.dimen.qsb_text_spacing);
        twoBubbleGap = getResources().getDimensionPixelSize(R.dimen.qsb_two_bubble_gap);
        qsbMaxHintLength = getResources().getDimensionPixelSize(R.dimen.qsb_max_hint_length);
        qsbHint = new TextPaint();
        qsbHint.setTextSize((float) getResources().getDimensionPixelSize(R.dimen.qsb_hint_text_size));
        mIsRtl = Utilities.isRtl(getResources());
        CV = new Paint(1);
        CV.setColor(Color.WHITE);
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

    protected InsetDrawable createRipple() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(getCornerRadius());
        shape.setColor(ContextCompat.getColor(getContext(), android.R.color.white));

        ColorStateList rippleColor = ContextCompat.getColorStateList(getContext(), R.color.focused_background);
        RippleDrawable ripple = new RippleDrawable(rippleColor, null, shape);
        return new InsetDrawable(ripple, getResources().getDimensionPixelSize(R.dimen.qsb_shadow_margin));
    }

    protected float getCornerRadius() {
        return getCornerRadius(getContext(),
                Utilities.pxFromDp(100, getResources().getDisplayMetrics()));
    }

    @Nullable
    protected String getClipboardText() {
        ClipboardManager clipboardManager = ContextCompat
                .getSystemService(getContext(), ClipboardManager.class);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        if (primaryClip != null) {
            for (int i = 0; i < primaryClip.getItemCount(); i++) {
                CharSequence text = primaryClip.getItemAt(i).coerceToText(getContext());
                if (!TextUtils.isEmpty(text)) {
                    return text.toString();
                }
            }
        }
        return null;
    }
}