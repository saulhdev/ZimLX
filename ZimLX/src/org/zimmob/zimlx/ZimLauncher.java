package org.zimmob.zimlx;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.settings.ui.SettingsActivity;
import org.zimmob.zimlx.views.ZimBackgroundView;

public class ZimLauncher extends NexusLauncherActivity implements ZimPreferences.OnPreferenceChangeListener {

    public static Context mContext;
    public static final int REQUEST_PERMISSION_STORAGE_ACCESS = 666;
    public static final int REQUEST_PERMISSION_LOCATION_ACCESS = 667;
    public static final int CODE_EDIT_ICON = 100;

    private ZimPreferencesChangeCallback prefCallback = new ZimPreferencesChangeCallback(this);
    private ZimPreferences zimPrefs;
    private boolean paused = false;
    private boolean sRestart = false;
    public ZimBackgroundView background;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !Utilities.hasStoragePermission(this)) {
            Utilities.requestStoragePermission(this);
        }
        super.onCreate(savedInstanceState);
        mContext = this;
        zimPrefs = Utilities.getZimPrefs(mContext);
        zimPrefs.registerCallback(prefCallback);
        background = findViewById(R.id.zim_background);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FeatureFlags.QSB_ON_FIRST_SCREEN != showSmartspace()) {
            if (Utilities.ATLEAST_NOUGAT) {
                recreate();
            } else {
                finish();
                startActivity(getIntent());
            }
        }
    }

    private boolean showSmartspace() {
        return Utilities.getPrefs(this).getBoolean(SettingsActivity.SMARTSPACE_PREF, true);
    }

    @Override
    public void onValueChanged(@NotNull String key, @NotNull ZimPreferences prefs, boolean force) {

    }

    public void scheduleRestart() {
        if (paused) {
            sRestart = true;
        } else {
            Utilities.restartLauncher(this);
        }
    }


    public boolean shouldRecreate() {
        return !sRestart;
    }

    public void refreshGrid() {
        //workspace.refreshChildren();
    }
}
