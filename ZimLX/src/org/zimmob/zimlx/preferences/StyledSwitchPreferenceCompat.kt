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

package org.zimmob.zimlx.preferences


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import androidx.preference.AndroidResources
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import org.zimmob.zimlx.ZimPreferences
import org.zimmob.zimlx.applyColor
import org.zimmob.zimlx.settings.ui.ControlledPreference
import org.zimmob.zimlx.settings.ui.SearchIndex
import org.zimmob.zimlx.zimPrefs

open class StyledSwitchPreferenceCompat @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        SwitchPreference(context, attrs),
        ControlledPreference by ControlledPreference.Delegate(context, attrs), SearchIndex.Slice {

    protected var checkableView: View? = null
        private set

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        checkableView = holder?.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET)
        //ColorEngine.getInstance(context).addColorChangeListeners(this, ColorEngine.Resolvers.ACCENT)
    }

    override fun getSlice(context: Context, key: String): View {
        val prefs = context.zimPrefs
        val color = prefs.accentColor
        var pref by prefs.BooleanPref(key)
        return Switch(context).apply {
            applyColor(color)
            prefs.addOnPreferenceChangeListener(key, object : ZimPreferences.OnPreferenceChangeListener {
                override fun onValueChanged(key: String, prefs: ZimPreferences, force: Boolean) {
                    isChecked = pref
                }
            })
            setOnCheckedChangeListener { _, isChecked ->
                pref = isChecked
            }
        }
    }
}
