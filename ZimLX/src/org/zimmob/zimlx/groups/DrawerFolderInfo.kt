package org.zimmob.zimlx.groups

import com.android.launcher3.FolderInfo
import com.android.launcher3.Launcher
import com.android.launcher3.allapps.AllAppsStore
import org.zimmob.zimlx.preferences.DrawerTabEditBottomSheet
import org.zimmob.zimlx.zimPrefs

class DrawerFolderInfo(private val drawerFolder: DrawerFolders.Folder) : FolderInfo() {

    private var changed = false
    lateinit var appsStore: AllAppsStore

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        changed = true
        drawerFolder.title.value = title?.toString()
    }

    override fun onIconChanged() {
        super.onIconChanged()
        drawerFolder.context.zimPrefs.withChangeCallback {
            it.reloadDrawer()
        }
    }

    fun onCloseComplete() {
        if (changed) {
            changed = false
            drawerFolder.context.zimPrefs.appGroupsManager.drawerFolders.saveToJson()
        }
    }

    fun showEdit(launcher: Launcher) {
        DrawerTabEditBottomSheet.editFolder(launcher, drawerFolder)
    }
}
