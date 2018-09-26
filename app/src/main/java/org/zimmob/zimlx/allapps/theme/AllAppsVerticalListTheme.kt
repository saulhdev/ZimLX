package org.zimmob.zimlx.allapps.theme

import android.content.Context
import org.zimmob.zimlx.R

class AllAppsVerticalListTheme(context: Context) : AllAppsBaseTheme(context) {
    val mContext = context
    override val iconLayout = R.layout.all_apps_icon_vertical
    override fun numIconPerRow(default: Int) = 1
    override fun iconHeight(default: Int) = mContext.resources.getDimensionPixelSize(R.dimen.all_apps_vertical_row_height)
}