package org.zimmob.zimlx;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.gestures.GestureController;
import org.zimmob.zimlx.override.CustomInfoProvider;
import org.zimmob.zimlx.settings.ui.SettingsActivity;
import org.zimmob.zimlx.views.ZimBackgroundView;

public class ZimLauncher extends NexusLauncherActivity implements ZimPreferences.OnPreferenceChangeListener {

    public static Context mContext;
    public static final int REQUEST_PERMISSION_STORAGE_ACCESS = 666;
    public static final int REQUEST_PERMISSION_LOCATION_ACCESS = 667;
    public static final int CODE_EDIT_ICON = 100;
    public static Drawable currentEditIcon = null;

    private GestureController gestureController;

    private ZimPreferencesChangeCallback prefCallback = new ZimPreferencesChangeCallback(this);
    public ZimPreferences zimPrefs;
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
        gestureController = new GestureController(this);
    }

    public GestureController getGestureController() {
        return gestureController;
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

    public static ZimLauncher getLauncher(Context context) {
        if (context instanceof Launcher) {
            return (ZimLauncher) context;
        }
        return ((ZimLauncher) ((ContextWrapper) context).getBaseContext());
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
        mWorkspace.refreshChildren();
    }

    public void startEditIcon(ItemInfo itemInfo, CustomInfoProvider<ItemInfo> infoProvider) {



        /*val component: ComponentKey? = when (itemInfo) {
            is AppInfo -> itemInfo.toComponentKey()
            is ShortcutInfo -> itemInfo.targetComponent?.let { ComponentKey(it, itemInfo.user) }
            is FolderInfo -> itemInfo.toComponentKey()
            else -> null
        }
        currentEditIcon = when (itemInfo) {
            is AppInfo -> IconPackManager.getInstance(this).getEntryForComponent(component!!)?.drawable
            is ShortcutInfo -> BitmapDrawable(resources, itemInfo.iconBitmap)
            is FolderInfo -> itemInfo.getIcon(this)
            else -> null
        }
        currentEditInfo = itemInfo
        Intent intent = EditIconActivity.newIntent(this, infoProvider.getTitle(itemInfo), FolderInfo itemInfo is FolderInfo, component);
        val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        BlankActivity.startActivityForResult(this, intent, CODE_EDIT_ICON,
                flags) { resultCode, data -> handleEditIconResult(resultCode, data) }*/
    }
}
