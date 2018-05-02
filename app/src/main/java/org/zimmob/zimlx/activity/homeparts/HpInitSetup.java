package org.zimmob.zimlx.activity.homeparts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.zimmob.zimlx.AppObject;
import org.zimmob.zimlx.activity.Home;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppManager;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DatabaseHelper;
import org.zimmob.zimlx.util.SimpleIconProvider;
import org.zimmob.zimlx.viewutil.DesktopGestureListener.DesktopGestureCallback;
import org.zimmob.zimlx.viewutil.ItemGestureListener;
import org.zimmob.zimlx.viewutil.ItemGestureListener.ItemGestureCallback;

/* compiled from: Home.kt */
public final class HpInitSetup extends Setup {
    private final AppManager _appLoader;
    private final DatabaseHelper _dataManager;
    private final HpDesktopGestureCallback _desktopGestureCallback;
    private final HpEventHandler _eventHandler;
    private final ImageLoader _imageLoader;
    private final ItemGestureCallback _itemGestureCallback;
    private final Logger _logger;
    private final AppSettings _appSettings;

    public HpInitSetup(Home home) {
        _appSettings = AppSettings.get();
        _desktopGestureCallback = new HpDesktopGestureCallback(_appSettings);
        _dataManager = new DatabaseHelper(home);
        _appLoader = AppManager.getInstance(home);
        _eventHandler = new HpEventHandler();

        _logger = new Logger() {
            @Override
            public void log(Object source, int priority, String tag, String msg, Object... args) {
                Log.println(priority, tag, String.format(msg, args));
            }
        };

        _imageLoader = new ImageLoader() {
            @NonNull
            public SimpleIconProvider createIconProvider(@Nullable Drawable drawable) {
                return new SimpleIconProvider(drawable);
            }

            @NonNull
            public SimpleIconProvider createIconProvider(int icon) {
                return new SimpleIconProvider(icon);
            }
        };
        _itemGestureCallback = new ItemGestureCallback() {
            @Override
            public boolean onItemGesture(Item item, ItemGestureListener.Type event) {
                return false;
            }
        };
    }

    @NonNull
    public Context getAppContext() {
        return AppObject.get();
    }

    @NonNull
    public AppSettings getAppSettings() {
        return _appSettings;
    }

    @NonNull
    public DesktopGestureCallback getDesktopGestureCallback() {
        return _desktopGestureCallback;
    }

    @NonNull
    public ItemGestureCallback getItemGestureCallback() {
        return _itemGestureCallback;
    }

    @NonNull
    public ImageLoader getImageLoader() {
        return _imageLoader;
    }

    @NonNull
    public DataManager getDataManager() {
        return _dataManager;
    }

    @NonNull
    public AppManager getAppLoader() {
        return _appLoader;
    }

    @NonNull
    public EventHandler getEventHandler() {
        return _eventHandler;
    }

    @NonNull
    public Logger getLogger() {
        return _logger;
    }
}