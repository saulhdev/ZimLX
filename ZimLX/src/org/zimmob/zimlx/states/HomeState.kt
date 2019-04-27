package org.zimmob.zimlx.states

import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.Utilities
import org.zimmob.zimlx.zimPrefs

open class HomeState(id: Int, containerType: Int, transitionDuration: Int, flags: Int) :
        LauncherState(id, containerType, transitionDuration, flags) {

    override fun getScrimProgress(launcher: Launcher): Float {
        if (!launcher.zimPrefs.dockGradientStyle) {
            return getNormalProgress(launcher)
        }
        return super.getScrimProgress(launcher)
    }

    companion object {

        fun getNormalProgress(launcher: Launcher): Float {
            return 1 - (getScrimHeight(launcher) / launcher.allAppsController.shiftRange)
        }

        private fun getScrimHeight(launcher: Launcher): Float {
            val dp = launcher.deviceProfile
            val prefs = Utilities.getZimPrefs(launcher)

            return if (prefs.dockHide) {
                dp.allAppsCellHeightPx - dp.allAppsIconTextSizePx
            } else {
                (dp.hotseatCellHeightPx * prefs.dockRowsCount + dp.hotseatBarTopPaddingPx) * prefs.dockScale +
                        if (prefs.twoRowDock && dp.isTallDevice) {
                            dp.hotseatBarTopPaddingPx * prefs.dockScale
                        } else 0f
            }
        }
    }
}