package org.zimmob.zimlx

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Looper
import com.android.launcher3.*
import com.android.launcher3.util.ComponentKey
import org.json.JSONArray
import org.json.JSONObject
import org.zimmob.zimlx.settings.GridSize
import org.zimmob.zimlx.util.ZimFlags
import org.zimmob.zimlx.util.ZimFlags.FOLDER_SHAPE_SQUARE
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KProperty

class ZimPreferences(val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val onChangeMap: MutableMap<String, () -> Unit> = HashMap()
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
        return context.applicationContext.getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    }

    private val doNothing = { }
    private val recreate = { recreate() }
    private val reloadApps = { reloadApps() }
    private val reloadAll = { reloadAll() }
    private val restart = { restart() }
    private val refreshGrid = { refreshGrid() }

    var restoreSuccess by BooleanPref("pref_restoreSuccess", false)
    var configVersion by IntPref("config_version", if (restoreSuccess) 0 else CURRENT_VERSION)

    // Theme
    var iconPack by StringPref(ZimFlags.ICON_PACK, "", doNothing)
    var overrideLauncherTheme by BooleanPref(ZimFlags.OVERRIDE_LAUNCHER_THEME, false, recreate)
    val usePixelIcons by BooleanPref(ZimFlags.USE_PIXEL_ICONS, true)
    val primaryColor by IntPref(ZimFlags.PRIMARY_COLOR, R.color.colorPrimary, recreate)
    val accentColor by IntPref(ZimFlags.ACCENT_COLOR, R.color.colorAccent)
    val minibarColor by IntPref(ZimFlags.MINIBAR_COLOR, R.color.colorPrimary, recreate)

    // Desktop
    val smartspaceTime by BooleanPref("pref_smartspace_time", false, refreshGrid)
    val smartspaceDate by BooleanPref("pref_smartspace_date", false, refreshGrid)
    val allowFullWidthWidgets by BooleanPref("pref_fullWidthWidgets", false, restart)
    val gridSize by lazy { GridSize(this, "numRows", "numColumns", LauncherAppState.getIDP(context)) }
    val desktopIconScale by FloatPref(ZimFlags.DESKTOP_ICON_SCALE, 1f)
    val hideAppLabels by BooleanPref("pref_hideAppLabels", false, recreate)

    // Dock
    val hideDockGradient by BooleanPref("pref_key__hide_dock_gradient", false, recreate)
    val hideDockButton by BooleanPref("pref_key__hide_dock_button", false, recreate)
    val dockSearchBar = true
    val hotseatIconScale by FloatPref("pref_hotseatIconScale", 1f, recreate)
    val hotseatHeightScale by FloatPref(ZimFlags.HOTSEAT_HEIGHT_SCALE, 1f)
    val hotseatShowArrow by BooleanPref("pref_hotseatShowArrow", true)
    val twoRowDock by BooleanPref("pref_twoRowDock", false, recreate)
    val transparentHotseat by BooleanPref(ZimFlags.HOTSEAT_TRANSPARENT, false, recreate)
    val hideHotseat by BooleanPref(ZimFlags.HOTSEAT_HIDE, false, recreate)

    fun numHotseatIcons(default: String): String {
        return sharedPrefs.getString("pref_title__num_hotseat_icons", default)
    }

    //Folder
    val folderBadgeCount by BooleanPref("pref_key__folder_badge_count", true)
    val folderShape by IntPref("pref_key__folder_shape", FOLDER_SHAPE_SQUARE)

    // Drawer
    val hideAllAppsAppLabels by BooleanPref("pref_hideAllAppsAppLabels", false, recreate)
    val allAppsIconScale by FloatPref(ZimFlags.APPDRAWER_ICON_SCALE, 1f, recreate)
    val iconLabelsInTwoLines by BooleanPref("pref_key__labels_two_lines", true)
    val allAppsIconPaddingScale by FloatPref(ZimFlags.APPDRAWER_ALL_APPS_ICON_PADDING_SCALE, 1f)

    fun getNumPredictedApps(): String {
        return sharedPrefs.getString("pref_predictive_apps_values", "5")
    }

    //Notification
    val notificationCount: Boolean by BooleanPref("pref_notification_count", true)
    val notificationBackground by IntPref("pref_notification_background", R.color.notification_background)


    // Dev
    var developerOptionsEnabled by BooleanPref("pref_developerOptionsEnabled", false, doNothing)
    val showDebugInfo by BooleanPref("pref_showDebugInfo", false, doNothing)

    var hiddenAppSet by StringSetPref("hidden-app-set", Collections.emptySet(), reloadApps)
    val customAppName = object : MutableMapPref<ComponentKey, String>("pref_appNameMap", reloadAll) {
        override fun flattenKey(key: ComponentKey) = key.toString()
        override fun unflattenKey(key: String) = ComponentKey(context, key)
        override fun flattenValue(value: String) = value
        override fun unflattenValue(value: String) = value
    }

    val recentBackups = object : MutableListPref<Uri>(
            Utilities.getDevicePrefs(context), "pref_recentBackups") {
        override fun unflattenValue(value: String) = Uri.parse(value)
    }

    private fun recreate() {
        onChangeCallback?.recreate()
    }

    private fun reloadApps() {
        onChangeCallback?.reloadApps()
    }

    private fun reloadAll() {
        onChangeCallback?.reloadAll()
    }

    private fun restart() {
        onChangeCallback?.restart()
    }

    fun refreshGrid() {
        onChangeCallback?.refreshGrid()
    }

    abstract inner class MutableListPref<T>(private val prefs: SharedPreferences,
                                            private val prefKey: String,
                                            onChange: () -> Unit = doNothing) {

        constructor(prefKey: String, onChange: () -> Unit = doNothing) : this(sharedPrefs, prefKey, onChange)

        private val valueList = ArrayList<T>()

        init {
            val arr = JSONArray(prefs.getString(prefKey, "[]"))
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

        private fun saveChanges() {
            val arr = JSONArray()
            valueList.forEach { arr.put(flattenValue(it)) }
            @SuppressLint("CommitPrefEdits")
            val editor = prefs.edit()
            editor.putString(prefKey, arr.toString())
            if (!bulkEditing)
                commitOrApply(editor, blockingEditing)
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
    }

    open inner class StringPref(key: String, defaultValue: String = "", onChange: () -> Unit = doNothing) :
            PrefDelegate<String>(key, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)

        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    open inner class StringSetPref(key: String, defaultValue: Set<String>, onChange: () -> Unit = doNothing) :
            PrefDelegate<Set<String>>(key, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    open inner class IntPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) :
            PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    open inner class FloatPref(key: String, defaultValue: Float = 0f, onChange: () -> Unit = doNothing) :
            PrefDelegate<Float>(key, defaultValue, onChange) {
        override fun onGetValue(): Float = sharedPrefs.getFloat(getKey(), defaultValue)

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), value) }
        }
    }

    open inner class BooleanPref(key: String, defaultValue: Boolean = false, onChange: () -> Unit = doNothing) :
            PrefDelegate<Boolean>(key, defaultValue, onChange) {
        override fun onGetValue(): Boolean = sharedPrefs.getBoolean(getKey(), defaultValue)

        override fun onSetValue(value: Boolean) {
            edit { putBoolean(getKey(), value) }
        }
    }

    private inner class MutableStringPref(key: String, defaultValue: String = "") :
            StringPref(key, defaultValue), MutablePrefDelegate<String> {
        fun onSetValue(thisRef: Any?, property: KProperty<*>, value: String) {
            edit { putString(key, value) }
        }
    }

    private interface MutablePrefDelegate<T> {
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }
    // ----------------
    // Helper functions and class
    // ----------------

    fun getPrefKey(key: String) = "pref_$key"

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
            cached = false
            onChange.invoke()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        onChangeMap[key]?.invoke()
    }

    fun registerCallback(callback: ZimPreferencesChangeCallback) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        onChangeCallback = callback
    }

    fun unregisterCallback() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
        onChangeCallback = null
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

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: ZimPreferences? = null

        const val CURRENT_VERSION = 200

        fun getInstance(context: Context): ZimPreferences {
            if (INSTANCE == null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    INSTANCE = ZimPreferences(context.applicationContext)
                } else {
                    try {
                        return MainThreadExecutor().submit(Callable { ZimPreferences.getInstance(context) }).get()
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
    }
}