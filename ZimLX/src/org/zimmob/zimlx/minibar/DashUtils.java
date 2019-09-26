package org.zimmob.zimlx.minibar;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

import androidx.drawerlayout.widget.DrawerLayout;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

import static com.android.launcher3.LauncherState.ALL_APPS;
import static org.zimmob.zimlx.minibar.DashAction.Action;

public class DashUtils {
    public static Context mContext = Launcher.mContext;
    public static DashItem[] actionDisplayItems = new DashItem[]{
            DashItem.asCustomItem(Action.AppDrawer, "App Drawer", mContext.getString(R.string.minibar_8), R.drawable.ic_apps_dark_24dp, 17),
            DashItem.asCustomItem(Action.EditMinibar, "Edit Minibar", mContext.getString(R.string.minibar_0), R.drawable.ic_mode_edit_black_24dp, 10),
            DashItem.asCustomItem(Action.SetWallpaper, "Set Wallpaper", mContext.getString(R.string.minibar_1), R.drawable.ic_photo_black_24dp, 11),
            DashItem.asCustomItem(Action.LauncherSettings, "Launcher Settings", Launcher.getLauncher(mContext).getString(R.string.minibar_5), R.drawable.ic_settings_launcher_black_24dp, 12),
            DashItem.asCustomItem(Action.VolumeDialog, "Volume Dialog", Launcher.getLauncher(mContext).getString(R.string.minibar_7), R.drawable.ic_volume_up_black_24dp, 13),
            DashItem.asCustomItem(Action.DeviceSettings, "Device Settings", Launcher.getLauncher(mContext).getString(R.string.minibar_4), R.drawable.ic_build, 14),
            DashItem.asCustomItem(Action.MobileNetworkSettings, "Mobile Network Settings", Launcher.getLauncher(mContext).getString(R.string.minibar_10), R.drawable.ic_network_24dp, 15),
            DashItem.asCustomItem(Action.AppSettings, "App Settings", Launcher.getLauncher(mContext).getString(R.string.minibar_11), R.drawable.ic_font_download, 16)
    };

    public static void RunAction(DashAction.Action action, final Context context) {
        RunAction(new DashAction(action, null), context);
    }

    private static void RunAction(DashAction action, Context context) {
        switch (action.action) {
            case EditMinibar:
                context.startActivity(new Intent(context, MinibarEditActivity.class));
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
