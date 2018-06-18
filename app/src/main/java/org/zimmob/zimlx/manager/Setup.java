package org.zimmob.zimlx.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.zimmob.zimlx.apps.AppManager;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.icon.BaseIconProvider;
import org.zimmob.zimlx.icon.SimpleIconProvider;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DatabaseHelper;
import org.zimmob.zimlx.util.IDialogListener;
import org.zimmob.zimlx.viewutil.DesktopGestureListener;
import org.zimmob.zimlx.viewutil.ItemGestureListener;

import java.util.List;

public abstract class Setup {

    // ----------------
    // Class and singleton
    // ----------------

    private static Setup _setup = null;

    public static boolean wasInitialised() {
        return _setup != null;
    }

    public static void init(Setup setup) {
        Setup._setup = setup;
    }

    public static Setup get() {
        if (_setup == null) {
            throw new RuntimeException("Setup has not been initialised!");
        }
        return _setup;
    }

    // ----------------
    // Methods for convenience and shorter code
    // ----------------

    public static Context appContext() {
        return get().getAppContext();
    }

    public static AppSettings appSettings() {
        return get().getAppSettings();
    }

    public static DesktopGestureListener.DesktopGestureCallback desktopGestureCallback() {
        return get().getDesktopGestureCallback();
    }

    public static ItemGestureListener.ItemGestureCallback itemGestureCallback() {
        return get().getItemGestureCallback();
    }

    public static ImageLoader imageLoader() {
        return get().getImageLoader();
    }

    public static DatabaseHelper dataManager() {
        return get().getDataManager();
    }

    public static AppManager appLoader() {
        return get().getAppLoader();
    }

    public static EventHandler eventHandler() {
        return get().getEventHandler();
    }

    public static Logger logger() {
        return get().getLogger();
    }

    // ----------------
    // Settings
    // ----------------

    protected abstract Context getAppContext();

    public abstract AppSettings getAppSettings();

    protected abstract DesktopGestureListener.DesktopGestureCallback getDesktopGestureCallback();

    protected abstract ItemGestureListener.ItemGestureCallback getItemGestureCallback();

    protected abstract DatabaseHelper getDataManager();

    public abstract AppManager getAppLoader();

    protected abstract EventHandler getEventHandler();

    protected abstract Logger getLogger();

    public abstract ImageLoader getImageLoader();

    public interface ImageLoader {
        SimpleIconProvider createIconProvider(Drawable drawable);
        SimpleIconProvider createIconProvider(int icon);
    }

    public interface DataManager {
        void saveItem(Item item);

        void saveItem(Item item, Config.ItemState state);

        void saveItem(Item item, int page, Config.ItemPosition desktop);

        void deleteItem(Item item, boolean deleteSubItems);

        List<List<Item>> getDesktop();

        List<Item> getDock();

        Item getItem(int id);

        void saveApp(String packageName);
        void updateAppCount(String packageName);
        int getAppCount(String packageaName);
    }

    public interface EventHandler {
        void showLauncherSettings(Context context);

        void showPickAction(Context context, IDialogListener.OnAddAppDrawerItemListener listener);

        void showEditDialog(Context context, Item item, IDialogListener.OnEditDialogListener listener);

        void showDeletePackageDialog(Context context, Item item);
    }

    public interface Logger {
        void log(Object source, int priority, String tag, String msg, Object... args);
    }
}
