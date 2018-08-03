package org.zimmob.zimlx.config;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import org.zimmob.zimlx.backup.RestoreBackupActivity;
import org.zimmob.zimlx.model.LauncherModel;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.LooperExecutor;

/**
 * Created by saul on 05-06-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class Config {
    //App Drawer Mode
    public static final int DRAWER_HORIZONTAL = 0;
    public static final int DRAWER_VERTICAL = 1;

    //Folder Shape
    public static final int FOLDER_SHAPE_CIRCLE = 0;
    public static final int FOLDER_SHAPE_CIRCLE_SHADOW = 1;
    public static final int FOLDER_SHAPE_SQUARE = 2;
    public static final int FOLDER_SHAPE_SQUARE_SHADOW = 3;

    //SORT MODE
    public static final int APP_SORT_AZ = 0;
    public static final int APP_SORT_ZA = 1;
    public static final int APP_SORT_LI = 2;//last installer
    public static final int APP_SORT_MU = 3;//most used

    //INDICATOR MODE
    public static final int INDICATOR_DOTS = 0;
    public static final int INDICATOR_ARROW = 1;
    public static final int INDICATOR_LINE = 2;

    public static final int ACTION_LAUNCHER = 8;
    public static final int NO_SCALE = -1;

    public static final boolean DEBUG_MODE = true;

    // separates a list of integers
    public static final String INT_SEP = "#";

    // don't change the order, index is saved into db!
    public enum ItemPosition {
        Dock,
        Desktop
    }

    // don't change the order, index is saved into db!
    public enum ItemState {
        Hidden,
        Visible
    }

    public enum PeekDirection {
        UP, LEFT, RIGHT, DOWN
    }

    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    // doesn't work reliably yet
    public static final boolean ENABLE_ITEM_TOUCH_LISTENER = false;


    public static int boundToRange(int value, int lowerBound, int upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }

    private static final String LAUNCHER_RESTART_KEY = "launcher_restart_key";
    private static final int WAIT_BEFORE_RESTART = 250;

    public static final boolean ATLEAST_MARSHMALLOW =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    public static final boolean ATLEAST_LOLLIPOP_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static final boolean ATLEAST_NOUGAT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

    public static final boolean ATLEAST_NOUGAT_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;

    public static final boolean ATLEAST_OREO =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    public static boolean isRtl(Resources res) {
        return res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    @NonNull
    public static SharedPreferences getPrefs(Context context) {
        return AppSettings.get().getDefaultPreferences();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void restartLauncher(final Context context) {
        new LooperExecutor(LauncherModel.getWorkerLooper()).execute(() -> {
            try {
                Thread.sleep(WAIT_BEFORE_RESTART);
            } catch (Exception e) {
                Log.e("SettingsActivity", "Error waiting", e);
            }

            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setPackage(context.getPackageName())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Create a pending intent so the application is restarted after Process.killProcess() was called.
            // We use an AlarmManager to call this intent in 50ms
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 50, pendingIntent);

            // Kill the application
            android.os.Process.killProcess(android.os.Process.myPid());
        });
    }

    public static void checkRestoreSuccess(Context context) {
        context.startActivity(new Intent(context, RestoreBackupActivity.class)
                .putExtra(RestoreBackupActivity.EXTRA_SUCCESS, true));

    }

    public static void killLauncher() {
        System.exit(0);
    }
}
