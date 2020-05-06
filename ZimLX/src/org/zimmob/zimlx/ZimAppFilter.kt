package org.zimmob.zimlx

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.AppFilter

open class ZimAppFilter(context: Context) : AppFilter() {

    private val hideList = HashSet<ComponentName>()

    init {
        hideList.add(ComponentName(context, ZimLauncher::class.java.name))
        //Voice Search
        hideList.add(ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/.VoiceSearchActivity")!!)

        //Wallpapers
        hideList.add(ComponentName.unflattenFromString("com.google.android.apps.wallpaper/.picker.CategoryPickerActivity")!!)

        //Google Now Launcher
        hideList.add(ComponentName.unflattenFromString("com.google.android.launcher/.StubApp")!!)

        //Actions Services
        hideList.add(ComponentName.unflattenFromString("com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity")!!)
    }

    override fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return !hideList.contains(componentName) && super.shouldShowApp(componentName, user)
    }
}