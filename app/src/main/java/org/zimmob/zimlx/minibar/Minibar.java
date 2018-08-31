package org.zimmob.zimlx.minibar;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.settings.ui.SettingsActivity;
import org.zimmob.zimlx.util.DialogHelper;
import org.zimmob.zimlx.util.Tool;

public class Minibar {
    public static ActionDisplayItem[] actionDisplayItems = new ActionDisplayItem[]{
            new ActionDisplayItem(Action.EditMinibar, "EditMinibar", "", R.drawable.ic_mode_edit_black_24dp, 98),
            new ActionDisplayItem(Action.SetWallpaper, "SetWallpaper", "", R.drawable.ic_photo_black_24dp, 36),
            new ActionDisplayItem(Action.LockScreen, "LockScreen", "", R.drawable.ic_lock_black_24dp, 24),
            new ActionDisplayItem(Action.LauncherSettings, "LauncherSettings", "", R.drawable.ic_settings_launcher_black_24dp, 50),
            new ActionDisplayItem(Action.VolumeDialog, "VolumeDialog", "", R.drawable.ic_volume_up_black_24dp, 71),
            new ActionDisplayItem(Action.DeviceSettings, "DeviceSettings", "", R.drawable.ic_build, 25),
            new ActionDisplayItem(Action.AppDrawer, "AppDrawer", "", R.drawable.ic_apps_dark_24dp, 73),
            new ActionDisplayItem(Action.SearchBar, "SearchBar", "", R.drawable.ic_search_light_24dp, 89),
            new ActionDisplayItem(Action.MobileNetworkSettings, "MobileNetworkSettings", "", R.drawable.ic_network_24dp, 46),

    };

    public static void RunAction(Action action, final Context context) {
        Minibar.RunAction(new ActionItem(action, null), context);

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
                    DialogHelper.alertDialog(context,
                            "Device Admin Required",
                            context.getResources().getString(R.string.device_admin_message),
                            "Enable", (dialog, which) -> {
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
                //HomeActivity.companion.getLauncher().openAppDrawer();
                break;
            case SearchBar: {
                //assert HomeActivity.companion.getLauncher() != null;
                //HomeActivity.companion.getLauncher().getSearchBar().getSearchButton().performClick();
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
                //PackageManager pm = AppManager.getInstance(context).getPackageManager();
                //action.extraData = new Intent(pm.getLaunchIntentForPackage(AppSettings.get().getString(context.getString(R.string.pref_key__gesture_double_tap) + "__", "")));
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

    public enum Action {
        EditMinibar, SetWallpaper, LockScreen, DeviceSettings, LauncherSettings, VolumeDialog, AppDrawer, LaunchApp, SearchBar, MobileNetworkSettings,
    }

    public static class ActionItem {
        public Intent extraData;
        Action action;

        ActionItem(Action action, Intent extraData) {
            this.action = action;
            this.extraData = extraData;
        }
    }

    public static class ActionDisplayItem {
        public String label;
        public String description;
        public int icon;
        public int id;
        Action action;

        ActionDisplayItem(Action action, String label, String description, int icon, int id) {
            this.action = action;
            this.label = label;
            this.description = description;
            this.icon = icon;
            this.id = id;
        }
    }
}