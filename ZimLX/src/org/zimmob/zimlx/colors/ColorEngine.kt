/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.zimmob.zimlx.colors

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color.alpha
import android.text.TextUtils
import com.android.launcher3.Utilities
import com.google.android.apps.nexuslauncher.utils.ThemedContextProvider
import org.zimmob.zimlx.*
import org.zimmob.zimlx.colors.resolvers.DockQsbAutoResolver
import org.zimmob.zimlx.colors.resolvers.DrawerQsbAutoResolver
import org.zimmob.zimlx.colors.resolvers.WorkspaceLabelAutoResolver
import org.zimmob.zimlx.theme.ThemeOverride
import org.zimmob.zimlx.util.Config
import org.zimmob.zimlx.util.SingletonHolder
import java.lang.reflect.Constructor

class ColorEngine private constructor(val context: Context) : ZimPreferences.OnPreferenceChangeListener {

    private val prefs by lazy { Utilities.getZimPrefs(context) }
    private val colorListeners = mutableMapOf<String, MutableSet<OnColorChangeListener>>()

    private val resolverCache = mutableMapOf<String, ResolverCache>()
    private val constructorCache = mutableMapOf<String, Constructor<*>>()

    private var _accentResolver = getResolverCache(Resolvers.ACCENT)
    val accentResolver get() = _accentResolver.value
    val accent get() = accentResolver.resolveColor()

