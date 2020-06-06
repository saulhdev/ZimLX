package com.google.android.apps.nexuslauncher;

import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.google.android.libraries.gsa.launcherclient.ISerializableScrollCallback;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

public class NexusLauncherOverlay implements Launcher.LauncherOverlay, ISerializableScrollCallback {
    private static String PREF_PERSIST_FLAGS = "pref_persistent_flags";
    final Launcher mLauncher;
    boolean mFlagsChanged = false;
    boolean mAttached = false;
    private LauncherClient mClient;
    private Launcher.LauncherOverlayCallbacks mOverlayCallbacks;
    private int mFlags;

    public NexusLauncherOverlay(Launcher launcher) {
        mLauncher = launcher;
        mFlags = Utilities.getDevicePrefs(launcher).getInt(PREF_PERSIST_FLAGS, 0);
    }

    public void setClient(LauncherClient client) {
        mClient = client;
    }

    @Override
    public void setPersistentFlags(int flags) {
        flags &= (8 | 16);
        if (flags != mFlags) {
            mFlagsChanged = true;
            mFlags = flags;
            Utilities.getDevicePrefs(mLauncher).edit().putInt(PREF_PERSIST_FLAGS, flags).apply();
        }
    }

    @Override
    public void onServiceStateChanged(boolean overlayAttached) {
        if (overlayAttached != mAttached) {
            mAttached = overlayAttached;
            mLauncher.setLauncherOverlay(overlayAttached ? this : null);
        }
    }

    @Override
    public void onOverlayScrollChanged(float n) {
        if (mOverlayCallbacks != null) {
            mOverlayCallbacks.onScrollChanged(n);
        }
    }

    @Override
    public void onScrollChange(float progress, boolean rtl) {
        mClient.setScroll(progress);
    }

    @Override
    public void onScrollInteractionBegin() {
        mClient.startScroll();
    }

    @Override
    public void onScrollInteractionEnd() {
        mClient.endScroll();
    }

    @Override
    public void setOverlayCallbacks(Launcher.LauncherOverlayCallbacks cb) {
        mOverlayCallbacks = cb;
    }
}