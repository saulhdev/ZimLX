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
import android.util.AttributeSet;

import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;

public class FallbackAppsSearchView extends ExtendedEditText implements AllAppsStore.OnUpdateListener, AllAppsSearchBarController.Callbacks {
    final AllAppsSearchBarController DI;
    AlphabeticalAppsList mApps;
    AllAppsContainerView mAppsView;

    public FallbackAppsSearchView(Context context) {
        this(context, null);
    }

    public FallbackAppsSearchView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FallbackAppsSearchView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        DI = new AllAppsSearchBarController();
    }

    public void onSearchResult(String query, ArrayList<ComponentKey> apps) {
        if (getParent() != null) {
            if (apps != null) {
                mApps.setOrderedFilter(apps);
            }
        }
    }

    @Override
    public final void clearSearchResult() {
        if (getParent() != null) {
            if (mApps.setOrderedFilter(null) || mApps.setSearchSuggestions(null)) {
                dV();
            }
            mAppsView.onClearSearchResult();
        }
    }

    @Override
    public void onAppsUpdated() {
        DI.refreshSearchResult();
    }

    private void dV() {
        mAppsView.onSearchResultsChanged();
    }
}
