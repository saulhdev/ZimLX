/*
 * 2020 Zim Launcher
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppTransitionManager;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.LauncherState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.WorkspaceItemInfo;
import com.android.launcher3.util.ComponentKey;
import com.aosp.launcher.AospLauncher;
import com.aosp.launcher.AospLauncherCallbacks;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.blur.BlurWallpaperProvider;
import org.zimmob.zimlx.gestures.GestureController;
import org.zimmob.zimlx.iconpack.EditIconActivity;
import org.zimmob.zimlx.iconpack.IconPackManager;
import org.zimmob.zimlx.override.CustomInfoProvider;
import org.zimmob.zimlx.sensors.BrightnessManager;
import org.zimmob.zimlx.views.OptionsPanel;

import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class ZimLauncher extends AospLauncher {
    public static final int REQUEST_PERMISSION_STORAGE_ACCESS = 666;
    public static final int REQUEST_PERMISSION_LOCATION_ACCESS = 667;
    public static final int CODE_EDIT_ICON = 100;
    public static Drawable currentEditIcon = null;
    public static ItemInfo currentEditInfo = null;
    private ZimPreferences mZimPrefs;
    private boolean paused = false;
    private boolean sRestart = false;
    private GestureController gestureController;
    private ZimPreferencesChangeCallback prefCallback = new ZimPreferencesChangeCallback(this);
    private OptionsPanel optionView;
    private View dummyView;

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
        super.onCreate(savedInstanceState);

        IconPackManager.Companion.getInstance(this).getDefaultPack().getDynamicClockDrawer();
        gestureController = new GestureController(this);
        mContext = this;
        mZimPrefs = Utilities.getZimPrefs(mContext);
        mZimPrefs.registerCallback(prefCallback);
        dummyView = findViewById(R.id.dummy_view);
    }

    @Override
    public boolean startActivitySafely(View v, Intent intent, ItemInfo item) {
        boolean success = super.startActivitySafely(v, intent, item);
        return success;

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        restartIfPending();
        BrightnessManager.Companion.getInstance(this).startListening();
        paused = false;
    }

    public void onPause() {
        super.onPause();
        BrightnessManager.Companion.getInstance(this).stopListening();
        paused = true;
    }

    public void restartIfPending() {
        if (sRestart) {
            ZimAppKt.getZimApp(mContext).restart(false);
        }
    }

    public void finishBindingItems(int currentScreen) {
        super.finishBindingItems(currentScreen);
        Utilities.onLauncherStart();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Utilities.onLauncherStart();
    }

    public void refreshGrid() {
        mWorkspace.refreshChildren();
    }

    public void onDestroy() {
        super.onDestroy();
        Utilities.getZimPrefs(this).unregisterCallback();

        if (sRestart) {
            sRestart = false;
            ZimPreferences.Companion.destroyInstance();
        }
    }

    public OptionsPanel getOptionsView() {
        return optionView = findViewById(R.id.options_view);
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
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> Utilities.requestStoragePermission(this))
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

    public void prepareDummyView(int left, int top, @NotNull Function0<Unit> callback) {
        int size = getResources().getDimensionPixelSize(R.dimen.options_menu_thumb_size);
        int halfSize = size / 2;
        prepareDummyView(left - halfSize, top - halfSize, left + halfSize, top + halfSize, callback);
    }

    public void prepareDummyView(int left, int top, int right, int bottom, @NotNull Function0<Unit> callback) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dummyView.getLayoutParams();
        lp.leftMargin = left;
        lp.topMargin = top;
        lp.height = bottom - top;
        lp.width = right - left;
        dummyView.setLayoutParams(lp);
        dummyView.requestLayout();
        dummyView.post(callback::invoke);
    }

    public GestureController getGestureController() {
        return gestureController;
    }

    public boolean shouldRecreate() {
        return !sRestart;
    }

    public void scheduleRestart() {
        if (paused) {
            sRestart = true;
        } else {
            Utilities.restartLauncher(mContext);
        }
    }

    public void startEditIcon(ItemInfo itemInfo, CustomInfoProvider<ItemInfo> infoProvider) {
        ComponentKey component;

        if (itemInfo instanceof AppInfo) {
            component = ((AppInfo) itemInfo).toComponentKey();
            currentEditIcon = Objects.requireNonNull(IconPackManager.Companion.getInstance(this).getEntryForComponent(component)).getDrawable();
        } else if (itemInfo instanceof WorkspaceItemInfo) {
            component = new ComponentKey(itemInfo.getTargetComponent(), itemInfo.user);
            currentEditIcon = new BitmapDrawable(mContext.getResources(), ((WorkspaceItemInfo) itemInfo).iconBitmap);
        } else if (itemInfo instanceof FolderInfo) {
            component = ((FolderInfo) itemInfo).toComponentKey();
            currentEditIcon = ((FolderInfo) itemInfo).getDefaultIcon(this);
        } else {
            component = null;
            currentEditIcon = null;
        }

        currentEditInfo = itemInfo;
        boolean folderInfo = itemInfo instanceof FolderInfo;
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
            IconPackManager.CustomIconEntry customIconEntry = IconPackManager.CustomIconEntry.Companion.fromNullableString(entryString);

            Objects.requireNonNull(CustomInfoProvider.Companion.forItem(this, itemInfo)).setIcon(itemInfo, customIconEntry);
        }
    }

    @Override
    public int getCurrentState() {
        return 0;
    }
}
