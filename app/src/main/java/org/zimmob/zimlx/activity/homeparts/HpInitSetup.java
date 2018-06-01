package org.zimmob.zimlx.activity.homeparts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.zimmob.zimlx.AppObject;
import org.zimmob.zimlx.launcher.Launcher;
import org.zimmob.zimlx.icon.SimpleIconProvider;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.apps.AppManager;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DatabaseHelper;
import org.zimmob.zimlx.viewutil.DesktopGestureListener.DesktopGestureCallback;
import org.zimmob.zimlx.viewutil.ItemGestureListener.ItemGestureCallback;

/* compiled from: Launcher.kt */
public final class HpInitSetup extends Setup {
    private final AppManager _appLoader;
    private final DatabaseHelper _dataManager;
    private final HpGestureCallback _desktopGestureCallback;
    private final HpEventHandler _eventHandler;
    private final ItemGestureCallback _itemGestureCallback;
    private final Logger _logger;
    private final AppSettings _appSettings;
    private final ImageLoader _imageLoader;

    public HpInitSetup(Launcher home) {
        _appSettings = AppSettings.get();
        _desktopGestureCallback = new HpGestureCallback(_appSettings);
        _dataManager = new DatabaseHelper(home);
        _appLoader = AppManager.getInstance(home);
        _eventHandler = new HpEventHandler();

        _logger = (source, priority, tag, msg, args) -> Log.println(priority, tag, String.format(msg, args));

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

        _itemGestureCallback = (item, event) -> false;
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
    public DatabaseHelper getDataManager() {
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