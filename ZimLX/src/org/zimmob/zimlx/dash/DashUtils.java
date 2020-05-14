/*
 * Copyright (c) 2020 Zim Launcher
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
package org.zimmob.zimlx.dash;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

import androidx.drawerlayout.widget.DrawerLayout;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

import org.zimmob.zimlx.settings.SettingsActivity;

import java.util.Objects;

import static com.android.launcher3.LauncherState.ALL_APPS;

public class DashUtils {
    public static DashItem[] actionDisplayItems = new DashItem[]{
            DashItem.asCustomItem(DashAction.Action.EditMinibar, "Edit Minibar", Launcher.mContext.getString(R.string.minibar_0), R.drawable.ic_mode_edit_black_24dp, 10),
            DashItem.asCustomItem(DashAction.Action.SetWallpaper, "Set Wallpaper", Launcher.mContext.getString(R.string.minibar_1), R.drawable.ic_photo_black_24dp, 11),
            DashItem.asCustomItem(DashAction.Action.LauncherSettings, "Launcher Settings", Launcher.mContext.getString(R.string.minibar_5), R.drawable.ic_settings_launcher_black_24dp, 12),
            DashItem.asCustomItem(DashAction.Action.VolumeDialog, "Volume Dialog", Launcher.mContext.getString(R.string.minibar_7), R.drawable.ic_volume_up_black_24dp, 13),
            DashItem.asCustomItem(DashAction.Action.DeviceSettings, "Device Settings", Launcher.mContext.getString(R.string.minibar_4), R.drawable.ic_build, 14),
            DashItem.asCustomItem(DashAction.Action.MobileNetworkSettings, "Mobile Network Settings", Launcher.mContext.getString(R.string.minibar_10), R.drawable.ic_network_24dp, 15),
            DashItem.asCustomItem(DashAction.Action.AppSettings, "App Settings", Launcher.mContext.getString(R.string.minibar_11), R.drawable.ic_font_download, 16),
            DashItem.asCustomItem(DashAction.Action.AppDrawer, "App Drawer", Launcher.mContext.getString(R.string.minibar_8), R.drawable.ic_apps_24dp, 17),
    };

    public static void RunAction(DashAction.Action action, final Context context) {
        RunAction(new DashAction(action, null), context);
    }

    private static void RunAction(DashAction action, Context context) {
        switch (action.action) {
            case EditMinibar:
                String fragment = "org.zimmob.zimlx.dash.DashFragment";
                SettingsActivity.startFragment(context, fragment, R.string.minibar);
                break;
            case MobileNetworkSettings: {
                context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                break;
            }
            case AppSettings: {
                context.startActivity(new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));

                break;
            }
            case SetWallpaper:
                context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), context.getString(R.string.wallpaper_pick)));
                break;

            case DeviceSettings:
                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                break;
            case LauncherSettings:
                context.startActivity(new Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                        .setPackage(context.getPackageName())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;

            case AppDrawer:
                if (!Launcher.getLauncher(context).isInState(ALL_APPS)) {
                    Launcher.getLauncher(context).getStateManager().goToState(ALL_APPS);
                    ((DrawerLayout) Launcher.getLauncher(context).findViewById(R.id.drawer_layout)).closeDrawers();
                }
                break;
            case VolumeDialog:
                try {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    Objects.requireNonNull(audioManager).setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                } catch (Exception e) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (!Objects.requireNonNull(mNotificationManager).isNotificationPolicyAccessGranted()) {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        context.startActivity(intent);
                    }
                }
                break;
        }
    }

    public static DashItem getDashItemFromString(String string) {
        for (DashItem item : actionDisplayItems) {
            if (Integer.toString(item.id).equals(string)) {
                return item;
            }
        }
        return null;
    }
}
