package org.zimmob.zimlx.config;

public class FeatureFlags {
    public static final boolean IS_DOGFOOD_BUILD = false;
    // When enabled allows to use any point on the fast scrollbar to start dragging.
    public static final boolean LAUNCHER3_DIRECT_SCROLL = true;
    // When enabled while all-apps open, the soft input will be set to adjust resize .
    public static final boolean LAUNCHER3_UPDATE_SOFT_INPUT_MODE = false;
    // When enabled the promise icon is visible in all apps while installation an app.
    public static final boolean LAUNCHER3_PROMISE_APPS_IN_ALL_APPS = false;
    // When enabled uses the AllAppsRadialGradientAndScrimDrawable for all apps
    public static final boolean LAUNCHER3_GRADIENT_ALL_APPS = true;
    // When enabled allows use of physics based motions in the Launcher.
    public static final boolean LAUNCHER3_PHYSICS = true;
    // When enabled allows use of spring motions on the icons.
    public static final boolean LAUNCHER3_SPRING_ICONS = true;
    // When enabled, app shortcuts are extracted from the package XML.
    public static final boolean LAUNCHER3_BACKPORT_SHORTCUTS = false;
    // Feature flag to enable moving the QSB on the 0th screen of the workspace.
    public static final boolean QSB_ON_FIRST_SCREEN = true;
    // When enabled the all-apps icon is not added to the hotseat.
    public static final boolean NO_ALL_APPS_ICON = true;
    // When enabled fling down gesture on the first workspace triggers search.
    public static final boolean PULLDOWN_SEARCH = false;
    // When enabled the status bar may show dark icons based on the top of the wallpaper.
    public static final boolean LIGHT_STATUS_BAR = false;
    // When enabled, icons not supporting {@link AdaptiveIconDrawable} will be wrapped in {@link FixedScaleDrawable}.
    public static final boolean LEGACY_ICON_TREATMENT = true;
    // When enabled, adaptive icons would have shadows baked when being stored to icon cache.
    public static final boolean ADAPTIVE_ICON_SHADOW = true;
    // When enabled, app discovery will be enabled if service is implemented
    public static final boolean DISCOVERY_ENABLED = false;
    // When enabled, the qsb will be moved to the hotseat.
    public static final boolean QSB_IN_HOTSEAT = true;
    // Features to control Launcher3Go behavior
    public static final boolean GO_DISABLE_WIDGETS = false;
    // Custom flags go below this
    public static boolean LAUNCHER3_DISABLE_ICON_NORMALIZATION = false;
    public static boolean LAUNCHER3_LEGACY_FOLDER_ICON = false;
    public static boolean LAUNCHER3_DISABLE_PINCH_TO_OVERVIEW = false;
    public static boolean LAUNCHER3_NEW_FOLDER_ANIMATION = true;

    private FeatureFlags() {
    }
}
