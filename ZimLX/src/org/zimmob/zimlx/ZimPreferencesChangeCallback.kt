package org.zimmob.zimlx

import com.android.launcher3.compat.UserManagerCompat
import com.android.launcher3.pageindicators.WorkspacePageIndicator
import org.zimmob.zimlx.blur.BlurWallpaperProvider

class ZimPreferencesChangeCallback(val launcher: ZimLauncher) {

    fun recreate() {
        if (launcher.shouldRecreate()) launcher.recreate()
    }

    fun reloadApps() {
        UserManagerCompat.getInstance(launcher).userProfiles.forEach { launcher.model.onPackagesReload(it) }
    }

    fun forceReloadApps() {
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

    fun updateBlur() {
        BlurWallpaperProvider.getInstance(launcher).updateAsync()
    }

    fun resetAllApps() {
        launcher.mAllAppsController.reset()
    }

    fun updatePageIndicator() {
        val indicator = launcher.workspace.pageIndicator
        if (indicator is WorkspacePageIndicator) {
            indicator.updateLineHeight()
        }
    }

    fun updateSmartspaceProvider() {
        launcher.zimApp.smartspace.onProviderChanged()
    }

    fun updateSmartspace() {
        launcher.refreshGrid()
    }

    fun reloadIcons() {
        org.zimmob.zimlx.reloadIcons(launcher)
    }
}
