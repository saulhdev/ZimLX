/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use file except in compliance with the License.
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
package com.aosp.launcher.qsb;

import static com.android.launcher3.LauncherState.ALL_APPS;

import android.content.Context;
import android.util.AttributeSet;

import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Launcher;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore.OnUpdateListener;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.AllAppsSearchBarController.Callbacks;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;

public class DefaultQsbContainer extends ExtendedEditText implements OnUpdateListener, Callbacks {

    public AllAppsSearchBarController mController;
    public AllAppsQsbContainer mAllAppsQsb;
    public AlphabeticalAppsList mApps;
    public AllAppsContainerView mAppsView;

    public DefaultQsbContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DefaultQsbContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mController = new AllAppsSearchBarController();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Launcher.getLauncher(getContext()).getAppsView().getAppsStore().addUpdateListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Launcher.getLauncher(getContext()).getAppsView().getAppsStore().removeUpdateListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Launcher.getLauncher(getContext()).getStateManager().goToState(ALL_APPS);
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onSearchResult(String query, ArrayList<ComponentKey> apps) {
        if (apps != null && getParent() != null) {
            mApps.setOrderedFilter(apps);
            refreshSearchResult();
            mAppsView.setLastSearchQuery(query);
        }
    }

    @Override
    public void clearSearchResult() {
        if (getParent() != null) {
            if (mApps.setOrderedFilter(null)) {
                refreshSearchResult();
            }
            mAllAppsQsb.setKeepDefaultView(true);
            mAppsView.onClearSearchResult();
            mAllAppsQsb.setKeepDefaultView(false);
        }
    }

    @Override
    public void onAppsUpdated() {
        mController.refreshSearchResult();
    }

    public void refreshSearchResult() {
        mAllAppsQsb.updateAlpha(0);
        mAppsView.onSearchResultsChanged();
    }
}
