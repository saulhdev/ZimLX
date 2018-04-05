package org.zimmob.zimlx.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.toolbar.*
import org.zimmob.zimlx.R
import org.zimmob.zimlx.core.activity.CoreHome
import org.zimmob.zimlx.core.util.DatabaseHelper
import org.zimmob.zimlx.core.widget.AppDrawerController
import org.zimmob.zimlx.util.AppManager
import org.zimmob.zimlx.util.AppSettings
import org.zimmob.zimlx.util.LauncherAction
import org.zimmob.zimlx.viewutil.DialogHelper

class SettingsActivity : ThemeActivity() {

    private var appSettings: AppSettings? = null
    private var shouldLauncherRestart = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        appSettings = AppSettings.get()
        toolbar.setTitle(R.string.settings)
        toolbar.navigationIcon = this@SettingsActivity.getDrawable(R.drawable.ic_arrow_back_white_24px)
        toolbar.setNavigationOnClickListener { this@SettingsActivity.onBackPressed() }
        toolbar.setBackgroundColor(AppSettings.get().primaryColor)

        showFragment(SettingsFragmentMaster.TAG, false)
    }

    private fun showFragment(tag: String, addToBackStack: Boolean) {
        var fragment: PreferenceFragmentCompat? = supportFragmentManager.findFragmentByTag(tag) as PreferenceFragmentCompat?
        if (fragment == null) {
            when (tag) {
                SettingsFragmentDesktop.TAG -> {
                    fragment = SettingsFragmentDesktop()
                    toolbar.setTitle(R.string.pref_title__desktop)
                }
                SettingsFragmentAppDrawer.TAG -> {
                    fragment = SettingsFragmentAppDrawer()
                    toolbar.setTitle(R.string.pref_title__app_drawer)
                }
                SettingsFragmentDock.TAG -> {
                    fragment = SettingsFragmentDock()
                    toolbar.setTitle(R.string.pref_title__dock)
                }
                SettingsFragmentGestures.TAG -> {
                    fragment = SettingsFragmentGestures()
                    toolbar.setTitle(R.string.pref_title__gestures)
                }
                SettingsFragmentAppearance.TAG -> {
                    fragment = SettingsFragmentAppearance()
                    toolbar.setTitle(R.string.pref_title__appearance)
                }
                SettingsFragmentFolders.TAG -> {
                    fragment = SettingsFragmentFolders()
                    toolbar.setTitle(R.string.pref_title__folders)
                }

                SettingsFragmentNotifications.TAG -> {
                    fragment = SettingsFragmentNotifications()
                    toolbar.setTitle(R.string.pref_title__notifications)
                }

                SettingsFragmentDebug.TAG -> {
                    fragment = SettingsFragmentDebug()
                    toolbar.setTitle(R.string.pref_title__debug)
                }
                SettingsFragmentAdvanced.TAG -> {
                    fragment = SettingsFragmentAdvanced()
                    toolbar.setTitle(R.string.pref_title__advanced)
                }
                SettingsFragmentMaster.TAG -> {
                    fragment = SettingsFragmentMaster()
                    toolbar.setTitle(R.string.settings)
                }
                else -> {
                    fragment = SettingsFragmentMaster()
                    toolbar.setTitle(R.string.settings)
                }
            }
        }
        val t = supportFragmentManager.beginTransaction()
        t.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)

        if (addToBackStack) {
            t.addToBackStack(tag)
        }
        t.replace(R.id.settings_fragment_container, fragment, tag).commit()
    }

    override fun onPause() {
        appSettings!!.appRestartRequired = shouldLauncherRestart
        super.onPause()
    }

    class SettingsFragmentMaster : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_master)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val settings = AppSettings.get()
                val key = preference.key
                if (settings.isKeyEqual(key, R.string.pref_key__cat_desktop)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentDesktop.TAG, true)
                    return true
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_app_drawer)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentAppDrawer.TAG, true)
                    return true
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_dock)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentDock.TAG, true)
                    return true
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_gestures)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentGestures.TAG, true)
                    return true
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_appearance)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentAppearance.TAG, true)
                    return true
                }
                else if (settings.isKeyEqual(key, R.string.pref_key__cat_folders)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentFolders.TAG, true)
                    return true
                }
                else if (settings.isKeyEqual(key, R.string.pref_key__cat_notifications)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentNotifications.TAG, true)
                    return true
                }else if (settings.isKeyEqual(key, R.string.pref_key__cat_debug)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentDebug.TAG, true)
                    return true
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_advanced)) {
                    (activity as SettingsActivity).showFragment(SettingsFragmentAdvanced.TAG, true)
                    return true
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_about)) {
                    startActivity(Intent(activity, AboutActivity::class.java))
                    return true
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        @SuppressLint("DefaultLocale")
        override fun onResume() {
            super.onResume()
            (activity as SettingsActivity).toolbar!!.setTitle(R.string.settings)

            val settings = AppSettings.get()

            val desktopSummary = String.format("%s: %d x %d", getString(R.string.pref_title__size), settings.desktopColumnCount, settings.desktopRowCount)
            findPreference(getString(R.string.pref_key__cat_desktop)).summary = desktopSummary

            val dockSummary = String.format("%s: %d", getString(R.string.pref_title__size), settings.dockSize)
            findPreference(getString(R.string.pref_key__cat_dock)).summary = dockSummary

            var drawerSummary = String.format("%s: ", getString(R.string.pref_title__style))
            when (settings.drawerStyle) {
                AppDrawerController.DrawerMode.HORIZONTAL_PAGED -> drawerSummary += getString(R.string.horizontal_paged_drawer)
                AppDrawerController.DrawerMode.VERTICAL -> drawerSummary += getString(R.string.vertical_scroll_drawer)
            }
            findPreference(getString(R.string.pref_key__cat_app_drawer)).summary = drawerSummary

            val iconsSummary = String.format("%s: %ddp", getString(R.string.pref_title__size), settings.iconSize)
            findPreference(getString(R.string.pref_key__cat_appearance)).summary = iconsSummary
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentMaster"
        }
    }

    class SettingsFragmentDesktop : BasePreferenceFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_desktop)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key

                if (key == getString(R.string.pref_key__minibar)) {
                    LauncherAction.RunAction(LauncherAction.Action.EditMinBar, activity)
                    return true
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
            if (key == getString(R.string.pref_key__desktop_indicator_style)) {
                Home.launcher?.getDesktopIndicator()?.setMode(appSettings.desktopIndicatorMode)
            }
            if (key == getString(R.string.pref_title__desktop_show_position_indicator)) {
                Home.launcher?.updateDesktopIndicatorVisibility()
            }
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentDesktop"

            private val requireRestartPreferenceIds = intArrayOf(R.string.pref_key__desktop_columns, R.string.pref_key__desktop_rows, R.string.pref_key__desktop_style, R.string.pref_key__desktop_fullscreen, R.string.pref_key__desktop_show_label,

                    R.string.pref_key__search_bar_enable, R.string.pref_key__search_bar_show_hidden_apps,

                    R.string.pref_key__desktop_background_color, R.string.pref_key__minibar_background_color)
        }
    }

    class SettingsFragmentDock : BasePreferenceFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_dock)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val settings = AppSettings.get()
                val key = preference.key
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
            if (key == getString(R.string.pref_key__dock_enable)) {
                Home.launcher?.updateDock(true)
            }
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentDock"

            private val requireRestartPreferenceIds = intArrayOf(R.string.pref_key__dock_enable, R.string.pref_key__dock_size, R.string.pref_key__dock_show_label, R.string.pref_key__dock_background_color)
        }
    }

    class SettingsFragmentAppDrawer : BasePreferenceFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_app_drawer)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key

                if (key == getString(R.string.pref_key__hidden_apps)) {
                    Log.i("PREFERENCES", "HANDLE HIDDEN APPS")
                    val intent = Intent(activity, HideAppsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    (activity as SettingsActivity).shouldLauncherRestart = true
                    return true
                }


            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentAppDrawer"

            private val requireRestartPreferenceIds = intArrayOf(
                    R.string.pref_key__drawer_columns,
                    R.string.pref_key__drawer_rows,
                    R.string.pref_key__drawer_style,
                    R.string.pref_key__drawer_show_card_view,
                    R.string.pref_key__drawer_show_position_indicator,
                    R.string.pref_key__drawer_show_label,
                    R.string.pref_key__drawer_background_color,
                    R.string.pref_key__drawer_card_color,
                    R.string.pref_key__drawer_label_color,
                    R.string.pref_key__drawer_fast_scroll_color,
                    R.string.pref_key__date_bar_date_format_custom_1,
                    R.string.pref_key__date_bar_date_format_custom_2,
                    R.string.pref_key__date_bar_date_format_type,
                    R.string.pref_key__date_bar_date_text_color,
                    R.string.pref_key__sort_mode)
        }
    }

    class SettingsFragmentFolders : BasePreferenceFragment(){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_folders)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentFolders"

            private val requireRestartPreferenceIds = intArrayOf(
                    R.string.pref_key__drawer_columns)
        }

    }

    class SettingsFragmentGestures : BasePreferenceFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_gestures)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentGestures"

            private val requireRestartPreferenceIds = intArrayOf()
        }
    }

    class SettingsFragmentAppearance : BasePreferenceFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_appearance)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key

                if (key == getString(R.string.pref_key__icon_pack)) {
                    AppManager.getInstance(activity!!).startPickIconPackIntent(activity!!)
                    return true
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentAppareance"

            private val requireRestartPreferenceIds = intArrayOf(R.string.pref_key__icon_size, R.string.pref_key__icon_pack)
        }
    }

    class SettingsFragmentNotifications: BasePreferenceFragment(){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_notifications)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key


            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentNotifications"

            private val requireRestartPreferenceIds = intArrayOf(R.string.pref_key__notifications)
        }
    }

    class SettingsFragmentDebug : BasePreferenceFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_debug)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key

                if (key == getString(R.string.pref_key__clear_database)) {
                    DialogHelper.alertDialog(context!!, "Clear user data", "Are you sure, all your shortcuts will be removed.", MaterialDialog.SingleButtonCallback { dialog, which ->
                        if (Home.launcher != null)
                            Home.launcher?.recreate()
                        (CoreHome.db as DatabaseHelper).onUpgrade((CoreHome.db as DatabaseHelper).writableDatabase, 1, 1)
                        activity!!.finish()
                    })
                    return true
                }

                if (key == getString(R.string.pref_key__restart)) {
                    if (Home.launcher != null)
                        Home.launcher?.recreate()
                    activity!!.finish()
                    return true
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentDebug"

            private val requireRestartPreferenceIds = intArrayOf(R.string.pref_title__clear_database)
        }
    }

    class SettingsFragmentAdvanced : BasePreferenceFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "app"
            addPreferencesFromResource(R.xml.preferences_advanced)

            if (resources.getBoolean(R.bool.isTablet)) {
                val c = findPreference(getString(R.string.pref_key__cat_advanced)) as PreferenceCategory
                val p = c.findPreference(getString(R.string.pref_key__desktop_rotate))
                p.parent?.removePreference(p)
            }
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            if (isAdded && preference.hasKey()) {
                val key = preference.key
                val activity = activity

                if (key == getString(R.string.pref_key__backup)) {
                    if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        DialogHelper.backupDialog(activity)
                    } else {
                        ActivityCompat.requestPermissions(Home.launcher!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CoreHome.REQUEST_PERMISSION_STORAGE)
                    }
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onPause() {
            appSettings.unregisterPreferenceChangedListener(this)
            super.onPause()
        }

        override fun onResume() {
            appSettings.registerPreferenceChangedListener(this)
            super.onResume()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key)
        }

        companion object {
            val TAG = "org.zimmob.zimlx.settings.SettingsFragmentAdvanced"

            private val requireRestartPreferenceIds = intArrayOf(R.string.pref_summary__backup, R.string.pref_summary__theme)
        }
    }

    abstract class BasePreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        var appSettings: AppSettings = AppSettings.get()

        fun checkIfPreferenceChangedRequireRestart(ids: IntArray, key: String) {
            val settingsActivity = activity as SettingsActivity?

            if (settingsActivity!!.shouldLauncherRestart) return
            for (id in ids) {
                if (getString(id) == key) {
                    settingsActivity.shouldLauncherRestart = true
                    return
                }
            }
        }
    }
}
