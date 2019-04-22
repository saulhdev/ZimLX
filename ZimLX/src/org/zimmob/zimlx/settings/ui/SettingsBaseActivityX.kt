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

package org.zimmob.zimlx.settings.ui

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import org.zimmob.zimlx.ZimLayoutInflater
import org.zimmob.zimlx.getBooleanAttr
import org.zimmob.zimlx.launcherAppState
import org.zimmob.zimlx.theme.ThemeManager
import org.zimmob.zimlx.theme.ThemeOverride

@SuppressLint("Registered")
open class SettingsBaseActivityX : AppCompatActivity(), ThemeManager.ThemeableActivity {
    val dragLayer by lazy { SettingsDragLayer(this, null) }
    val decorLayout by lazy { DecorLayout(this, window) }

    protected open val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private lateinit var themeOverride: ThemeOverride
    private var currentTheme = 0
    private var paused = false
    private val customLayoutInflater by lazy {
        ZimLayoutInflater(super.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (layoutInflater as ZimLayoutInflater).installFactory(delegate)
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentTheme = themeOverride.getTheme(this)

        super.onCreate(savedInstanceState ?: intent.getBundleExtra("state"))
        dragLayer.addView(decorLayout, InsettableFrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        super.setContentView(dragLayer)

        val prefs = Utilities.getZimPrefs(this)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(prefs.primaryColor)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_white_24px)
        setSupportActionBar(toolbar)

        var flags = window.decorView.systemUiVisibility
        if (Utilities.ATLEAST_MARSHMALLOW) {
            val useLightBars = getBooleanAttr(R.attr.useLightSystemBars)
            flags = Utilities.setFlag(flags, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, useLightBars)
            if (Utilities.ATLEAST_OREO) {
                flags = Utilities.setFlag(flags, View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR, useLightBars)
            }
        }
        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = dark(Utilities.getZimPrefs(applicationContext).primaryColor)
        }
    }

    fun dark(color: Int): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, Math.max((r * 0.8).toInt(), 0), Math.max((g * 0.8).toInt(), 0), Math.max((b * 0.8).toInt(), 0))
    }

    override fun onBackPressed() {
        dragLayer.getTopOpenView()?.let {
            it.close(true)
            return
        }
        super.onBackPressed()
    }

    override fun setContentView(v: View) {
        val contentParent = getContentFrame()
        contentParent.removeAllViews()
        contentParent.addView(v)
    }

    override fun setContentView(resId: Int) {
        val contentParent = getContentFrame()
        contentParent.removeAllViews()
        LayoutInflater.from(this).inflate(resId, contentParent)
    }

    override fun setContentView(v: View, lp: ViewGroup.LayoutParams) {
        val contentParent = getContentFrame()
        contentParent.removeAllViews()
        contentParent.addView(v, lp)
    }

    fun getContentFrame(): ViewGroup {
        return decorLayout.findViewById(android.R.id.content)
    }

    /*override fun onColorChange(resolver: String, color: Int, foregroundColor: Int) {
        when (resolver) {
            ColorEngine.Resolvers.ACCENT -> {
                val arrowBack = resources.getDrawable(R.drawable.ic_arrow_back, null)
                arrowBack?.setTint(color)
                supportActionBar?.setHomeAsUpIndicator(arrowBack)
            }
        }
    }*/

    override fun onResume() {
        super.onResume()
        paused = false
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    protected open fun createRelaunchIntent(): Intent {
        val state = Bundle()
        onSaveInstanceState(state)
        return intent.putExtra("state", state)
    }

    protected fun getRelaunchInstanceState(savedInstanceState: Bundle?): Bundle? {
        return savedInstanceState ?: intent.getBundleExtra("state")
    }

    override fun onThemeChanged() {
        if (currentTheme == themeOverride.getTheme(this)) return
        if (paused) {
            recreate()
        } else {
            finish()
            startActivity(createRelaunchIntent(), ActivityOptions.makeCustomAnimation(
                    this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        }
    }

    override fun getSystemService(name: String): Any? {
        if (name == Context.LAYOUT_INFLATER_SERVICE) {
            return customLayoutInflater
        }
        return super.getSystemService(name)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        launcherAppState.launcher?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {

        fun getActivity(context: Context): SettingsBaseActivityX {
            return context as? SettingsBaseActivityX
                    ?: (context as ContextWrapper).baseContext as SettingsBaseActivityX
        }
    }
}
