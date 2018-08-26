package org.zimmob.zimlx;

/**
 * Central list of files the Launcher writes to the application data directory.
 */
public class LauncherFiles {

    public static final String LAUNCHER_DB = "launcher.db";
    public static final String SHARED_PREFERENCES_KEY = BuildConfig.APPLICATION_ID + "_preferences";
    public static final String OLD_SHARED_PREFERENCES_KEY = "org.zimmob.zimlx.prefs";
    public static final String MANAGED_USER_PREFERENCES_KEY = "org.zimmob.zimlx.managedusers.prefs";

    public static final String WIDGET_PREVIEWS_DB = "widgetpreviews.db";
    public static final String APP_ICONS_DB = "app_icons.db";
    public static final String APP_SHORTCUTS_DB = "app_shortcuts.db";
}
