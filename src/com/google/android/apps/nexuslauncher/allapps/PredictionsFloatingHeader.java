/*
 * Copyright (C) 2018 CypherOS
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

package com.google.android.apps.nexuslauncher.allapps;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.view.animation.Interpolator;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.FloatingHeaderView;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.util.ComponentKeyMapper;

import org.zimmob.zimlx.ZimPreferences;

import java.util.List;

import static com.google.android.apps.nexuslauncher.allapps.PredictionRowView.DividerType;

@TargetApi(26)
public class PredictionsFloatingHeader extends FloatingHeaderView implements Insettable {
    private static final FloatProperty<PredictionsFloatingHeader> CONTENT_ALPHA = new FloatProperty<PredictionsFloatingHeader>("contentAlpha") {
        public void setValue(PredictionsFloatingHeader predictionsFloatingHeader, float f) {
            predictionsFloatingHeader.setContentAlpha(f);
        }

        public Float get(PredictionsFloatingHeader predictionsFloatingHeader) {
            return predictionsFloatingHeader.mContentAlpha;
        }
    };
    private float mContentAlpha;
    private int mHeaderTopPadding;
    private boolean mIsCollapsed;
    private boolean mIsVerticalLayout;
    private PredictionRowView mPredictionRowView;
    private boolean mShowAllAppsLabel;
    private Context mContext;

    public PredictionsFloatingHeader(Context context) {
        this(context, null);
    }

    public PredictionsFloatingHeader(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContentAlpha = 1.0f;
        mHeaderTopPadding = context.getResources().getDimensionPixelSize(R.dimen.all_apps_header_top_padding);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPredictionRowView = findViewById(R.id.prediction_row);
        updateShowAllAppsLabel();
    }

    @Override
    public void setup(AllAppsContainerView.AdapterHolder[] mAH, boolean tabsHidden) {
        mPredictionRowView.setup(this, Utilities.getZimPrefs(mContext).getShowPredictions());
        mTabsHidden = tabsHidden;
        updateExpectedHeight();
        super.setup(mAH, tabsHidden);
    }

    private void updateExpectedHeight() {
        boolean useAllAppsLabel = mShowAllAppsLabel && mTabsHidden;
        DividerType dividerType = DividerType.NONE;
        if (useAllAppsLabel) {
            dividerType = DividerType.ALL_APPS_LABEL;
        } else if (mTabsHidden) {
            dividerType = DividerType.LINE;
        }
        mPredictionRowView.setDividerType(dividerType, false);
        mMaxTranslation = mPredictionRowView.getExpectedHeight();
    }

    @Override
    public int getMaxTranslation() {
        if (mMaxTranslation == 0 && mTabsHidden) {
            return getResources().getDimensionPixelSize(R.dimen.all_apps_search_bar_bottom_padding);
        }
        if (mMaxTranslation <= 0 || !mTabsHidden) {
            return mMaxTranslation;
        }
        return mMaxTranslation + getPaddingTop();
    }

    public PredictionRowView getPredictionRowView() {
        return mPredictionRowView;
    }

    @Override
    public void setInsets(Rect rect) {
        DeviceProfile deviceProfile = Launcher.getLauncher(getContext()).getDeviceProfile();
        int i = deviceProfile.desiredWorkspaceLeftRightMarginPx + deviceProfile.cellLayoutPaddingLeftRightPx;
        mPredictionRowView.setPadding(i, mPredictionRowView.getPaddingTop(), i, mPredictionRowView.getPaddingBottom());
        mIsVerticalLayout = deviceProfile.isVerticalBarLayout();
    }

    public void headerChanged() {
        int i = mMaxTranslation;
        updateExpectedHeight();
        if (mMaxTranslation != i) {
            Launcher.getLauncher(getContext()).getAppsView().setupHeader();
        }
    }

    @Override
    protected void applyScroll(int uncappedY, int currentY) {
        if (uncappedY < currentY - mHeaderTopPadding) {
            mPredictionRowView.setScrolledOut(true);
            return;
        }
        ZimPreferences prefs = Utilities.getZimPrefs(getContext());
        float translationY = uncappedY;
        if (!prefs.getDockSearchBar() || prefs.getDockHide()) {
            int qsbHeight = getResources().getDimensionPixelSize(R.dimen.qsb_widget_height);
            translationY -= mHeaderTopPadding;
            translationY += qsbHeight / 2;
        }
        mPredictionRowView.setScrolledOut(false);
        mPredictionRowView.setScrollTranslation(translationY);
    }

    @Override
    public void setContentVisibility(boolean hasHeader, boolean hasContent, PropertySetter propertySetter, Interpolator interpolator) {
        if (hasHeader && !hasContent && mIsCollapsed) {
            Launcher.getLauncher(getContext()).getAppsView().getSearchUiManager().resetSearch();
        }
        allowTouchForwarding(hasContent);
        propertySetter.setFloat(this, CONTENT_ALPHA, hasContent ? 1.0f : 0.0f, interpolator);
        mPredictionRowView.setContentVisibility(hasHeader, hasContent, propertySetter, interpolator);
    }

    public void updateShowAllAppsLabel() {
        setShowAllAppsLabel(Utilities.ATLEAST_MARSHMALLOW && Utilities.getZimPrefs(getContext()).getShowAllAppsLabel());
    }

    public void setShowAllAppsLabel(boolean show) {
        if (mShowAllAppsLabel != show) {
            mShowAllAppsLabel = show;
            headerChanged();
        }
    }

    private void setContentAlpha(float alpha) {
        mContentAlpha = alpha;
        mTabLayout.setAlpha(alpha);
    }

    public boolean hasVisibleContent() {
        return Utilities.getZimPrefs(mContext).getShowPredictions();
    }

    public void setCollapsed(boolean collapsed) {
        if (collapsed != mIsCollapsed) {
            mIsCollapsed = collapsed;
            mPredictionRowView.setCollapsed(collapsed);
            headerChanged();
        }
    }

    public void setPredictedApps(boolean z, List<ComponentKeyMapper> list) {
        mPredictionRowView.setPredictedApps(z, list);
    }
}
