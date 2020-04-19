package org.zimmob.zimlx.globalsearch.providers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import org.zimmob.zimlx.globalsearch.SearchProvider

@Keep
class AppSearchSearchProvider(context: Context) : SearchProvider(context) {

    override val name = context.getString(R.string.search_provider_appsearch)
    override val supportsVoiceSearch = false
    override val supportsAssistant = false
    override val supportsFeed = false

    override fun startSearch(callback: (intent: Intent) -> Unit) {
        val launcher = LauncherAppState.getInstanceNoCreate().launcher
        launcher.stateManager.goToState(LauncherState.ALL_APPS, true) {
            launcher.appsView.searchUiManager.startSearch()
        }
    }

    override fun getIcon(): Drawable = context.getDrawable(R.drawable.ic_search)!!.mutate()

}