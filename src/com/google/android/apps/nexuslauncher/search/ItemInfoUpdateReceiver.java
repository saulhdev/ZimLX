package com.google.android.apps.nexuslauncher.search;

import android.content.SharedPreferences;

import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.icons.IconCache;

import org.zimmob.zimlx.util.ZimFlags;

public class ItemInfoUpdateReceiver implements IconCache.ItemInfoUpdateReceiver, SharedPreferences.OnSharedPreferenceChangeListener {
    private final LauncherCallbacks mCallbacks;
    private final int eD;
    private final Launcher mLauncher;

    public ItemInfoUpdateReceiver(final Launcher launcher, final LauncherCallbacks callbacks) {
        this.mLauncher = launcher;
        this.mCallbacks = callbacks;
        this.eD = launcher.getDeviceProfile().allAppsNumCols;
    }

    public void di() {
        /*final IconCache iconCache = LauncherAppState.getInstance(this.mLauncher).getIconCache();
        final Iterator<ComponentKeyMapper> iterator = this.mCallbacks.getPredictedApps().iterator();
        int n = 0;
        while (iterator.hasNext()) {
            final AppInfo app = mLauncher.mAppsView.getAppsStore().getApp(iterator.next().getKey());
            int n2;
            if (app != null) {
                if (app.usingLowResIcon) {
                    iconCache.updateIconInBackground(this, app);
                }
                n2 = n + 1;
                if (n2 >= this.eD) {
                    break;
                }
            } else {
                n2 = n;
            }
            n = n2;
        }*/
    }

    public void onCreate() {
        mLauncher.getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    public void onDestroy() {
        mLauncher.getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String s) {
        if ("reflection_last_predictions".equals(s) || ZimFlags.APPDRAWER_SHOW_PREDICTIONS.equals(s)) {
            this.di();
        }
    }

    public void reapplyItemInfo(final ItemInfoWithIcon itemInfoWithIcon) {
    }
}
