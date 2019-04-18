package org.zimmob.zimlx;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.Utilities;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;

import org.jetbrains.annotations.NotNull;

public class ZimLauncher extends NexusLauncherActivity implements ZimPreferences.OnPreferenceChangeListener {

    public static Context mContext;
    public static final int REQUEST_PERMISSION_STORAGE_ACCESS = 666;
    public static final int REQUEST_PERMISSION_LOCATION_ACCESS = 667;
    public static final int CODE_EDIT_ICON = 100;

    private Boolean paused = false;
    private Boolean sRestart = false;
    private ZimPreferencesChangeCallback prefCallback = new ZimPreferencesChangeCallback(this);
    private ZimPreferences zimPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !Utilities.hasStoragePermission(this)) {
            Utilities.requestStoragePermission(this);
        }
        super.onCreate(savedInstanceState);
        mContext = this;
        zimPrefs = Utilities.getZimPrefs(mContext);
        zimPrefs.registerCallback(prefCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Utilities.getZimPrefs(this).unregisterCallback();

        if (sRestart) {
            sRestart = false;
            LauncherAppState.destroyInstance();
            ZimPreferences.Companion.destroyInstance();
        }
    }

    public boolean shouldRecreate() {
        return !sRestart;
    }

    public void refreshGrid() {
        mWorkspace.refreshChildren();
    }

    public void scheduleRestart() {
        if (paused) {
            sRestart = true;
        } else {
            Utilities.restartLauncher(this);
        }
    }

    @Override
    public void onValueChanged(@NotNull String key, @NotNull ZimPreferences prefs, boolean force) {

    }
}
