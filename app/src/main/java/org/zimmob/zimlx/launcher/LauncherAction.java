package org.zimmob.zimlx.launcher;


import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.provider.Settings;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.MinibarEditActivity;
import org.zimmob.zimlx.activity.SettingsActivity;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.apps.AppManager;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DialogHelper;
import org.zimmob.zimlx.util.Tool;

public class LauncherAction {

    public enum Action {
        EditMinibar, SetWallpaper, LockScreen, DeviceSettings, LauncherSettings, VolumeDialog, AppDrawer, LaunchApp, SearchBar, MobileNetworkSettings,
    }

    public static ActionDisplayItem[] actionDisplayItems = new ActionDisplayItem[]{
            new ActionDisplayItem(Action.EditMinibar, "EditMinibar", HomeActivity.Companion.get_resources() != null ? HomeActivity.Companion.get_resources().getString(R.string.minibar_0) : null, R.drawable.ic_mode_edit_black_24dp, 98),
            new ActionDisplayItem(Action.SetWallpaper, "SetWallpaper", HomeActivity.Companion.get_resources().getString(R.string.minibar_1), R.drawable.ic_photo_black_24dp, 36),
            new ActionDisplayItem(Action.LockScreen, "LockScreen", HomeActivity.Companion.get_resources().getString(R.string.minibar_2), R.drawable.ic_lock_black_24dp, 24),
            new ActionDisplayItem(Action.LauncherSettings, "LauncherSettings", HomeActivity.Companion.get_resources().getString(R.string.minibar_5), R.drawable.ic_settings_launcher_black_24dp, 50),
            new ActionDisplayItem(Action.VolumeDialog, "VolumeDialog", HomeActivity.Companion.get_resources().getString(R.string.minibar_7), R.drawable.ic_volume_up_black_24dp, 71),
            new ActionDisplayItem(Action.DeviceSettings, "DeviceSettings", HomeActivity.Companion.get_resources().getString(R.string.minibar_4), R.drawable.ic_android_minimal, 25),
            new ActionDisplayItem(Action.AppDrawer, "AppDrawer", HomeActivity.Companion.get_resources().getString(R.string.minibar_8), R.drawable.ic_apps_dark_24dp, 73),
            new ActionDisplayItem(Action.SearchBar, "SearchBar", HomeActivity.Companion.get_resources().getString(R.string.minibar_9), R.drawable.ic_search_light_24dp, 89),
            new ActionDisplayItem(Action.MobileNetworkSettings, "MobileNetworkSettings", HomeActivity.Companion.get_resources().getString(R.string.minibar_10), R.drawable.ic_network_24dp, 46),
    };

    public static void RunAction(Action action, final Context context) {
        LauncherAction.RunAction(new ActionItem(action, null), context);
    }

    public static void RunAction(ActionItem action, final Context context) {
        switch (action.action) {
            case EditMinibar:
                context.startActivity(new Intent(context, MinibarEditActivity.class));
                break;
            case MobileNetworkSettings: {
                context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                break;
            }
            case SetWallpaper:
                context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), context.getString(R.string.wallpaper_pick)));
                break;
            case LockScreen:
                try {
                    ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
                } catch (Exception e) {
                    DialogHelper.alertDialog(context, "Device Admin Required", "OpenLauncher requires the Device Administration permission to lock your screen. Please enable it in the settings to use this feature.", "Enable", (dialog, which) -> {
                        Tool.toast(context, context.getString(R.string.toast_device_admin_required));
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                        context.startActivity(intent);
                    });
                }
                break;
            case DeviceSettings:
                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                break;
            case LauncherSettings:
                context.startActivity(new Intent(context, SettingsActivity.class));
                break;
            case AppDrawer:
                HomeActivity.Companion.getLauncher().openAppDrawer();
                break;
            case SearchBar: {
                assert HomeActivity.Companion.getLauncher() != null;
                HomeActivity.Companion.getLauncher().getSearchBar().getSearchButton().performClick();
                break;
            }
            case VolumeDialog:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                    } catch (Exception e) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            context.startActivity(intent);
                        }
                    }
                } else {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                }
                break;
            case LaunchApp:
                PackageManager pm = AppManager.getInstance(context).getPackageManager();
                action.extraData = new Intent(pm.getLaunchIntentForPackage(AppSettings.get().getString(context.getString(R.string.pref_key__gesture_double_tap) + "__", "")));
                break;
        }
    }

    public static ActionDisplayItem getActionItemFromString(String string) {
        for (ActionDisplayItem item : actionDisplayItems) {
            if (Integer.toString(item.id).equals(string)) {
                return item;
            }
        }
        return null;
    }

    public static ActionItem getActionItem(int position) {
        return new ActionItem(Action.values()[position], null);
    }

    public static class ActionItem {
        Action action;
        public Intent extraData;

        ActionItem(Action action, Intent extraData) {
            this.action = action;
            this.extraData = extraData;
        }
    }

    public static class ActionDisplayItem {
        Action action;
        public String label;
        public String description;
        public int icon;
        public int id;

        ActionDisplayItem(Action action, String label, String description, int icon, int id) {
            this.action = action;
            this.label = label;
            this.description = description;
            this.icon = icon;
            this.id = id;
        }
    }
}