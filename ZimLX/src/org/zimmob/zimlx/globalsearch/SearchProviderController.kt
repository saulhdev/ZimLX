package org.zimmob.zimlx.globalsearch

import android.content.Context
import android.view.ContextThemeWrapper
import com.android.launcher3.Utilities
import org.zimmob.zimlx.ensureOnMainThread
import org.zimmob.zimlx.globalsearch.providers.*
import org.zimmob.zimlx.theme.ThemeManager
import org.zimmob.zimlx.theme.ThemeOverride
import org.zimmob.zimlx.useApplicationContext
import org.zimmob.zimlx.util.SingletonHolder

class SearchProviderController(private val context: Context) {

    private val prefs by lazy { Utilities.getZimPrefs(context) }
    private var cache: SearchProvider? = null
    private var cached: String = ""

    private val themeOverride = ThemeOverride(ThemeOverride.Launcher(), ThemeListener())
    private var themeRes: Int = 0

    private val listeners = HashSet<OnProviderChangeListener>()

    val isGoogle get() = searchProvider is GoogleSearchProvider

    init {
        ThemeManager.getInstance(context).addOverride(themeOverride)
    }

    fun addOnProviderChangeListener(listener: OnProviderChangeListener) {
        listeners.add(listener)
    }

    fun removeOnProviderChangeListener(listener: OnProviderChangeListener) {
        listeners.remove(listener)
    }

    fun onSearchProviderChanged() {
        cache = null
        notifyProviderChanged()
    }

    private fun notifyProviderChanged() {
        HashSet(listeners).forEach(OnProviderChangeListener::onSearchProviderChanged)
    }

    val searchProvider: SearchProvider
        get() {
            val curr = prefs.searchProvider
            if (cache == null || cached != curr) {
                cache = null
                try {
                    val constructor = Class.forName(prefs.searchProvider).getConstructor(Context::class.java)
                    val themedContext = ContextThemeWrapper(context, themeRes)
                    val prov = constructor.newInstance(themedContext) as SearchProvider
                    if (prov.isAvailable) {
                        cache = prov
                    }
                } catch (ignored: Exception) {
                }
                if (cache == null) cache = GoogleSearchProvider(context)
                cached = cache!!::class.java.name
                notifyProviderChanged()
            }
            return cache!!
        }

    inner class ThemeListener : ThemeOverride.ThemeOverrideListener {

        override val isAlive = true

        override fun applyTheme(themeRes: Int) {
            this@SearchProviderController.themeRes = themeRes
        }

        override fun reloadTheme() {
            cache = null
            applyTheme(themeOverride.getTheme(context))
            onSearchProviderChanged()
        }
    }

    interface OnProviderChangeListener {

        fun onSearchProviderChanged()
    }

    companion object : SingletonHolder<SearchProviderController, Context>(ensureOnMainThread(useApplicationContext(::SearchProviderController))) {
        fun getSearchProviders(context: Context) = listOf(
                AppSearchSearchProvider(context),
                GoogleSearchProvider(context),
                SFinderSearchProvider(context),
                DisabledDummySearchProvider(context),
                GoogleGoSearchProvider(context),
                FirefoxSearchProvider(context),
                DuckDuckGoSearchProvider(context),
                BingSearchProvider(context),
                BaiduSearchProvider(context),
                YandexSearchProvider(context),
                QwantSearchProvider(context),
                SearchLiteSearchProvider(context),
                CoolSearchSearchProvider(context)
        ).filter { it.isAvailable }
    }
}
