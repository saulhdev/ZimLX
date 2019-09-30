/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.zimmob.zimlx

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.Keep
import com.android.launcher3.Utilities
import org.zimmob.zimlx.blur.BlurWallpaperProvider
import org.zimmob.zimlx.flowerpot.Flowerpot
import org.zimmob.zimlx.smartspace.ZimSmartspaceController
import org.zimmob.zimlx.theme.ThemeManager

class ZimApp : Application() {
    val activityHandler = ActivityHandler()
    val smartspace by lazy { ZimSmartspaceController(this) }
    val recentsEnabled by lazy { checkRecentsComponent() }
    var accessibilityService: ZimAccessibilityService? = null

    override fun onCreate() {
        super.onCreate()

        ThemeManager.getInstance(this)
        BlurWallpaperProvider.getInstance(this)
    }

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)

        BlurWallpaperProvider.getInstance(this)
        Flowerpot.Manager.getInstance(this)
    }


    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll(recreateLauncher)
        } else {
            Utilities.restartLauncher(this)
        }
    }

    fun performGlobalAction(action: Int): Boolean {
        return if (accessibilityService != null) {
            accessibilityService!!.performGlobalAction(action)
        } else {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeManager.getInstance(this).updateNightMode(newConfig)
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        val activities = HashSet<Activity>()
        var foregroundActivity: Activity? = null

        fun finishAll(recreateLauncher: Boolean = true) {
            HashSet(activities).forEach { if (recreateLauncher && it is ZimLauncher) it.recreate() else it.finish() }
        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {
            foregroundActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == foregroundActivity)
                foregroundActivity = null
            activities.remove(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }



    @Keep
    fun checkRecentsComponent(): Boolean {
        if (!Utilities.ATLEAST_P) return false
        //if (!Utilities.HIDDEN_APIS_ALLOWED) return false

        val resId = resources.getIdentifier("config_recentsComponentName", "string", "android")
        if (resId == 0) return false
        val recentsComponent = ComponentName.unflattenFromString(resources.getString(resId))
                ?: return false
        return recentsComponent.packageName == packageName
        //&& recentsComponent.className == RecentsActivity::class.java.name
    }

}


val Context.zimApp get() = applicationContext as ZimApp