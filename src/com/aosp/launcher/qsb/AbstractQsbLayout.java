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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.aosp.launcher.AospLauncher;

import org.zimmob.zimlx.globalsearch.SearchProvider;
import org.zimmob.zimlx.globalsearch.SearchProviderController;

public abstract class AbstractQsbLayout extends FrameLayout {
    protected final static String GOOGLE_QSB = "com.google.android.googlequicksearchbox";

    protected final AospLauncher mActivity;
    protected Bitmap mShadowBitmap;
    protected ImageView mMicIconView;
    protected boolean mUseTwoBubbles;
    private boolean mShowAssistant;
    private ImageView mLogoIconView;
    private float mRadius = -1.0f;

    public AbstractQsbLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public AbstractQsbLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractQsbLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivity = (AospLauncher) Launcher.getLauncher(context);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "opa_enabled":
            case "opa_assistant":
            case "pref_bubbleSearchStyle":
                loadPreferences(sharedPreferences);
        }
        if (key.equals("pref_searchbarRadius")) {
            mShadowBitmap = null;
            loadPreferences(sharedPreferences);
        }
    }

    protected void loadPreferences(SharedPreferences sharedPreferences) {
        post(() -> {
            mShowAssistant = sharedPreferences.getBoolean("opa_assistant", true);
            mLogoIconView.setImageDrawable(getIcon());
            mMicIconView.setVisibility(sharedPreferences.getBoolean("opa_enabled", true) ? View.VISIBLE : View.GONE);
            mMicIconView.setImageDrawable(getMicIcon());
            mUseTwoBubbles = useTwoBubbles();
            mRadius = Utilities.getZimPrefs(getContext()).getSearchBarRadius();
            invalidate();
        });
    }

    public boolean useTwoBubbles() {
        return mMicIconView.getVisibility() == View.VISIBLE && Utilities
                .getZimPrefs(mActivity).getDualBubbleSearch();
    }

    protected Drawable getIcon() {
        return getIcon(true);
    }

    protected Drawable getIcon(boolean colored) {
        SearchProvider provider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
        return provider.getIcon(colored);
    }

    protected Drawable getMicIcon() {
        return getMicIcon(true);
    }

    protected Drawable getMicIcon(boolean colored) {
        SearchProvider provider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
        if (mShowAssistant && provider.getSupportsAssistant()) {
            return provider.getAssistantIcon(colored);
        } else if (provider.getSupportsVoiceSearch()) {
            return provider.getVoiceIcon(colored);
        } else {
            mMicIconView.setVisibility(GONE);
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }
}
