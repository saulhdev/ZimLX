package org.zimmob.zimlx.config

import android.content.Context
import org.zimmob.zimlx.Utilities
import org.zimmob.zimlx.allapps.theme.AllAppsPagedTheme
import org.zimmob.zimlx.allapps.theme.AllAppsVerticalListTheme
import org.zimmob.zimlx.allapps.theme.AllAppsVerticalTheme
import org.zimmob.zimlx.allapps.theme.IAllAppsThemer
import org.zimmob.zimlx.popup.theme.IPopupThemer
import org.zimmob.zimlx.popup.theme.PopupBaseTheme
import org.zimmob.zimlx.popup.theme.PopupCardTheme

open class ThemerImpl : IThemer {

    var allAppsTheme: IAllAppsThemer? = null
    var popupTheme: IPopupThemer? = null

    override fun allAppsTheme(context: Context): IAllAppsThemer {
        val drawerStyle = Integer.valueOf(Utilities.getPrefs(context).drawerLayoutStyle("1"))
        if (allAppsTheme == null) {
            when (drawerStyle) {
                0 -> allAppsTheme = AllAppsPagedTheme(context)
                1 -> allAppsTheme = AllAppsVerticalTheme(context)
                2 -> allAppsTheme = AllAppsVerticalListTheme(context)
            }
        }
        return allAppsTheme!!
    }

    override fun popupTheme(context: Context): IPopupThemer {
        val useCardTheme = Utilities.getPrefs(context).popupCardTheme
        if (popupTheme == null ||
                (useCardTheme && popupTheme !is PopupCardTheme) ||
                (!useCardTheme && popupTheme !is PopupBaseTheme)) {
            popupTheme = if (useCardTheme) PopupCardTheme() else PopupBaseTheme()
        }
        return popupTheme!!
    }
}