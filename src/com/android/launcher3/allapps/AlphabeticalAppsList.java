/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.launcher3.allapps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AlphabeticIndexCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ComponentKeyMapper;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.LabelComparator;

import org.zimmob.zimlx.ZimPreferences;
import org.zimmob.zimlx.model.AppCountInfo;
import org.zimmob.zimlx.util.DbHelper;
import org.zimmob.zimlx.util.InstallTimeComparator;
import org.zimmob.zimlx.util.MostUsedComparator;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static org.zimmob.zimlx.util.Config.SORT_AZ;
import static org.zimmob.zimlx.util.Config.SORT_LAST_INSTALLED;
import static org.zimmob.zimlx.util.Config.SORT_MOST_USED;
import static org.zimmob.zimlx.util.Config.SORT_ZA;

/**
 * The alphabetically sorted list of applications.
 */
public class AlphabeticalAppsList implements AllAppsStore.OnUpdateListener {

    public static final String TAG = "AlphabeticalAppsList";

    private static final int FAST_SCROLL_FRACTION_DISTRIBUTE_BY_ROWS_FRACTION = 0;
    private static final int FAST_SCROLL_FRACTION_DISTRIBUTE_BY_NUM_SECTIONS = 1;

    private final int mFastScrollDistributionMode = FAST_SCROLL_FRACTION_DISTRIBUTE_BY_NUM_SECTIONS;

    private final Launcher mLauncher;

    // The set of apps from the system
    private final List<AppInfo> mApps = new ArrayList<>();
    private final AllAppsStore mAllAppsStore;
    private final HashMap<ComponentKey, AppInfo> mComponentToAppMap = new HashMap<>();
    // The set of filtered apps with the current filter
    private final List<AppInfo> mFilteredApps = new ArrayList<>();
    // The current set of adapter items
    private final ArrayList<AdapterItem> mAdapterItems = new ArrayList<>();
    // The set of sections that we allow fast-scrolling to (includes non-merged sections)
    private final List<FastScrollSectionInfo> mFastScrollerSections = new ArrayList<>();
    // The set of predicted app component names
    private final List<ComponentKeyMapper<AppInfo>> mPredictedAppComponents = new ArrayList<>();
    // Is it the work profile app list.
    private final boolean mIsWork;
    // The set of predicted apps resolved from the component names and the current set of apps
    private final List<AppInfo> mPredictedApps = new ArrayList<>();

    // The of ordered component names as a result of a search query
    private ArrayList<ComponentKey> mSearchResults;
    private HashMap<CharSequence, String> mCachedSectionNames = new HashMap<>();
    private AllAppsGridAdapter mAdapter;
    private AlphabeticIndexCompat mIndexer;
    private AppInfoComparator mAppNameComparator;
    private final int mNumAppsPerRow;
    private int mNumAppRowsInAdapter;
    private ItemInfoMatcher mItemFilter;

    public AlphabeticalAppsList(Context context, AllAppsStore appsStore, boolean isWork) {
        mAllAppsStore = appsStore;
        mLauncher = Launcher.getLauncher(context);
        mIndexer = new AlphabeticIndexCompat(context);
        mAppNameComparator = new AppInfoComparator(context);
        mIsWork = isWork;
        mNumAppsPerRow = mLauncher.getDeviceProfile().inv.numColumns;
        mAllAppsStore.addUpdateListener(this);
    }

    public void updateItemFilter(ItemInfoMatcher itemFilter) {
        this.mItemFilter = itemFilter;
        onAppsUpdated();
    }

