package org.zimmob.zimlx.preferences

import android.content.Context
import android.support.v14.preference.SwitchPreference
import android.util.AttributeSet
import org.zimmob.zimlx.R
import org.zimmob.zimlx.overlay.ILauncherClient

class GoogleNowPreference(context: Context, attrs: AttributeSet) : SwitchPreference(context, attrs) {

    private val enabledState = ILauncherClient.getEnabledState(context)

    init {
        isEnabled = enabledState == ILauncherClient.ENABLED
        if (!isEnabled) {
            if (enabledState == ILauncherClient.DISABLED_NO_PROXY_APP)
                setSummary(R.string.lawnfeed_not_found)
            if (enabledState == ILauncherClient.DISABLED_CLIENT_OUTDATED)
                setSummary(R.string.lawnfeed_incompatible)
            if (enabledState == ILauncherClient.DISABLED_NO_GOOGLE_APP)
                setSummary(R.string.google_app_not_found)
        }
    }
}