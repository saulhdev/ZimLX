package org.zimmob.zimlx

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.AppFilter

open class ZimAppFilter(context: Context) : AppFilter() {

    private val hideList = HashSet<ComponentName>()

    init {
        //hideList.add(ComponentName(context, ))
    }

    override fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return !hideList.contains(componentName) && super.shouldShowApp(componentName, user)
    }
}