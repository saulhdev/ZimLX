/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zimmob.zimlx.config;

import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.preferences.IPreferenceProvider;
import org.zimmob.zimlx.preferences.PreferenceProvider;

/**
 * Defines a set of flags used to control various launcher behaviors
 */
public final class FeatureFlags extends BaseFlags {

    // When enabled, app shortcuts are extracted from the package XML.
    public static final String KEY_PREF_LIGHT_STATUS_BAR = "pref_forceLightStatusBar";
    public static final String KEY_PREF_PINCH_TO_OVERVIEW = "pref_pinchToOverview";
    public static final String KEY_PREF_PULLDOWN_NOTIS = "pref_pulldownNotis";
    public static final String KEY_PREF_HOTSEAT_EXTRACTED_COLORS = "pref_hotseatShouldUseExtractedColors";
    public static final String KEY_PREF_KEEP_SCROLL_STATE = "pref_keepScrollState";
    public static final String KEY_FULL_WIDTH_SEARCHBAR = "pref_fullWidthSearchbar";
    public static final String KEY_SHOW_PIXEL_BAR = "pref_showPixelBar";
    public static final String KEY_HOME_OPENS_DRAWER = "pref_homeOpensDrawer";
    public static final String KEY_SHOW_SEARCH_PILL = "pref_showSearchPill";
    public static final String KEY_SHOW_DATE_OR_WEATHER = "pref_showDateOrWeather";
    public static final String KEY_SHOW_VOICE_SEARCH_BUTTON = "pref_showMic";
    public static final String KEY_PREF_PIXEL_STYLE_ICONS = "pref_pixelStyleIcons";
    public static final String KEY_PREF_HIDE_APP_LABELS = "pref_hideAppLabels";
    public static final String KEY_PREF_ENABLE_SCREEN_ROTATION = "pref_enableScreenRotation";
    public static final String KEY_PREF_FULL_WIDTH_WIDGETS = "pref_fullWidthWidgets";
    public static final String KEY_PREF_SHOW_NOW_TAB = "pref_showGoogleNowTab";
    public static final String KEY_PREF_TRANSPARENT_HOTSEAT = "pref_isHotseatTransparent";
    public static final String KEY_PREF_ENABLE_DYNAMIC_UI = "pref_enableDynamicUi";
    public static final String KEY_PREF_ENABLE_BLUR = "pref_enableBlur";
    public static final String KEY_PREF_WHITE_GOOGLE_ICON = "pref_enableWhiteGoogleIcon";
    public static final String KEY_PREF_DARK_THEME = "pref_enableDarkTheme";
    public static final String KEY_PREF_ROUND_SEARCH_BAR = "pref_useRoundSearchBar";
    public static final String KEY_PREF_ENABLE_BACKPORT_SHORTCUTS = "pref_enableBackportShortcuts";
    public static final String KEY_PREF_SHOW_TOP_SHADOW = "pref_showTopShadow";
    public static final String KEY_PREF_THEME = "pref_theme";
    public static final String KEY_PREF_PRIMARY_COLOR = "pref_primary_color";
    public static final String KEY_PREF_MINIBAR_COLOR = "pref_minibar_color";
    public static final String KEY_PREF_NOTIFICATION_BACKGROUND = "pref_notification_background";
    public static final String KEY_PREF_NOTIFICATION_COUNT = "pref_notification_count";
    public static final String KEY_PREF_PREDICTIVE_APPS = "pref_predictive_apps";
    public static final String KEY_PREF_NUM_PREDICTIVE_APPS = "pref_predictive_apps_values";

    public static final String KEY_PREF_THEME_MODE = "pref_themeMode";
    public static final String KEY_PREF_HIDE_HOTSEAT = "pref_hideHotseat";
    public static final String KEY_PREF_PLANE = "pref_plane";
    public static final String KEY_PREF_WEATHER = "pref_weather";
    public static final String KEY_PREF_PULLDOWN_ACTION = "pref_pulldownAction";
    public static final String KEY_PREF_LOCK_DESKTOP = "pref_lockDesktop";
    public static final String KEY_PREF_ANIMATED_CLOCK_ICON = "pref_animatedClockIcon";
    public static final String KEY_PREF_USE_SYSTEM_FONTS = "pref_useSystemFonts";
    public static final String KEY_PREF_AUTO_ADD_SHORTCUTS = "pref_autoAddShortcuts";
    public static final int PULLDOWN_NOTIFICATIONS = 1;
    public static final int PULLDOWN_SEARCH = 2;
    public static final int PULLDOWN_APPS_SEARCH = 3;
    public static final String KEY_PREF_DT2S_HANDLER = "pref_dt2sHandler";

    // Feature flag to enable moving the QSB on the 0th screen of the workspace.
    public static int DARK_QSB = 1;
    public static int DARK_FOLDER = 2;
    public static int DARK_ALLAPPS = 4;
    public static int DARK_SHORTCUTS = 8;
    public static int DARK_BLUR = 16;
    public static boolean useDarkTheme = false;
    // When enabled the all-apps icon is not added to the hotseat.
    public static boolean NO_ALL_APPS_ICON = false;
    private static int darkThemeFlag = 0;
    private static int myCurrentTheme = 0;
    public static int[] LAUNCHER_THEMES = {R.style.LauncherTheme, R.style.LauncherTheme_Dark, R.style.LauncherTheme_Black};
    public static int[] SETTINGS_THEMES = {R.style.SettingsTheme, R.style.SettingsTheme_Dark, R.style.SettingsTheme_Black};
    public static int[] SETTINGS_HOME_THEMES = {R.style.SettingsHome, R.style.SettingsHome_Dark, R.style.SettingsHome_Black};

    private FeatureFlags() {
    }

    public static int pullDownAction(Context context) {
        Utilities.getPrefs(context).migratePullDownPref(context);
        return Integer.parseInt(PreferenceProvider.INSTANCE.getPreferences(context).getPulldownAction());
    }

    public static int getCurrentTheme() {
        return myCurrentTheme;
    }

    public static void loadThemePreference(Context context) {
        IPreferenceProvider prefs = PreferenceProvider.INSTANCE.getPreferences(context);
        myCurrentTheme = Integer.parseInt(prefs.getTheme());
        useDarkTheme = getCurrentTheme() != 0;
        darkThemeFlag = prefs.getThemeMode();
    }

    public static boolean getUseDarkTheme() {
        return useDarkTheme && darkThemeFlag != 0;
    }

    public static boolean getUseDarkTheme(int flag) {
        return useDarkTheme && darkThemeFlag != 0 && flag != 0;
    }

    public static Context applyDarkTheme(Context context, int flag) {
        if (getUseDarkTheme(flag)) {
            return new ContextThemeWrapper(context, LAUNCHER_THEMES[myCurrentTheme]);
        } else {
            return context;
        }
    }

    public static void applyDarkTheme(Activity activity) {
        Utilities.getPrefs(activity).migrateThemePref(activity);
        loadThemePreference(activity);
        if (FeatureFlags.useDarkTheme)
            activity.setTheme(SETTINGS_HOME_THEMES[myCurrentTheme]);
    }

    public static LayoutInflater getLayoutInflator(LayoutInflater layoutInflater) {
        Context context = layoutInflater.getContext();
        Utilities.getPrefs(context).migrateThemePref(context);
        loadThemePreference(context);
        return LayoutInflater.from(new ContextThemeWrapper(context, SETTINGS_THEMES[myCurrentTheme]));
    }
}