    /**
     * Sets the adapter to notify when this dataset changes.
     */
    public void setAdapter(AllAppsGridAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Returns all the apps.
     */
    public List<AppInfo> getApps() {
        return mApps;
    }

    /**
     * Returns the predicted apps.
     */
    public List<AppInfo> getPredictedApps() {
        return mPredictedApps;
    }

    /**
     * Sets the current set of predicted apps.
     * <p>
     * This can be called before we get the full set of applications, we should merge the results
     * only in onAppsUpdated() which is idempotent.
     * <p>
     * If the number of predicted apps is the same as the previous list of predicted apps,
     * we can optimize by swapping them in place.
     */
    public void setPredictedApps(List<ComponentKeyMapper<AppInfo>> apps) {
        mPredictedAppComponents.clear();
        mPredictedAppComponents.addAll(apps);

        List<AppInfo> newPredictedApps = processPredictedAppComponents(apps);
        // We only need to do work if any of the visible predicted apps have changed.
        if (!newPredictedApps.equals(mPredictedApps)) {
            if (newPredictedApps.size() == mPredictedApps.size()) {
                swapInNewPredictedApps(newPredictedApps);
            } else {
                // We need to update the appIndex of all the items.
                onAppsUpdated();
            }
        }
    }


    private void sortApps(int sortType) {
        switch (sortType) {
            //SORT BY NAME AZ
            case SORT_AZ:
                Collections.sort(mApps, mAppNameComparator);
                break;

            //SORT BY NAME ZA
            case SORT_ZA:
                Collections.sort(mApps, (p2, p1) -> Collator
                        .getInstance()
                        .compare(p1.title, p2.title));
                break;

            //SORT BY LAST INSTALLED
            case SORT_LAST_INSTALLED:
                PackageManager pm = mLauncher.getApplicationContext().getPackageManager();
                InstallTimeComparator installTimeComparator = new InstallTimeComparator(pm);
                Collections.sort(mApps, installTimeComparator);
                break;

            //SORT BY MOST USED DESC
            case SORT_MOST_USED:
                DbHelper db = new DbHelper(mLauncher.getApplicationContext());
                List<AppCountInfo> appsCounter = db.getAppsCount();
                db.close();
                MostUsedComparator mostUsedComparator = new MostUsedComparator(appsCounter);
                Collections.sort(mApps, mostUsedComparator);
                break;
            default:
                Collections.sort(mApps, mAppNameComparator);
                break;

        }
    }

    /**
     * Returns fast scroller sections of all the current filtered applications.
     */
    public List<FastScrollSectionInfo> getFastScrollerSections() {
        return mFastScrollerSections;
    }

    /**
     * Returns the current filtered list of applications broken down into their sections.
     */
    public List<AdapterItem> getAdapterItems() {
        return mAdapterItems;
    }

    /**
     * Returns the number of rows of applications
     */
    public int getNumAppRows() {
        return mNumAppRowsInAdapter;
    }

    /**
     * Returns the number of applications in this list.
     */
    public int getNumFilteredApps() {
        return mFilteredApps.size();
    }

    /**
     * Returns whether there are is a filter set.
     */
    public boolean hasFilter() {
        return (mSearchResults != null);
    }

    /**
     * Returns whether there are no filtered results.
     */
    public boolean hasNoFilteredResults() {
        return (mSearchResults != null) && mFilteredApps.isEmpty();
    }

    public List<AppInfo> getFilteredApps() {
        return mFilteredApps;
    }

    /**
     * Sets the sorted list of filtered components.
     */
    public boolean setOrderedFilter(ArrayList<ComponentKey> f) {
        if (mSearchResults != f) {
            boolean same = mSearchResults != null && mSearchResults.equals(f);
            mSearchResults = f;
            onAppsUpdated();
            return !same;
        }
        return false;
    }

    private List<AppInfo> processPredictedAppComponents(List<ComponentKeyMapper<AppInfo>> components) {
        if (mComponentToAppMap.isEmpty()) {
            // Apps have not been bound yet.
            return Collections.emptyList();
        }

        int nPredictedApps = Utilities.getZimPrefs(mLauncher.getApplicationContext()).getNumPredictedApps();
        List<AppInfo> predictedApps = new ArrayList<>();
        for (ComponentKeyMapper<AppInfo> mapper : components) {
            AppInfo info = mapper.getItem(mComponentToAppMap);
            if (info != null) {
                predictedApps.add(info);
            } else {
                if (FeatureFlags.IS_DOGFOOD_BUILD) {
                    Log.e(TAG, "Predicted app not found: " + mapper);
                }
            }
            // Stop at the number of predicted apps
            if (predictedApps.size() == nPredictedApps) {
                break;
            }
        }
        return predictedApps;
    }

    /**
     * Updates internals when the set of apps are updated.
     */
    @Override
    public void onAppsUpdated() {
        // Sort the list of apps
        mApps.clear();

        for (AppInfo app : mAllAppsStore.getApps()) {
            if (mItemFilter == null || mItemFilter.matches(app, null) || hasFilter()) {
                mApps.add(app);
            }
        }

        Context context = mLauncher.getApplicationContext();
        ZimPreferences pref = new ZimPreferences(context);
        if (!pref.getShowPredictions()) {
            sortApps(pref.getSortMode());
        } else {
            Collections.sort(mApps, mAppNameComparator);
        }


        // As a special case for some languages (currently only Simplified Chinese), we may need to
        // coalesce sections
        Locale curLocale = mLauncher.getResources().getConfiguration().locale;
        boolean localeRequiresSectionSorting = curLocale.equals(Locale.SIMPLIFIED_CHINESE);
        if (localeRequiresSectionSorting) {
            // Compute the section headers. We use a TreeMap with the section name comparator to
            // ensure that the sections are ordered when we iterate over it later
            TreeMap<String, ArrayList<AppInfo>> sectionMap = new TreeMap<>(new LabelComparator());
            for (AppInfo info : mApps) {
                // Add the section to the cache
                String sectionName = getAndUpdateCachedSectionName(info.title);

                // Add it to the mapping
                ArrayList<AppInfo> sectionApps = sectionMap.get(sectionName);
                if (sectionApps == null) {
                    sectionApps = new ArrayList<>();
                    sectionMap.put(sectionName, sectionApps);
                }
                sectionApps.add(info);
            }

            // Add each of the section apps to the list in order
            mApps.clear();
            for (Map.Entry<String, ArrayList<AppInfo>> entry : sectionMap.entrySet()) {
                mApps.addAll(entry.getValue());
            }
        } else {
            // Just compute the section headers for use below
            for (AppInfo info : mApps) {
                // Add the section to the cache
                getAndUpdateCachedSectionName(info.title);
            }
        }

        // Recompose the set of adapter items from the current set of apps
        updateAdapterItems();
    }

    /**
     * Updates the set of filtered apps with the current filter.  At this point, we expect
     * mCachedSectionNames to have been calculated for the set of all apps in mApps.
     */
    private void updateAdapterItems() {
        refillAdapterItems();
        refreshRecyclerView();
    }

    private void refreshRecyclerView() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void refillAdapterItems() {
        String lastSectionName = null;
        FastScrollSectionInfo lastFastScrollerSectionInfo = null;
        int position = 0;
        int appIndex = 0;

        // Prepare to update the list of sections, filtered apps, etc.
        mFilteredApps.clear();
        mFastScrollerSections.clear();
        mAdapterItems.clear();

        // Process the predicted app components
        mPredictedApps.clear();
        if (mPredictedAppComponents != null && !mPredictedAppComponents.isEmpty() && !hasFilter()) {
            mPredictedApps.addAll(processPredictedAppComponents(mPredictedAppComponents));

            if (!mPredictedApps.isEmpty()) {
                // Add a section for the predictions
                lastFastScrollerSectionInfo = new FastScrollSectionInfo("");
                mFastScrollerSections.add(lastFastScrollerSectionInfo);

                // Add the predicted app items
                for (AppInfo info : mPredictedApps) {
                    AdapterItem appItem = AdapterItem.asPredictedApp(position++, "", info,
                            appIndex++);
                    if (lastFastScrollerSectionInfo.fastScrollToItem == null) {
                        lastFastScrollerSectionInfo.fastScrollToItem = appItem;
                    }
                    mAdapterItems.add(appItem);
                    mFilteredApps.add(info);
                }

                mAdapterItems.add(AdapterItem.asPredictionDivider(position++));
            }
        }

        // Recreate the filtered and sectioned apps (for convenience for the grid layout) from the
        // ordered set of sections
        for (AppInfo info : getFiltersAppInfos()) {
            String sectionName = getAndUpdateCachedSectionName(info.title);

            // Create a new section if the section names do not match
            if (!sectionName.equals(lastSectionName)) {
                lastSectionName = sectionName;
                lastFastScrollerSectionInfo = new FastScrollSectionInfo(sectionName);
                mFastScrollerSections.add(lastFastScrollerSectionInfo);
            }

            // Create an app item
            AdapterItem appItem = AdapterItem.asApp(position++, sectionName, info, appIndex++);
            if (lastFastScrollerSectionInfo.fastScrollToItem == null) {
                lastFastScrollerSectionInfo.fastScrollToItem = appItem;
            }
            mAdapterItems.add(appItem);
            mFilteredApps.add(info);
        }

        if (hasFilter()) {
            // Append the search market item
            if (hasNoFilteredResults()) {
                mAdapterItems.add(AdapterItem.asEmptySearch(position++));
            } else {
                mAdapterItems.add(AdapterItem.asAllAppsDivider(position++));
            }
            mAdapterItems.add(AdapterItem.asMarketSearch(position++));
        }

        if (mNumAppsPerRow != 0) {
            // Update the number of rows in the adapter after we do all the merging (otherwise, we
            // would have to shift the values again)
            int numAppsInSection = 0;
            int numAppsInRow = 0;
            int rowIndex = -1;
            for (AdapterItem item : mAdapterItems) {
                item.rowIndex = 0;
                if (AllAppsGridAdapter.isDividerViewType(item.viewType)) {
                    numAppsInSection = 0;
                } else if (AllAppsGridAdapter.isIconViewType(item.viewType)) {
                    if (numAppsInSection % mNumAppsPerRow == 0) {
                        numAppsInRow = 0;
                        rowIndex++;
                    }
                    item.rowIndex = rowIndex;
                    item.rowAppIndex = numAppsInRow;
                    numAppsInSection++;
                    numAppsInRow++;
                }
            }
            mNumAppRowsInAdapter = rowIndex + 1;

            // Pre-calculate all the fast scroller fractions
            switch (mFastScrollDistributionMode) {
                case FAST_SCROLL_FRACTION_DISTRIBUTE_BY_ROWS_FRACTION:
                    float rowFraction = 1f / mNumAppRowsInAdapter;
                    for (FastScrollSectionInfo info : mFastScrollerSections) {
                        AdapterItem item = info.fastScrollToItem;
                        if (!AllAppsGridAdapter.isIconViewType(item.viewType)) {
                            info.touchFraction = 0f;
                            continue;
                        }

                        float subRowFraction = item.rowAppIndex * (rowFraction / mNumAppsPerRow);
                        info.touchFraction = item.rowIndex * rowFraction + subRowFraction;
                    }
                    break;
                case FAST_SCROLL_FRACTION_DISTRIBUTE_BY_NUM_SECTIONS:
                    float perSectionTouchFraction = 1f / mFastScrollerSections.size();
                    float cumulativeTouchFraction = 0f;
                    for (FastScrollSectionInfo info : mFastScrollerSections) {
                        AdapterItem item = info.fastScrollToItem;
                        if (!AllAppsGridAdapter.isIconViewType(item.viewType)) {
                            info.touchFraction = 0f;
                            continue;
                        }
                        info.touchFraction = cumulativeTouchFraction;
                        cumulativeTouchFraction += perSectionTouchFraction;
                    }
                    break;
            }
        }

        // Add the work profile footer if required.
        if (shouldShowWorkFooter()) {
            mAdapterItems.add(AdapterItem.asWorkTabFooter(position++));
        }
    }

    private boolean shouldShowWorkFooter() {
        return mIsWork && Utilities.ATLEAST_P &&
                (DeepShortcutManager.getInstance(mLauncher).hasHostPermission()
                        || mLauncher.checkSelfPermission("android.permission.MODIFY_QUIET_MODE")
                        == PackageManager.PERMISSION_GRANTED);
    }

    private List<AppInfo> getFiltersAppInfos() {
        if (mSearchResults == null) {
            return mApps;
        }
        ArrayList<AppInfo> result = new ArrayList<>();
        for (ComponentKey key : mSearchResults) {
            AppInfo match = mAllAppsStore.getApp(key);
            if (match != null) {
                result.add(match);
            }
        }
        return result;
    }

    public AppInfo findApp(ComponentKeyMapper<AppInfo> mapper) {
        return mapper.getItem(mComponentToAppMap);
    }

    public AppInfo AllAppsList(ComponentKeyMapper<AppInfo> mapper) {
        return mapper.getItem(mComponentToAppMap);
    }

    /**
     * Returns the cached section name for the given title, recomputing and updating the cache if
     * the title has no cached section name.
     */
    private String getAndUpdateCachedSectionName(CharSequence title) {
        String sectionName = mCachedSectionNames.get(title);
        if (sectionName == null) {
            sectionName = mIndexer.computeSectionName(title);
            mCachedSectionNames.put(title, sectionName);
        }
        return sectionName;
    }

    /**
     * Swaps out the old predicted apps with the new predicted apps, in place. This optimization
     * allows us to skip an entire relayout that would otherwise be called by notifyDataSetChanged.
     * <p>
     * Note: This should only be called if the # of predicted apps is the same.
     * This method assumes that predicted apps are the first items in the adapter.
     */
    private void swapInNewPredictedApps(List<AppInfo> apps) {
        mPredictedApps.clear();
        mPredictedApps.addAll(apps);

        int size = apps.size();
        for (int i = 0; i < size; ++i) {
            AppInfo info = apps.get(i);
            AdapterItem appItem = AdapterItem.asPredictedApp(i, "", info, i);
            appItem.rowAppIndex = i;
            mAdapterItems.set(i, appItem);
            mFilteredApps.set(i, info);
            mAdapter.notifyItemChanged(i);
        }
    }

    /**
     * Info about a fast scroller section, depending if sections are merged, the fast scroller
     * sections will not be the same set as the section headers.
     */
    public static class FastScrollSectionInfo {
        // The section name
        public String sectionName;
        // The AdapterItem to scroll to for this section
        public AdapterItem fastScrollToItem;
        // The touch fraction that should map to this fast scroll section info
        public float touchFraction;

        public FastScrollSectionInfo(String sectionName) {
            this.sectionName = sectionName;
        }
    }

    /**
     * Info about a particular adapter item (can be either section or app)
     */
    public static class AdapterItem {
        /**
         * Common properties
         */
        // The index of this adapter item in the list
        public int position;
        // The type of this item
        public int viewType;

        /**
         * App-only properties
         */
        // The section name of this app.  Note that there can be multiple items with different
        // sectionNames in the same section
        public String sectionName = null;
        // The row that this item shows up on
        public int rowIndex;
        // The index of this app in the row
        public int rowAppIndex;
        // The associated AppInfo for the app
        public AppInfo appInfo = null;
        // The index of this app not including sections
        public int appIndex = -1;

        public static AdapterItem asPredictedApp(int pos, String sectionName, AppInfo appInfo,
                                                 int appIndex) {
            AdapterItem item = asApp(pos, sectionName, appInfo, appIndex);
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON;
            return item;
        }

        public static AdapterItem asApp(int pos, String sectionName, AppInfo appInfo,
                                        int appIndex) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_ICON;
            item.position = pos;
            item.sectionName = sectionName;
            item.appInfo = appInfo;
            item.appIndex = appIndex;
            return item;
        }

        public static AdapterItem asPredictionDivider(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_PREDICTION_DIVIDER;
            item.position = pos;
            return item;
        }


        public static AdapterItem asEmptySearch(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_EMPTY_SEARCH;
            item.position = pos;
            return item;
        }

        public static AdapterItem asAllAppsDivider(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_ALL_APPS_DIVIDER;
            item.position = pos;
            return item;
        }

        public static AdapterItem asMarketSearch(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET;
            item.position = pos;
            return item;
        }

        public static AdapterItem asWorkTabFooter(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_WORK_TAB_FOOTER;
            item.position = pos;
            return item;
        }
    }

    /**
     * Common interface for different merging strategies.
     */
    public interface MergeAlgorithm {
        boolean continueMerging(SectionInfo section);
    }

    /**
     * Info about a section in the alphabetic list
     */
    public static class SectionInfo {
        // The number of applications in this section
        public int numApps;
        // The section break AdapterItem for this section
        public AdapterItem sectionBreakItem;
        // The first app AdapterItem for this section
        public AdapterItem firstAppItem;
    }
}
