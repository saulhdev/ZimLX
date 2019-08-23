package org.zimmob.zimlx.minibar;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

import androidx.drawerlayout.widget.DrawerLayout;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.userevent.nano.LauncherLogProto;

import org.zimmob.zimlx.settings.ui.SettingsActivity;

import static com.android.launcher3.LauncherState.ALL_APPS;
import static org.zimmob.zimlx.minibar.DashAction.Action;

public class DashUtils {
    public static Context mContext = Launcher.mContext;
    public static DashModel[] actionDisplayItems = new DashModel[]{
            new DashModel(Action.EditMinibar, "EditMinibar", mContext.getString(R.string.minibar_0), R.drawable.ic_mode_edit_black_24dp, 10),
            new DashModel(Action.SetWallpaper, "SetWallpaper", mContext.getString(R.string.minibar_1), R.drawable.ic_photo_black_24dp, 11),
            new DashModel(Action.LauncherSettings, "LauncherSettings", Launcher.getLauncher(mContext).getString(R.string.minibar_5), R.drawable.ic_settings_launcher_black_24dp, 12),
            new DashModel(Action.VolumeDialog, "VolumeDialog", Launcher.getLauncher(mContext).getString(R.string.minibar_7), R.drawable.ic_volume_up_black_24dp, 13),
            new DashModel(Action.DeviceSettings, "DeviceSettings", Launcher.getLauncher(mContext).getString(R.string.minibar_4), R.drawable.ic_build, 14),
            new DashModel(Action.MobileNetworkSettings, "MobileNetworkSettings", Launcher.getLauncher(mContext).getString(R.string.minibar_10), R.drawable.ic_network_24dp, 15),
            new DashModel(Action.AppSettings, "AppSettings", Launcher.getLauncher(mContext).getString(R.string.minibar_11), R.drawable.ic_font_download, 16),
            new DashModel(Action.AppDrawer, "AppDrawer", mContext.getString(R.string.minibar_8), R.drawable.ic_apps_dark_24dp, 17)
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
                context.startActivity(new Intent(context, SettingsActivity.class));
                break;

            case AppDrawer:
                if (!Launcher.getLauncher(mContext).isInState(ALL_APPS)) {
                    Launcher.getLauncher(mContext).getUserEventDispatcher()
                            .logActionOnControl(LauncherLogProto.Action.Touch.TAP,
                                    LauncherLogProto.ControlType.ALL_APPS_BUTTON);
                    Launcher.getLauncher(mContext).getStateManager().goToState(ALL_APPS);
                    ((DrawerLayout) Launcher.getLauncher(mContext).findViewById(R.id.drawer_layout)).closeDrawers();
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

    public static DashModel getDashItemFromString(String string) {
        for (DashModel item : actionDisplayItems) {
            if (Integer.toString(item.id).equals(string)) {
                return item;
            }
        }
        return null;
    }
}
