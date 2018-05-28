package org.zimmob.zimlx.activity.homeparts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.zimmob.zimlx.AppObject;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.apps.AppManager;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DatabaseHelper;
import org.zimmob.zimlx.viewutil.DesktopGestureListener.DesktopGestureCallback;
import org.zimmob.zimlx.viewutil.ItemGestureListener.ItemGestureCallback;

/* compiled from: HomeActivity.kt */
public final class HpInitSetup extends Setup {
    private final AppManager _appLoader;
    private final DatabaseHelper _dataManager;
    private final HpGestureCallback _desktopGestureCallback;
    private final HpEventHandler _eventHandler;
    private final ItemGestureCallback _itemGestureCallback;
    private final Logger _logger;
    private final AppSettings _appSettings;

    public HpInitSetup(HomeActivity home) {
        _appSettings = AppSettings.get();
        _desktopGestureCallback = new HpGestureCallback(_appSettings);
        _dataManager = new DatabaseHelper(home);
        _appLoader = AppManager.getInstance(home);
        _eventHandler = new HpEventHandler();

        _logger = (source, priority, tag, msg, args) -> Log.println(priority, tag, String.format(msg, args));

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