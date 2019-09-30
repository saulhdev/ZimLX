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

package org.zimmob.zimlx.theme.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.preference.SwitchPreference
import com.android.launcher3.R
import org.zimmob.zimlx.ZimPreferences
import org.zimmob.zimlx.util.hasFlag
import org.zimmob.zimlx.util.setFlag
import org.zimmob.zimlx.zimPrefs

@Keep
open class ThemeFlagSwitchPreference(context: Context, attrs: AttributeSet) : SwitchPreference(context, attrs),
        ZimPreferences.OnPreferenceChangeListener {

    protected val prefs = context.zimPrefs
    private var switchFlag = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ThemeFlagSwitchPreference)
        switchFlag = a.getInteger(R.styleable.ThemeFlagSwitchPreference_switchFlag, 0)
        a.recycle()
    }

    override fun onAttached() {
        super.onAttached()

        prefs.addOnPreferenceChangeListener("pref_launcherTheme", this)
    }

    override fun onDetached() {
        super.onDetached()

        prefs.removeOnPreferenceChangeListener("pref_launcherTheme", this)
    }

    override fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean) {
        isChecked = prefs.launcherTheme.hasFlag(switchFlag)
    }

    override fun persistBoolean(value: Boolean): Boolean {
        if (prefs.launcherTheme.hasFlag(switchFlag) != value) {
            prefs.launcherTheme = prefs.launcherTheme.setFlag(switchFlag, value)
        }
        return true
    }
}