    override fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean) {
        if (!force) {
            onColorChanged(key, getResolver(key))
        }
    }

    private fun onColorChanged(key: String, colorResolver: ColorResolver) {
        runOnMainThread { colorListeners[key]?.forEach { it.onColorChange(ResolveInfo(key, colorResolver)) } }
    }

    fun addColorChangeListeners(listener: OnColorChangeListener, vararg keys: String) {
        if (keys.isEmpty()) {
            throw RuntimeException("At least one key is required")
        }
        for (key in keys) {
            if (colorListeners[key] == null) {
                colorListeners[key] = createWeakSet()
                prefs.addOnPreferenceChangeListener(this, key)
            }
            colorListeners[key]?.add(listener)
            listener.onColorChange(ResolveInfo(key, getResolver(key)))
        }
    }

    fun removeColorChangeListeners(listener: OnColorChangeListener, vararg keys: String) {
        val keyz = if (keys.isEmpty()) colorListeners.keys.toTypedArray() else keys
        for (key in keyz) {
            colorListeners[key]?.remove(listener)
            if (colorListeners[key]?.isEmpty() == true) {
                colorListeners.remove(key)
                prefs.removeOnPreferenceChangeListener(key, this)
            }
        }
    }

    fun createColorResolver(key: String, string: String): ColorResolver {
        return (createColorResolverNullable(key, string) ?: Resolvers.getDefaultResolver(key, this))
    }

    fun createColorResolverNullable(key: String, string: String): ColorResolver? {
        var resolver: ColorResolver? = null
        try {
            val parts = string.split("|")
            val className = parts[0]
            val args = if (parts.size > 1) parts.subList(1, parts.size) else emptyList()

            val constructor = constructorCache.getOrPut(className) {
                Class.forName(className).getConstructor(ColorResolver.Config::class.java)
            }
            resolver = constructor.newInstance(ColorResolver.Config(key, this, ::onColorChanged, args)) as ColorResolver
        } catch (e: IllegalStateException) {
        } catch (e: ClassNotFoundException) {
        } catch (e: InstantiationException) {
        }
        return resolver
    }

    fun getResolverCache(key: String): ResolverCache {
        return resolverCache.getOrPut(key) {
            ResolverCache(this, key)
        }
    }

    fun getResolver(key: String): ColorResolver {
        return getResolverCache(key).value
    }

    companion object : SingletonHolder<ColorEngine, Context>(ensureOnMainThread(useApplicationContext(::ColorEngine))) {
        @JvmStatic
        override fun getInstance(arg: Context) = super.getInstance(arg)

        fun setColor(editor: SharedPreferences.Editor, resolver: String, color: Int) {
            editor.putString(
                    resolver, (if (alpha(color) < 0xFF) {
                // ARGB
                arrayOf(
                        ARGBColorResolver::class.java.name,
                        color.alpha.toString(),
                        color.red.toString(),
                        color.green.toString(),
                        color.blue.toString()
                )
            } else {
                // RGB
                arrayOf(
                        RGBColorResolver::class.java.name,
                        color.red.toString(),
                        color.green.toString(),
                        color.blue.toString()
                )
            }).joinToString("|")
            )
        }
    }

    interface OnColorChangeListener {
        fun onColorChange(resolver: ResolveInfo)
    }

    internal class Resolvers {
        companion object {
            const val ACCENT = "pref_key__accent_color"
            const val HOTSEAT_QSB_BG = "pref_hotseatQsbColorResolver"
            const val ALLAPPS_QSB_BG = "pref_allappsQsbColorResolver"
            const val WORKSPACE_ICON_LABEL = "pref_workspaceLabelColorResolver"

            fun getDefaultResolver(key: String, engine: ColorEngine): ColorResolver {
                val context = engine.context
                return when (key) {
                    HOTSEAT_QSB_BG -> {
                        DockQsbAutoResolver(createConfig(key, engine))
                    }
                    ALLAPPS_QSB_BG -> {
                        DrawerQsbAutoResolver(createConfig(key, engine))
                    }
                    /*ALLAPPS_ICON_LABEL -> {
                        DrawerLabelAutoResolver(createConfig(key, engine))
                    }
                    */
                    WORKSPACE_ICON_LABEL -> {
                        WorkspaceLabelAutoResolver(createConfig(key, engine))
                    }
                    /*
                    HOTSEAT_ICON_LABEL -> {
                        WorkspaceLabelAutoResolver(createConfig(key, engine))
                    }
                    DOCK_BACKGROUND, ALLAPPS_BACKGROUND -> {
                        ShelfBackgroundAutoResolver(createConfig(key, engine))
                    }
                    SUPERG_BACKGROUND -> {
                        SuperGAutoResolver(createConfig(key, engine))
                    }*/
                    else -> {
                        engine.createColorResolverNullable(key,
                                Config.getInstance(context).defaultColorResolver)
                                ?: PixelAccentResolver(createConfig(key, engine))
                    }
                }
            }

            private fun createConfig(key: String, engine: ColorEngine) = ColorResolver.Config(key, engine, engine::onColorChanged)
        }
    }

    class ResolverCache(private val engine: ColorEngine, key: String)
        : ZimPreferences.OnPreferenceChangeListener {

        private var currentValue: ColorResolver? = null
            set(value) {
                if (field != value) {
                    field?.stopListening()
                    field = value
                    field?.startListening()
                }
            }

        val value get() = currentValue!!

        private var prefValue by engine.context.zimPrefs.StringPref(key, "")

        init {
            engine.context.zimPrefs.addOnPreferenceChangeListener(key, this)
        }

        override fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean) {
            currentValue = engine.createColorResolver(key, prefValue)
            if (!force) {
                engine.onColorChanged(key, currentValue!!)
            }
        }

        fun set(resolver: ColorResolver) {
            prefValue = resolver.toString()
        }
    }

    abstract class ColorResolver(val config: Config) : ThemedContextProvider.Listener {

        private var listening = false
        val engine get() = config.engine
        val args get() = config.args
        open val isCustom = false
        open val themeAware = false
        open val themeSet: ThemeOverride.ThemeSet = ThemeOverride.Launcher()

        val context get() = engine.context

        private val themedContextProvider by lazy { ThemedContextProvider(context, this, themeSet) }
        val themedContext get() = themedContextProvider.get()


        abstract fun resolveColor(): Int

        abstract fun getDisplayName(): String

        override fun toString() = TextUtils.join("|", listOf(this::class.java.name) + args) as String

        fun computeForegroundColor() = resolveColor().foregroundColor

        open fun startListening() {
            listening = true
        }

        open fun stopListening() {
            listening = false
        }

        fun notifyChanged() {
            config.listener?.invoke(config.key, this)
        }

        fun onDestroy() {
            if (listening) {
                stopListening()
            }
        }

        override fun onThemeChanged() {
            notifyChanged()
        }


        class Config(
                val key: String,
                val engine: ColorEngine,
                val listener: ((String, ColorResolver) -> Unit)? = null,
                val args: List<String> = emptyList())
    }

    class ResolveInfo(val key: String, resolver: ColorResolver) {

        val color = resolver.resolveColor()
        val foregroundColor by lazy { color.foregroundColor }
        val luminance = color.luminance
        val isDark = luminance < 0.5f

        val resolverClass = resolver::class.java
    }
}
