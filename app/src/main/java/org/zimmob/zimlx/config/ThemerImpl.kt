package org.zimmob.zimlx.config

import android.content.Context
import org.zimmob.zimlx.Utilities
import org.zimmob.zimlx.allapps.theme.AllAppsBaseTheme
import org.zimmob.zimlx.allapps.theme.AllAppsVerticalTheme
import org.zimmob.zimlx.allapps.theme.IAllAppsThemer
import org.zimmob.zimlx.popup.theme.IPopupThemer
import org.zimmob.zimlx.popup.theme.PopupBaseTheme
import org.zimmob.zimlx.popup.theme.PopupCardTheme

open class ThemerImpl : IThemer {

    var allAppsTheme: IAllAppsThemer? = null
    var popupTheme: IPopupThemer? = null

    override fun allAppsTheme(context: Context): IAllAppsThemer {
        val useVerticalLayout = Utilities.getPrefs(context).verticalDrawerLayout
        if (allAppsTheme == null ||
                (useVerticalLayout && allAppsTheme !is AllAppsVerticalTheme) ||
                (!useVerticalLayout && allAppsTheme is AllAppsVerticalTheme))
            allAppsTheme = if (useVerticalLayout) AllAppsVerticalTheme(context) else AllAppsBaseTheme(context)
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