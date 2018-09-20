/*
 * Copyright (C) 2013 The Android Open Source Project
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

package org.zimmob.zimlx;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;

import org.zimmob.zimlx.compat.LauncherAppsCompat;
import org.zimmob.zimlx.compat.UserManagerCompat;
import org.zimmob.zimlx.dynamicui.ExtractionUtils;
import org.zimmob.zimlx.shortcuts.DeepShortcutManager;
import org.zimmob.zimlx.util.ConfigMonitor;
import org.zimmob.zimlx.util.Thunk;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public class LauncherAppState {

    private static WeakReference<LauncherProvider> sLauncherProvider;
    private static LauncherAppState INSTANCE;
    private static Context mContext;
    @Thunk
    final LauncherModel mModel;
    private final IconCache mIconCache;
    private final WidgetPreviewLoader mWidgetCache;
    @Thunk
    boolean mWallpaperChangedSinceLastCheck;
    private InvariantDeviceProfile mInvariantDeviceProfile;

    private Launcher mLauncher;

    private LauncherAppState() {
        if (mContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        Log.v(Launcher.TAG, "LauncherAppState inited");

        mInvariantDeviceProfile = new InvariantDeviceProfile(mContext);
        mIconCache = new IconCache(mContext, mInvariantDeviceProfile);
        mWidgetCache = new WidgetPreviewLoader(mContext, mIconCache);

        mModel = new LauncherModel(this, mIconCache, new StringSetAppFilter(), DeepShortcutManager.getInstance(getContext()));

        LauncherAppsCompat.getInstance(mContext).addOnAppsChangedCallback(mModel);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // For handling managed profiles
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED);
        // For extracting colors from the wallpaper
        filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);

        mContext.registerReceiver(mModel, filter);
        UserManagerCompat.getInstance(mContext).enableAndResetCache();
        new ConfigMonitor(mContext).register();

        if (Utilities.ATLEAST_NOUGAT) {
            ExtractionUtils.startColorExtractionServiceIfNecessary(mContext);
        } else {
            ExtractionUtils.startColorExtractionService(mContext);
        }
    }

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                INSTANCE = new LauncherAppState();
            } else {
                try {
                    return new MainThreadExecutor().submit(LauncherAppState::getInstance).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    static void setLauncherProvider(LauncherProvider provider) {
        if (sLauncherProvider != null) {
            Log.w(Launcher.TAG, "setLauncherProvider called twice! old=" +
                    sLauncherProvider.get() + " new=" + provider);
        }
        sLauncherProvider = new WeakReference<>(provider);

        // The content provider exists for the entire duration of the launcher main process and
        // is the first component to get created. Initializing application context here ensures
        // that LauncherAppState always exists in the main process.
        mContext = provider.getContext().getApplicationContext();
    }

    public static InvariantDeviceProfile getIDP(Context context) {
        return getInstance().mInvariantDeviceProfile;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Reloads the workspace items from the DB and re-binds the workspace. This should generally
     * not be called as DB updates are automatically followed by UI update
     */
    public void reloadWorkspace() {
        mModel.resetLoadedState(false, true);
        mModel.startLoaderFromBackground();
    }

    public void reloadAllApps() {
        mModel.resetLoadedState(true, false);
        mModel.startLoaderFromBackground();
    }

    public void reloadAll(boolean showWorkspace) {
        mModel.resetLoadedState(true, true);
        mModel.startLoaderFromBackground();
        if (showWorkspace) {
            mLauncher.showWorkspace(true);
        }
    }

    LauncherModel setLauncher(Launcher launcher) {
        sLauncherProvider.get().setLauncherProviderChangeListener(launcher);
        mModel.initialize(launcher);
        return mModel;
    }

    public void setMLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    public WidgetPreviewLoader getWidgetCache() {
        return mWidgetCache;
    }

    public boolean hasWallpaperChangedSinceLastCheck() {
        boolean result = mWallpaperChangedSinceLastCheck;
        mWallpaperChangedSinceLastCheck = false;
        return result;
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }

    public Launcher getLauncher() {
        return mLauncher;
    }

}
