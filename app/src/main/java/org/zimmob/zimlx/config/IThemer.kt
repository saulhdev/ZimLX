package org.zimmob.zimlx.config

import android.content.Context
import org.zimmob.zimlx.allapps.theme.IAllAppsThemer
import org.zimmob.zimlx.popup.theme.IPopupThemer

interface IThemer {

    fun allAppsTheme(context: Context): IAllAppsThemer
    fun popupTheme(context: Context): IPopupThemer
}