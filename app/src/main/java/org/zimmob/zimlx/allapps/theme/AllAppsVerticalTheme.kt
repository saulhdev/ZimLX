package org.zimmob.zimlx.allapps.theme

import android.content.Context
import org.zimmob.zimlx.R

open class AllAppsVerticalTheme(val context: Context) : AllAppsBaseTheme(context) {
    override val iconTextLines = 1
    override val searchTextColor = 0
    override val iconLayout = R.layout.all_apps_icon
    override fun numIconPerRow(default: Int) = default
    override fun iconHeight(default: Int) = default

}
