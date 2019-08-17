package org.zimmob.zimlx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.uioverrides.OverviewState;
import com.android.launcher3.util.ComponentKey;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.blur.BlurWallpaperProvider;
import org.zimmob.zimlx.gestures.GestureController;
import org.zimmob.zimlx.iconpack.EditIconActivity;
import org.zimmob.zimlx.iconpack.IconPackManager;
import org.zimmob.zimlx.iconpack.IconPackManager.CustomIconEntry;
import org.zimmob.zimlx.override.CustomInfoProvider;
import org.zimmob.zimlx.settings.ui.SettingsActivity;
import org.zimmob.zimlx.views.ZimBackgroundView;

import java.util.Objects;

public class ZimLauncher extends NexusLauncherActivity implements ZimPreferences.OnPreferenceChangeListener {

    public static final int REQUEST_PERMISSION_STORAGE_ACCESS = 666;
    public static final int REQUEST_PERMISSION_LOCATION_ACCESS = 667;
    public static final int CODE_EDIT_ICON = 100;
    public static Context mContext;
    public static Drawable currentEditIcon = null;
    public static ItemInfo currentEditInfo = null;
    public ZimPreferences zimPrefs;
    public ZimBackgroundView background;
    private GestureController gestureController;
    private ZimPreferencesChangeCallback prefCallback = new ZimPreferencesChangeCallback(this);
    private boolean paused = false;
    private boolean sRestart = false;

    public static ZimLauncher getLauncher(Context context) {

        if (context instanceof ZimLauncher) {
            return (ZimLauncher) context;
        } else {
            return (ZimLauncher) LauncherAppState.getInstance(context).getLauncher();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !Utilities.hasStoragePermission(this)) {
            Utilities.requestStoragePermission(this);

        }
        IconPackManager.Companion.getInstance(this).getDefaultPack().getDynamicClockDrawer();

        super.onCreate(savedInstanceState);
        mContext = this;
        zimPrefs = Utilities.getZimPrefs(mContext);
        zimPrefs.registerCallback(prefCallback);
        background = findViewById(R.id.zim_background);
        gestureController = new GestureController(this);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_STORAGE_ACCESS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.title_storage_permission_required)
                        .setMessage(R.string.content_storage_permission_required)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            Utilities.requestStoragePermission(this);
                        })
                        .show();

            }

        }
        if (requestCode == REQUEST_PERMISSION_LOCATION_ACCESS) {
            ZimAppKt.getZimApp(this).getSmartspace().updateWeatherData();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onRotationChanged() {
        BlurWallpaperProvider.Companion.getInstance(this).updateAsync();
    }

    public int getShelfHeight() {
        if (zimPrefs.getShowPredictions()) {
            int qsbHeight = getResources().getDimensionPixelSize(R.dimen.qsb_widget_height);
            return (int) (OverviewState.getDefaultSwipeHeight(mDeviceProfile) + qsbHeight);
        } else {
            return mDeviceProfile.hotseatBarSizePx;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        restartIfPending();
        paused = false;
    }

    public void onPause() {
        super.onPause();

        paused = true;
    }

    public void restartIfPending() {
        if (sRestart) {
            ZimAppKt.getZimApp(mContext).restart(false);
        }
    }

    @Override
    public void finishBindingItems() {
        super.finishBindingItems();
        Utilities.onLauncherStart();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Utilities.onLauncherStart();
    }

    public GestureController getGestureController() {
        return gestureController;
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

    public void startEditIcon(ItemInfo itemInfo, CustomInfoProvider<ItemInfo> infoProvider) {
        ComponentKey component;

        if (itemInfo instanceof AppInfo) {
            component = ((AppInfo) itemInfo).toComponentKey();
            currentEditIcon = IconPackManager.Companion.getInstance(this).getEntryForComponent(component).getDrawable();
        } else if (itemInfo instanceof ShortcutInfo) {
            component = new ComponentKey(itemInfo.getTargetComponent(), itemInfo.user);
            currentEditIcon = new BitmapDrawable(mContext.getResources(), ((ShortcutInfo) itemInfo).iconBitmap);
        } else if (itemInfo instanceof FolderInfo) {
            component = ((FolderInfo) itemInfo).toComponentKey();
            currentEditIcon = ((FolderInfo) itemInfo).getIcon(mContext);
        } else {
            component = null;
            currentEditIcon = null;
        }

        currentEditInfo = itemInfo;
        Boolean folderInfo = itemInfo instanceof FolderInfo;
        Log.e(TAG, "FOLDER INFO " + folderInfo);
        Intent intent = EditIconActivity
                .Companion
                .newIntent(this, infoProvider.getTitle(itemInfo), folderInfo, component);
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
        BlankActivity.Companion
                .startActivityForResult(this, intent, CODE_EDIT_ICON, flags, (resultCode, data) -> {
                    handleEditIconResult(resultCode, data);
                    return null;
                });

    }

    private void handleEditIconResult(int resultCode, @NotNull Bundle data) {
        if (resultCode == Activity.RESULT_OK) {
            if (currentEditInfo == null) {
                return;
            }
            ItemInfo itemInfo = currentEditInfo;
            String entryString = Objects.requireNonNull(data).getString(EditIconActivity.EXTRA_ENTRY);
            CustomIconEntry customIconEntry = CustomIconEntry.Companion.fromNullableString(entryString);

            CustomInfoProvider.Companion.forItem(this, itemInfo).setIcon(itemInfo, customIconEntry);
        }
    }

}
