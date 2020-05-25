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

package org.zimmob.zimlx.allapps;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.FloatingHeaderRow;
import com.android.launcher3.allapps.FloatingHeaderView;
import com.android.launcher3.appprediction.AppsDividerView;
import com.android.launcher3.appprediction.ComponentKeyMapper;
import com.android.launcher3.appprediction.PredictionRowView;
import com.android.launcher3.appprediction.PredictionUiStateManager;

import java.util.ArrayList;
import java.util.List;

import static com.android.launcher3.appprediction.AppsDividerView.*;

public class PredictionsFloatingHeader extends FloatingHeaderView implements Insettable {
    private static final FloatProperty<PredictionsFloatingHeader> CONTENT_ALPHA = new FloatProperty<PredictionsFloatingHeader>("contentAlpha") {
        public void setValue(PredictionsFloatingHeader predictionsFloatingHeader, float f) {
            predictionsFloatingHeader.setContentAlpha(f);
        }

        public Float get(PredictionsFloatingHeader predictionsFloatingHeader) {
            return predictionsFloatingHeader.mContentAlpha;
        }
    };
    private final int mHeaderTopPadding;
    private final PredictionUiStateManager mPredictionUiStateManager;
    public FloatingHeaderRow[] mAllRows = FloatingHeaderRow.NO_ROWS;
    private float mContentAlpha;
    private PredictionRowView mPredictionRowView;
    private boolean mShowAllAppsLabel;

    public PredictionsFloatingHeader(@NonNull Context context) {
        this(context, null);
    }

    public PredictionsFloatingHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContentAlpha = 1.0f;
        mHeaderTopPadding = context.getResources().getDimensionPixelSize(R.dimen.all_apps_header_top_padding);
        mPredictionUiStateManager = PredictionUiStateManager.INSTANCE.get(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPredictionRowView = findViewById(R.id.prediction_row);

        // Find all floating header rows.
        ArrayList<FloatingHeaderRow> rows = new ArrayList<>();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof FloatingHeaderRow) {
                rows.add((FloatingHeaderRow) child);
            }
        }

        mFixedRows = rows.toArray(new FloatingHeaderRow[rows.size()]);
        mAllRows = mFixedRows;
        updateShowAllAppsLabel();
    }

    @Override
    public void setup(AllAppsContainerView.AdapterHolder[] adapterHolderArr, boolean tabsHidden) {
        for (FloatingHeaderRow row : mAllRows) {
            row.setup(this, mAllRows, tabsHidden);
        }
        updateExpectedHeight();


        mPredictionRowView.setup(this, mAllRows, false);//mPredictionUiStateManager.getCurrentState().isEnabled);
        mTabsHidden = tabsHidden;
        updateExpectedHeight();
        super.setup(adapterHolderArr, tabsHidden);
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

    public void headerChanged() {
        int i = mMaxTranslation;
        updateExpectedHeight();
        if (mMaxTranslation != i) {
            Launcher.getLauncher(getContext()).getAppsView().setupHeader();
        }
    }

    private void updateExpectedHeight() {
        boolean useAllAppsLabel = mShowAllAppsLabel && mTabsHidden;
        /*DividerType dividerType = DividerType.NONE;
        if (useAllAppsLabel) {
            dividerType = DividerType.ALL_APPS_LABEL;
        } else if (mTabsHidden) {
            dividerType = DividerType.LINE;
        }
        mPredictionRowView.setDividerType(dividerType, false);*/
        mMaxTranslation = mPredictionRowView.getExpectedHeight();
    }

    public void setPredictedApps(List<ComponentKeyMapper> list) {
        mPredictionRowView.setPredictedApps(list);
    }
}
