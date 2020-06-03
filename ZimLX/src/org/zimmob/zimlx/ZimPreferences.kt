/*
 * 2020 Zim Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.zimmob.zimlx

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import com.android.launcher3.*
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.util.ComponentKey
import com.android.quickstep.OverviewInteractionState
import org.json.JSONArray
import org.json.JSONObject
import org.zimmob.zimlx.allapps.PredictionsFloatingHeader
import org.zimmob.zimlx.gestures.BlankGestureHandler
import org.zimmob.zimlx.gestures.handlers.*
import org.zimmob.zimlx.globalsearch.SearchProviderController
import org.zimmob.zimlx.groups.AppGroupsManager
import org.zimmob.zimlx.groups.DrawerTabs
import org.zimmob.zimlx.iconpack.IconPackManager
import org.zimmob.zimlx.preferences.DockStyle
import org.zimmob.zimlx.settings.GridSize
import org.zimmob.zimlx.settings.GridSize2D
import org.zimmob.zimlx.settings.SettingsActivity
import org.zimmob.zimlx.smartspace.*
import org.zimmob.zimlx.theme.ThemeManager
import org.zimmob.zimlx.util.*
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

class ZimPreferences(val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "ZimPreferences"
    private val onChangeMap: MutableMap<String, () -> Unit> = HashMap()
    private val onChangeListeners: MutableMap<String, MutableSet<OnPreferenceChangeListener>> = HashMap()
    private var onChangeCallback: ZimPreferencesChangeCallback? = null
    val sharedPrefs = migratePrefs()

    private fun migratePrefs(): SharedPreferences {
        val dir = context.cacheDir.parent
        val oldFile = File(dir, "shared_prefs/" + LauncherFiles.OLD_SHARED_PREFERENCES_KEY + ".xml")
        val newFile = File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml")
        if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile)
            oldFile.delete()
        }
        return context.applicationContext
                .getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .apply {
                    migrateConfig(this)
                }
    }

    val doNothing = { }
    val recreate = { recreate() }
    private val reloadApps = { reloadApps() }
    private val reloadAll = { reloadAll() }
    val restart = { restart() }
    private val refreshGrid = { refreshGrid() }
    private val updateBlur = { updateBlur() }
    private val reloadIcons = { reloadIcons() }
    private val reloadIconPacks = { IconPackManager.getInstance(context).packList.reloadPacks() }
    private val resetAllApps = { onChangeCallback?.resetAllApps() ?: Unit }
    private val updateWeatherData = { onChangeCallback?.updateWeatherData() ?: Unit }
    private val reloadDockStyle = {
        //LauncherAppState.getIDP(context).onDockStyleChanged(this)
        recreate()
    }
    private val zimConfig = Config.getInstance(context)

    var restoreSuccess by BooleanPref("pref_restoreSuccess", false)
    var configVersion by IntPref("config_version", if (restoreSuccess) 0 else CURRENT_VERSION)

    /* PREFERENCES */
    /* --DESKTOP-- */
    val usePopupMenuView by BooleanPref("pref_desktopUsePopupMenuView", true, doNothing)
    val lockDesktop by BooleanPref("pref_lockDesktop", false, reloadAll)
    val allowFullWidthWidgets by BooleanPref("pref_fullWidthWidgets", false, restart)
    private var gridSizeDelegate = ResettableLazy { GridSize2D(this, "numRows", "numColumns", LauncherAppState.getIDP(context), refreshGrid) }
    val gridSize by gridSizeDelegate
    val autoAddInstalled by BooleanPref("pref_add_icon_to_home", true, doNothing)
    val dashEnable by BooleanPref("pref_key__minibar_enable", true, recreate)
    fun setDashEnable(enable: Boolean) {
        sharedPrefs.edit().putBoolean("pref_key__minibar_enable", enable).apply()
    }
    var dashItems by StringSetPref("pref_key__minibar_items", zimConfig.minibarItems, recreate)

    val allowOverlap by BooleanPref(SettingsActivity.ALLOW_OVERLAP_PREF, false, reloadAll)
    private val homeMultilineLabel by BooleanPref("pref_homeIconLabelsInTwoLines", false, recreate)
    val homeLabelRows get() = if (homeMultilineLabel) 2 else 1
    val desktopIconScale by FloatPref("pref_iconScaleSB", 1f, recreate)
    val hideAppLabels by BooleanPref("pref_hideAppLabels", false, recreate)
    val desktopTextScale by FloatPref("pref_iconTextScaleSB", 1f, reloadAll)

    /* --APP DRAWER-- */
    fun getSortMode(): Int {
        val sort: String = sharedPrefs.getString("pref_key__sort_mode", "0")!!
        recreate
        return sort.toInt()
    }
    val showPredictions by BooleanPref("pref_show_predictions", false, recreate)
    private val drawerMultilineLabel by BooleanPref("pref_iconLabelsInTwoLines", false, recreate)
    val drawerLabelRows get() = if (drawerMultilineLabel) 2 else 1
    val hideAllAppsAppLabels by BooleanPref("pref_hideAllAppsAppLabels", false, recreate)
    val allAppsIconScale by FloatPref("pref_allAppsIconScale", 1f, recreate)
    private val drawerGridSizeDelegate = ResettableLazy { GridSize(this, "numColsDrawer", LauncherAppState.getIDP(context), recreate) }
    val drawerGridSize by drawerGridSizeDelegate
    private val predictionGridSizeDelegate = ResettableLazy { GridSize(this, "numPredictions", LauncherAppState.getIDP(context), recreate) }
    val predictionGridSize by predictionGridSizeDelegate
    val drawerTextScale by FloatPref("pref_allAppsIconTextScale", 1f, recreate)
    val drawerPaddingScale by FloatPref("pref_allAppsPaddingScale", 1.0f, recreate)
    val drawerLabelColor by IntPref("pref_key__drawer_label_color", R.color.qsb_drawer_text_color_normal, reloadApps)
    val showAllAppsLabel by BooleanPref("pref_showAllAppsLabel", false) {
        val header = onChangeCallback?.launcher?.appsView?.floatingHeaderView
        header?.updateShowAllAppsLabel()
    }

    val currentTabsModel
        get() = appGroupsManager.getEnabledModel() as? DrawerTabs ?: appGroupsManager.drawerTabs
    val drawerTabs get() = appGroupsManager.drawerTabs
    val appGroupsManager by lazy { AppGroupsManager(this) }
    val separateWorkApps by BooleanPref("pref_separateWorkApps", true, recreate)
    val saveScrollPosition by BooleanPref("pref_keepScrollState", false, doNothing)
    val searchHiddenApps by BooleanPref(DefaultAppSearchAlgorithm.SEARCH_HIDDEN_APPS, false)
    var hiddenAppSet by StringSetPref("hidden-app-set", Collections.emptySet(), reloadApps)
    var hiddenPredictionAppSet by StringSetPref("pref_hidden_prediction_set", Collections.emptySet(), doNothing)
    val allAppsBackground by IntPref("pref_allAppsBackground", R.color.ic_allapps_background, reloadApps)
    val allAppsOpacity by AlphaPref("pref_allAppsOpacitySB", -1, recreate)

    val allAppsSearch by BooleanPref("pref_allAppsSearch", true, recreate)

    /* --DOCK-- */
    private val dockMultilineLabel by BooleanPref("pref_dockIconLabelsInTwoLines", false, recreate)
    private val dockGridSizeDelegate = ResettableLazy { GridSize(this, "numHotseatIcons", LauncherAppState.getIDP(context), restart) }
    var dockHide by BooleanPref("pref_key__hide_hotseat", false, recreate)
    val dockGradient by BooleanPref("pref_dockGradient", false, restart)
    val dockColor by IntPref("pref_key__dock_background", R.color.transparentish, restart)
    val showTopShadow by BooleanPref("pref_showTopShadow", true, recreate)
    var dockRadius by FloatPref("pref_dockRadius", 16f, recreate)
    val dockGridSize by dockGridSizeDelegate
    var dockSearchBarPref by BooleanPref("pref_dockSearchBar", Utilities.ATLEAST_MARSHMALLOW, recreate)
    inline val dockSearchBar get() = !dockHide && dockSearchBarPref
    val twoRowDock by BooleanPref("pref_twoRowDock", false, restart)
    val dockRowsCount get() = if (twoRowDock) 2 else 1
    var dockScale by FloatPref("pref_dockScale", -1f, recreate)
    val hideDockLabels by BooleanPref("pref_hideDockLabels", true, restart)
    val dockLabelRows get() = if (dockMultilineLabel) 2 else 1
    val dockTextScale by FloatPref("pref_dockTextScale", -1f, restart)
    val dockColoredGoogle by BooleanPref("pref_dockColoredGoogle", true, doNothing)

    /* --THEME-- */
    private var iconPack by StringPref("pref_icon_pack", context.resources.getString(R.string.config_default_icon_pack), reloadIconPacks)
    val iconPacks = object : MutableListPref<String>("pref_iconPacks", reloadIconPacks,
            if (!TextUtils.isEmpty(iconPack)) listOf(iconPack) else zimConfig.defaultIconPacks.asList()) {
        override fun unflattenValue(value: String) = value
    }
    val iconPackMasking by BooleanPref("pref_iconPackMasking", false, reloadIcons)
    val adaptifyIconPacks by BooleanPref("pref_generateAdaptiveForIconPack", false, reloadIcons)
    var launcherTheme by StringIntPref("pref_launcherTheme", 1) { ThemeManager.getInstance(context).updateTheme() }
    val primaryColor by IntPref("pref_key__primary_color", R.color.colorPrimary, restart)
    val accentColor by IntPref("pref_key__accent_color", R.color.colorAccent, restart)
    val dashColor by IntPref("pref_key__minibar_color", R.color.colorPrimary, restart)
    val settingIconColor by IntPref("pref_key__settings_color", R.color.colorAccent, recreate)
    var enableBlur by BooleanPref("pref_enableBlur", zimConfig.defaultEnableBlur(), updateBlur)
    val blurRadius by FloatPref("pref_blurRadius", zimConfig.defaultBlurStrength, updateBlur)
    val colorizedLegacyTreatment by BooleanPref("pref_colorizeGeneratedBackgrounds",
            zimConfig.enableColorizedLegacyTreatment(), reloadIcons)
    val enableWhiteOnlyTreatment by BooleanPref("pref_enableWhiteOnlyTreatment", zimConfig.enableWhiteOnlyTreatment(), reloadIcons)
    val enableLegacyTreatment by BooleanPref("pref_enableLegacyTreatment", zimConfig.enableLegacyTreatment(), reloadIcons)

    /* --SMARTSPACE-- */
    val enableSmartspace by BooleanPref("pref_smartspace", zimConfig.enableSmartspace())
    val smartspaceTime by BooleanPref("pref_smartspace_time", false, refreshGrid)
    val smartspaceTimeAbove by BooleanPref("pref_smartspace_time_above", false, refreshGrid)
    val smartspaceTime24H by BooleanPref("pref_smartspace_time_24_h", false, refreshGrid)
    val smartspaceDate by BooleanPref("pref_smartspace_date", true, refreshGrid)
    var smartspaceWidgetId by IntPref("smartspace_widget_id", -1, doNothing)
    var weatherProvider by StringPref("pref_smartspace_widget_provider",
            SmartspaceDataWidget::class.java.name, ::updateSmartspaceProvider)
    var eventProvider by StringPref("pref_smartspace_event_provider",
            SmartspaceDataWidget::class.java.name, ::updateSmartspaceProvider)
    val weatherUnit by StringBasedPref("pref_weather_units", Temperature.Unit.Celsius, ::updateSmartspaceProvider,
            Temperature.Companion::unitFromString, Temperature.Companion::unitToString) { }
    var usePillQsb by BooleanPref("pref_use_pill_qsb", false, recreate)
    var weatherIconPack by StringPref("pref_weatherIcons", "", updateWeatherData)

    var eventProviders = StringListPref("pref_smartspace_event_providers",
            ::updateSmartspaceProvider, listOf(eventProvider,
            NotificationUnreadProvider::class.java.name,
            NowPlayingProvider::class.java.name,
            BatteryStatusProvider::class.java.name,
            PersonalityProvider::class.java.name))

    /* --SEARCH-- */
    var searchProvider by StringPref("pref_globalSearchProvider",
            zimConfig.defaultSearchProvider) {
        SearchProviderController.getInstance(context).onSearchProviderChanged()
    }
    var voiceSearchIcon by BooleanPref("opa_enabled", false, recreate)
    var showAssistantIcon by BooleanPref("opa_assistant", false, recreate)
    val dualBubbleSearch by BooleanPref("pref_bubbleSearchStyle", false, recreate)
    var searchBarRadius by DimensionPref("pref_searchbarRadius", -1f)
    var allAppsGlobalSearch by BooleanPref("pref_allAppsGoogleSearch", false, doNothing)

    // Quickstep
    var swipeUpToSwitchApps by BooleanPref("pref_swipe_up_to_switch_apps_enabled", true, doNothing)
    val recentsRadius by DimensionPref("pref_recents_radius", context.resources.getInteger(R.integer.task_corner_radius).toFloat(), doNothing)
    val swipeLeftToGoBack by BooleanPref("pref_swipe_left_to_go_back", false) {
        OverviewInteractionState.INSTANCE.get(context).setBackButtonAlpha(1f, true)
    }

    //Notification
    val notificationCount: Boolean by BooleanPref("pref_notification_count", true, recreate)
    val notificationBackground by IntPref("pref_notification_background", R.color.notification_background, recreate)

    //Folder
    val folderBadgeCount by BooleanPref("pref_key__folder_badge_count", true)
    val folderBackground by IntPref("pref_key__folder_background", R.color.folderBackground, recreate)

    /* --DEV-- */
    var developerOptionsEnabled by BooleanPref("pref_developerOptionsEnabled", false, doNothing)
    private var debugMenuKey by StringPref("pref_debugMenuKey", "", doNothing)
    var debugMenuEnabled
        get() = debugMenuKey == Settings.Secure.ANDROID_ID
        set(value) {
            debugMenuKey = if (value) Settings.Secure.ANDROID_ID else ""
        }
    val debugOkHttp by BooleanPref("pref_debugOkhttp", onChange = restart)
    val showDebugInfo by BooleanPref("pref_showDebugInfo", false, doNothing)
    val lowPerformanceMode by BooleanPref("pref_lowPerformanceMode", false, recreate)
    val enablePhysics get() = !lowPerformanceMode

    /* --BACKUP-- */
    val recentBackups = object : MutableListPref<Uri>(
            Utilities.getDevicePrefs(context), "pref_recentBackups") {
        override fun unflattenValue(value: String) = Uri.parse(value)
    }

    val customAppName = object : MutableMapPref<ComponentKey, String>("pref_appNameMap", reloadAll) {
        override fun flattenKey(key: ComponentKey) = key.toString()
        override fun unflattenKey(key: String) = ComponentKey(ComponentName(context, key), Utilities.myUserHandle())
        override fun flattenValue(value: String) = value
        override fun unflattenValue(value: String) = value
    }
    val customAppIcon = object : MutableMapPref<ComponentKey, IconPackManager.CustomIconEntry>("pref_appIconMap", reloadAll) {
        override fun flattenKey(key: ComponentKey) = key.toString()
        override fun unflattenKey(key: String) = ComponentKey(ComponentName(context, key), Process.myUserHandle())
        override fun flattenValue(value: IconPackManager.CustomIconEntry) = value.toString()
        override fun unflattenValue(value: String) = IconPackManager.CustomIconEntry.fromString(value)
    }

    init {
        migrateConfig()
    }

    private fun migrateConfig() {
        if (configVersion != CURRENT_VERSION) {
            blockingEdit {
                bulkEdit {
                    // Migration codes here
                    configVersion = CURRENT_VERSION
                }
            }
        }
    }

    fun migrateConfig(prefs: SharedPreferences) {
        val version = prefs.getInt(VERSION_KEY, CURRENT_VERSION)
        if (version != CURRENT_VERSION) {
            with(prefs.edit()) {
                // Migration codes here

                if (version == 100) {
                    migrateFromV1(this, prefs)
                }

                putInt(VERSION_KEY, CURRENT_VERSION)
                commit()
            }
        }
    }

    private fun migrateFromV1(editor: SharedPreferences.Editor, prefs: SharedPreferences) = with(
            editor
    ) {
        // Set flags
        putBoolean("pref_legacyUpgrade", true)
        putBoolean("pref_restoreSuccess", false)
        // Reset icon shape to system shape
        // TODO: possibly create some sort of migration shape which uses a path stored in another pref
        // TODO: where we would move the current value of this to
        putString("pref_iconShape", "")

        // Dt2s
        putString("pref_gesture_double_tap",
                when (prefs.getString("pref_dt2sHandler", "")) {
                    "" -> BlankGestureHandler(context, null)
                    "org.zimmob.zimlx.gestures.dt2s.DoubleTapGesture\$SleepGestureHandlerTimeout" ->
                        SleepGestureHandlerTimeout(context, null)
                    else -> SleepGestureHandler(context, null)
                }.toString())

        // Dock
        putString("pref_dockPreset", "0")
        putBoolean("pref_dockShadow", false)
        putBoolean("pref_hotseatShowArrow", prefs.getBoolean("pref_hotseatShowArrow", true))
        putFloat("pref_dockRadius", 0f)
        putBoolean("pref_dockGradient", prefs.getBoolean("pref_isHotseatTransparent", false))
        if (!prefs.getBoolean("pref_hotseatShouldUseCustomOpacity", false)) {
            putFloat("pref_hotseatCustomOpacity", -1f / 255)
        }
        putFloat("pref_dockScale", prefs.getFloat("pref_hotseatHeightScale", 1f))

        // Home widget
        val pillQsb = prefs.getBoolean("pref_showPixelBar", true)
                // The new dock qsb should be close enough I guess
                && !prefs.getBoolean("pref_fullWidthSearchbar", false)
        putBoolean("pref_use_pill_qsb", pillQsb)
        if (pillQsb) {
            putBoolean("pref_dockSearchBar", false)
        }
        if (!prefs.getBoolean("pref_showDateOrWeather", true)) {
            putString("pref_smartspace_widget_provider", BlankDataProvider::class.java.name)
        }
        // Theme
        putString("pref_launcherTheme",
                when (prefs.getString("pref_theme", "0")) {
                    //"1" -> ThemeManager.THEME_DARK
                    //"2" -> ThemeManager.THEME_USE_BLACK or ThemeManager.THEME_DARK
                    else -> 0
                }.toString())
        putString("pref_icon_pack", prefs.getString("pref_iconPackPackage", ""))

        // Gestures
        putString("pref_gesture_swipe_down",
                when (prefs.getInt("pref_pulldownAction", 1)) {
                    1 -> NotificationsOpenGestureHandler(context, null)
                    2 -> StartGlobalSearchGestureHandler(context, null)
                    3 -> StartAppSearchGestureHandler(context, null)
                    else -> BlankGestureHandler(context, null)
                }.toString())
        if (prefs.getBoolean("pref_homeOpensDrawer", false)) {
            putString("pref_gesture_press_home",
                    OpenDrawerGestureHandler(context, null).toString())
        }

        // misc
        putBoolean("pref_add_icon_to_home", prefs.getBoolean("pref_autoAddShortcuts", true))

        // Disable some newer features per default
        putBoolean("pref_allAppsGoogleSearch", false)
    }

    fun addOnPreferenceChangeListener(listener: OnPreferenceChangeListener, vararg keys: String) {
        keys.forEach { addOnPreferenceChangeListener(it, listener) }
    }

    fun addOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        if (onChangeListeners[key] == null) {
            onChangeListeners[key] = HashSet()
        }
        onChangeListeners[key]?.add(listener)
        listener.onValueChanged(key, this, true)
    }

    fun removeOnPreferenceChangeListener(listener: OnPreferenceChangeListener, vararg keys: String) {
        keys.forEach { removeOnPreferenceChangeListener(it, listener) }
    }

    fun removeOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        onChangeListeners[key]?.remove(listener)
    }

    fun getOnChangeCallback() = onChangeCallback

    fun recreate() {
        onChangeCallback?.recreate()
    }

    fun reloadApps() {
        onChangeCallback?.reloadApps()
    }

    fun reloadAll() {
        onChangeCallback?.reloadAll()
    }

    fun restart() {
        onChangeCallback?.restart()
    }

    fun refreshGrid() {
        onChangeCallback?.refreshGrid()
    }

    fun updateBlur() {
        onChangeCallback?.updateBlur()
    }

    fun updateSmartspaceProvider() {
        onChangeCallback?.updateSmartspaceProvider()
    }

    private fun updateSmartspace() {
        onChangeCallback?.updateSmartspace()
    }

    fun reloadIcons() {
        LauncherAppState.getInstance(context).reloadIconCache()
        runOnMainThread {
            onChangeCallback?.recreate()
        }
    }

    fun updateSortApps() {
        onChangeCallback?.forceReloadApps()
    }

    inline fun withChangeCallback(
            crossinline callback: (ZimPreferencesChangeCallback) -> Unit): () -> Unit {
        return { getOnChangeCallback()?.let { callback(it) } }
    }

    inner class ResettableLazy<out T : Any>(private val create: () -> T) {

        private var initialized = false
        private var currentValue: T? = null

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!initialized) {
                currentValue = create()
                initialized = true
            }
            return currentValue!!
        }

        fun resetValue() {
            initialized = false
            currentValue = null
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        onChangeMap[key]?.invoke()
        onChangeListeners[key]?.forEach {
            if (key != null) {
                it.onValueChanged(key, this, false)
            }
        }
    }

    fun registerCallback(callback: ZimPreferencesChangeCallback) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        onChangeCallback = callback
    }

    fun unregisterCallback() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
        onChangeCallback = null
    }

    // ----------------
    // Helper functions and class
    // ----------------

    fun commitOrApply(editor: SharedPreferences.Editor, commit: Boolean) {
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    var blockingEditing = false
    var bulkEditing = false
    var editor: SharedPreferences.Editor? = null

    fun beginBlockingEdit() {
        blockingEditing = true
    }

    fun endBlockingEdit() {
        blockingEditing = false
    }

    @SuppressLint("CommitPrefEdits")
    fun beginBulkEdit() {
        bulkEditing = true
        editor = sharedPrefs.edit()
    }

    fun endBulkEdit() {
        bulkEditing = false
        commitOrApply(editor!!, blockingEditing)
        editor = null
    }

    inline fun blockingEdit(body: ZimPreferences.() -> Unit) {
        beginBlockingEdit()
        body(this)
        endBlockingEdit()
    }

    inline fun bulkEdit(body: ZimPreferences.() -> Unit) {
        beginBulkEdit()
        body(this)
        endBulkEdit()
    }

    //PREFERENCE CLASSES
    abstract inner class PrefDelegate<T : Any>(val key: String, val defaultValue: T, private val onChange: () -> Unit) {

        private var cached = false
        private lateinit var value: T

        init {
            onChangeMap[key] = { onValueChanged() }
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!cached) {
                value = onGetValue()
                cached = true
            }
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            cached = false
            onSetValue(value)
        }

        abstract fun onGetValue(): T

        abstract fun onSetValue(value: T)

        protected inline fun edit(body: SharedPreferences.Editor.() -> Unit) {
            @SuppressLint("CommitPrefEdits")
            val editor = if (bulkEditing) editor!! else sharedPrefs.edit()
            body(editor)
            if (!bulkEditing)
                commitOrApply(editor, blockingEditing)
        }

        internal fun getKey() = key

        private fun onValueChanged() {
            discardCachedValue()
            cached = false
            onChange.invoke()
        }

        private fun discardCachedValue() {
            if (cached) {
                cached = false
                value.let(::disposeOldValue)
            }
        }

        open fun disposeOldValue(oldValue: T) {

        }
    }

    open inner class StringSetPref(key: String, defaultValue: Set<String>, onChange: () -> Unit = doNothing) :
            PrefDelegate<Set<String>>(key, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)!!

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    open inner class BooleanPref(key: String, defaultValue: Boolean = false, onChange: () -> Unit = doNothing) :
            PrefDelegate<Boolean>(key, defaultValue, onChange) {
        override fun onGetValue(): Boolean = sharedPrefs.getBoolean(getKey(), defaultValue)

        override fun onSetValue(value: Boolean) {
            edit { putBoolean(getKey(), value) }
        }
    }

    open inner class IntPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) :
            PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    inline fun <reified T : Enum<T>> EnumPref(key: String, defaultValue: T,
                                              noinline onChange: () -> Unit = doNothing): PrefDelegate<T> {
        return IntBasedPref(key, defaultValue, onChange, { value ->
            enumValues<T>().firstOrNull { item -> item.ordinal == value } ?: defaultValue
        }, { it.ordinal }, { })
    }

    open inner class IntBasedPref<T : Any>(key: String, defaultValue: T, onChange: () -> Unit = doNothing,
                                           private val fromInt: (Int) -> T,
                                           private val toInt: (T) -> Int,
                                           private val dispose: (T) -> Unit) : PrefDelegate<T>(key, defaultValue, onChange) {
        override fun onGetValue(): T {
            return if (sharedPrefs.contains(key)) {
                fromInt(sharedPrefs.getInt(getKey(), toInt(defaultValue)))
            } else defaultValue
        }

        override fun onSetValue(value: T) {
            edit { putInt(getKey(), toInt(value)) }
        }

        override fun disposeOldValue(oldValue: T) {
            dispose(oldValue)
        }
    }

    abstract inner class MutableMapPref<K, V>(private val prefKey: String, onChange: () -> Unit = doNothing) {
        private val valueMap = HashMap<K, V>()

        init {
            val obj = JSONObject(sharedPrefs.getString(prefKey, "{}"))
            obj.keys().forEach {
                valueMap[unflattenKey(it)] = unflattenValue(obj.getString(it))
            }
            if (onChange !== doNothing) {
                onChangeMap[prefKey] = onChange
            }
        }

        fun toMap() = HashMap<K, V>(valueMap)

        open fun flattenKey(key: K) = key.toString()
        abstract fun unflattenKey(key: String): K

        open fun flattenValue(value: V) = value.toString()
        abstract fun unflattenValue(value: String): V

        operator fun set(key: K, value: V?) {
            if (value != null) {
                valueMap[key] = value
            } else {
                valueMap.remove(key)
            }
            saveChanges()
        }

        private fun saveChanges() {
            val obj = JSONObject()
            valueMap.entries.forEach { obj.put(flattenKey(it.key), flattenValue(it.value)) }
            @SuppressLint("CommitPrefEdits")
            val editor = if (bulkEditing) editor!! else sharedPrefs.edit()
            editor.putString(prefKey, obj.toString())
            if (!bulkEditing)
                commitOrApply(editor, blockingEditing)
        }

        operator fun get(key: K): V? {
            return valueMap[key]
        }

        fun clear() {
            valueMap.clear()
            saveChanges()
        }
    }

    open inner class StringBasedPref<T : Any>(key: String, defaultValue: T, onChange: () -> Unit = doNothing,
                                              private val fromString: (String) -> T,
                                              private val toString: (T) -> String,
                                              private val dispose: (T) -> Unit) :
            PrefDelegate<T>(key, defaultValue, onChange) {
        override fun onGetValue(): T = sharedPrefs.getString(getKey(), null)?.run(fromString)
                ?: defaultValue

        override fun onSetValue(value: T) {
            edit { putString(getKey(), toString(value)) }
        }

        override fun disposeOldValue(oldValue: T) {
            dispose(oldValue)
        }
    }

    open inner class FloatPref(key: String, defaultValue: Float = 0f, onChange: () -> Unit = doNothing) :
            PrefDelegate<Float>(key, defaultValue, onChange) {
        override fun onGetValue(): Float = sharedPrefs.getFloat(getKey(), defaultValue)

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), value) }
        }
    }

    open inner class DimensionPref(key: String, defaultValue: Float = 0f, onChange: () -> Unit = doNothing) :
            PrefDelegate<Float>(key, defaultValue, onChange) {

        override fun onGetValue(): Float = dpToPx(sharedPrefs.getFloat(getKey(), defaultValue))

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), pxToDp(value)) }
        }
    }

    open inner class StringPref(key: String, defaultValue: String = "", onChange: () -> Unit = doNothing) :
            PrefDelegate<String>(key, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)!!

        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    open inner class StringIntPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) :
            PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getString(getKey(), "$defaultValue")!!.toInt()

        override fun onSetValue(value: Int) {
            edit { putString(getKey(), "$value") }
        }
    }

    inner class StringListPref(prefKey: String,
                               onChange: () -> Unit = doNothing,
                               default: List<String> = emptyList())
        : MutableListPref<String>(prefKey, onChange, default) {

        override fun unflattenValue(value: String) = value
        override fun flattenValue(value: String) = value
    }

    open inner class AlphaPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) :
            PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = (sharedPrefs.getFloat(getKey(), defaultValue.toFloat() / 255) * 255).roundToInt()

        override fun onSetValue(value: Int) {
            edit { putFloat(getKey(), value.toFloat() / 255) }
        }
    }

    abstract inner class MutableListPref<T>(private val prefs: SharedPreferences,
                                            private val prefKey: String,
                                            onChange: () -> Unit = doNothing,
                                            default: List<T> = emptyList()) {

        constructor(prefKey: String, onChange: () -> Unit = doNothing, default: List<T> = emptyList())
                : this(sharedPrefs, prefKey, onChange, default)

        private val valueList = ArrayList<T>()
        private val listeners: MutableSet<MutableListPrefChangeListener> = Collections.newSetFromMap(WeakHashMap())

        init {
            val arr = JSONArray(prefs.getString(prefKey, getJsonString(default)))
            (0 until arr.length()).mapTo(valueList) { unflattenValue(arr.getString(it)) }
            if (onChange != doNothing) {
                onChangeMap[prefKey] = onChange
            }
        }

        fun toList() = ArrayList<T>(valueList)

        open fun flattenValue(value: T) = value.toString()
        abstract fun unflattenValue(value: String): T

        operator fun get(position: Int): T {
            return valueList[position]
        }

        operator fun set(position: Int, value: T) {
            valueList[position] = value
            saveChanges()
        }

        fun getAll(): List<T> = valueList

        fun setAll(value: List<T>) {
            if (value == valueList) return
            valueList.clear()
            valueList.addAll(value)
            saveChanges()
        }

        fun add(value: T) {
            valueList.add(value)
            saveChanges()
        }

        fun add(position: Int, value: T) {
            valueList.add(position, value)
            saveChanges()
        }

        fun remove(value: T) {
            valueList.remove(value)
            saveChanges()
        }

        fun removeAt(position: Int) {
            valueList.removeAt(position)
            saveChanges()
        }

        fun contains(value: T): Boolean {
            return valueList.contains(value)
        }

        fun replaceWith(newList: List<T>) {
            valueList.clear()
            valueList.addAll(newList)
            saveChanges()
        }

        fun getList() = valueList

        fun addListener(listener: MutableListPrefChangeListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: MutableListPrefChangeListener) {
            listeners.remove(listener)
        }

        private fun saveChanges() {
            @SuppressLint("CommitPrefEdits")
            val editor = prefs.edit()
            editor.putString(prefKey, getJsonString(valueList))
            if (!bulkEditing)
                commitOrApply(editor, blockingEditing)
        }

        private fun getJsonString(list: List<T>): String {
            val arr = JSONArray()
            list.forEach { arr.put(flattenValue(it)) }
            return arr.toString()
        }
    }

    interface MutableListPrefChangeListener {

        fun onListPrefChanged(key: String)
    }

    interface OnPreferenceChangeListener {
        fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean)
    }

    /*INSTANCE*/
    companion object {

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: ZimPreferences? = null

        const val CURRENT_VERSION = 200
        const val VERSION_KEY = "config_version"

        fun getInstance(context: Context): ZimPreferences {
            if (INSTANCE == null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    INSTANCE = ZimPreferences(context.applicationContext)
                } else {
                    try {
                        return MainThreadExecutor().submit(Callable { getInstance(context) }).get()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    } catch (e: ExecutionException) {
                        throw RuntimeException(e)
                    }

                }
            }
            return INSTANCE!!
        }

        fun getInstanceNoCreate(): ZimPreferences {
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE?.apply {
                onChangeListeners.clear()
                onChangeCallback = null
                gridSizeDelegate.resetValue()
                dockGridSizeDelegate.resetValue()
                drawerGridSizeDelegate.resetValue()
            }
        }
    }
}