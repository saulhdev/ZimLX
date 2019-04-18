package org.zimmob.zimlx

import com.android.launcher3.compat.UserManagerCompat

class ZimPreferencesChangeCallback(private val launcher: ZimLauncher) {

    fun recreate() {
        if (launcher.shouldRecreate()) launcher.recreate()
    }

    fun reloadApps() {
        UserManagerCompat.getInstance(launcher).userProfiles.forEach { launcher.model.forceReload() }
    }

    fun reloadAll() {
        launcher.model.forceReload()
    }

    fun restart() {
        launcher.scheduleRestart()
    }

    fun refreshGrid() {
        launcher.refreshGrid()
    }

    fun resetAllApps() {
        launcher.mAllAppsController.reset()
    }
}
