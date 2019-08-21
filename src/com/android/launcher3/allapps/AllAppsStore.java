/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.PromiseAppInfo;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ComponentKeyMapper;
import com.android.launcher3.util.PackageUserKey;

import org.zimmob.zimlx.util.DbHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A utility class to maintain the collection of all apps.
 */
public class AllAppsStore {

    private PackageUserKey mTempKey = new PackageUserKey(null, null);
    private final HashMap<ComponentKey, AppInfo> mComponentToAppMap = new HashMap<>();
    private final List<OnUpdateListener> mUpdateListeners = new ArrayList<>();
    private final ArrayList<ViewGroup> mIconContainers = new ArrayList<>();
    private final Set<FolderIcon> mFolderIcons = Collections.newSetFromMap(new WeakHashMap<>());

    private boolean mDeferUpdates = false;
    private boolean mUpdatePending = false;

    // The set of predicted app component names
    private final List<ComponentKeyMapper<AppInfo>> mPredictedAppComponents = new ArrayList<>();
    private final List<AppInfo> mPredictedApps = new ArrayList<>();
    // The current set of adapter items
    private List<AlphabeticalAppsList.AdapterItem> mAdapterItems = new ArrayList<>();
    private AllAppsGridAdapter mAdapter;
    private int mNumPredictedAppsPerRow;

    // The set of filtered apps with the current filter
    private List<AppInfo> mFilteredApps = new ArrayList<>();



    public Collection<AppInfo> getApps() {
        return mComponentToAppMap.values();
    }

    /**
     * Sets the current set of apps.
     */
    public void setApps(List<AppInfo> apps) {
        mComponentToAppMap.clear();
        addOrUpdateApps(apps);
    }

    public AppInfo getApp(ComponentKey key) {
        return mComponentToAppMap.get(key);
    }

    public void setDeferUpdates(boolean deferUpdates) {
        if (mDeferUpdates != deferUpdates) {
            mDeferUpdates = deferUpdates;

            if (!mDeferUpdates && mUpdatePending) {
                notifyUpdate();
                mUpdatePending = false;
            }
        }
    }

    /**
     * Adds or updates existing apps in the list
     */
    public void addOrUpdateApps(List<AppInfo> apps) {
        for (AppInfo app : apps) {
            mComponentToAppMap.put(app.toComponentKey(), app);
        }
        notifyUpdate();
    }

    private List<AppInfo> processPredictedAppComponents(List<ComponentKeyMapper<AppInfo>> components) {
        if (mComponentToAppMap.isEmpty()) {
            // Apps have not been bound yet.
            return Collections.emptyList();
        }

        List<AppInfo> predictedApps = new ArrayList<>();
        for (ComponentKeyMapper<AppInfo> mapper : components) {
            AppInfo info = mapper.getItem(mComponentToAppMap);
            if (info != null) {
                predictedApps.add(info);
            } else {
                if (FeatureFlags.IS_DOGFOOD_BUILD) {
                    Log.e("AllAppsStore", "Predicted app not found: " + mapper);
                }
            }
            // Stop at the number of predicted apps
            if (predictedApps.size() == mNumPredictedAppsPerRow) {
                break;
            }
        }
        return predictedApps;
    }

    /**
     * Sets the adapter to notify when this dataset changes.
     */
    public void setAdapter(AllAppsGridAdapter adapter) {
        mAdapter = adapter;
    }


    /**
     * Removes some apps from the list.
     */
    public void removeApps(List<AppInfo> apps) {
        DbHelper db = new DbHelper(Launcher.mContext);
        for (AppInfo app : apps) {
            mComponentToAppMap.remove(app.toComponentKey());
            db.deleteApp(app.componentName.getPackageName());
        }
        db.close();
        notifyUpdate();
    }


    private void notifyUpdate() {
        if (mDeferUpdates) {
            mUpdatePending = true;
            return;
        }
        int count = mUpdateListeners.size();
        for (int i = 0; i < count; i++) {
            mUpdateListeners.get(i).onAppsUpdated();
        }
    }

    public void addUpdateListener(OnUpdateListener listener) {
        mUpdateListeners.add(listener);
    }

    public void removeUpdateListener(OnUpdateListener listener) {
        mUpdateListeners.remove(listener);
    }

    public void registerIconContainer(ViewGroup container) {
        if (container != null) {
            mIconContainers.add(container);
        }
    }

    public void unregisterIconContainer(ViewGroup container) {
        mIconContainers.remove(container);
    }

    public void registerFolderIcon(FolderIcon folderIcon) {
        mFolderIcons.add(folderIcon);
    }

    public void updateIconBadges(Set<PackageUserKey> updatedBadges) {
        updateAllIcons((child) -> {
            if (child.getTag() instanceof ItemInfo) {
                ItemInfo info = (ItemInfo) child.getTag();
                if (mTempKey.updateFromItemInfo(info) && updatedBadges.contains(mTempKey)) {
                    child.applyBadgeState(info, true /* animate */);
                }
            }
        });

        Set<FolderIcon> foldersToUpdate = new HashSet<>();
        /*for (FolderIcon folderIcon : mFolderIcons) {
            folderIcon.getFolder().iterateOverItems((info, view) -> {
                if (mTempKey.updateFromItemInfo(info) && updatedBadges.contains(mTempKey)) {
                    if (view instanceof BubbleTextView) {
                        ((BubbleTextView) view).applyBadgeState(info, true);
                    }
                    foldersToUpdate.add(folderIcon);
                }
                return false;
            });
        }*/

        for (FolderIcon folderIcon : foldersToUpdate) {
            folderIcon.updateIconBadges(updatedBadges, mTempKey);
        }
    }

    public void updatePromiseAppProgress(PromiseAppInfo app) {
        updateAllIcons((child) -> {
            if (child.getTag() == app) {
                child.applyProgressLevel(app.level);
            }
        });
    }

    private void updateAllIcons(IconAction action) {
        for (int i = mIconContainers.size() - 1; i >= 0; i--) {
            ViewGroup parent = mIconContainers.get(i);
            int childCount = parent.getChildCount();

            for (int j = 0; j < childCount; j++) {
                View child = parent.getChildAt(j);
                if (child instanceof BubbleTextView) {
                    action.apply((BubbleTextView) child);
                }
            }
        }
    }

    public interface OnUpdateListener {
        void onAppsUpdated();
    }

    public interface IconAction {
        void apply(BubbleTextView icon);
    }
}
