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
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import com.android.launcher3.R
import org.zimmob.zimlx.ZimPreferences
import org.zimmob.zimlx.runOnMainThread
import org.zimmob.zimlx.settings.ui.ControlledPreference
import org.zimmob.zimlx.smartspace.ZimSmartspaceController
import org.zimmob.zimlx.zimApp
import org.zimmob.zimlx.zimPrefs

class SmartspaceEventProvidersPreference(context: Context, attrs: AttributeSet?) :
        DialogPreference(context, attrs),
        ControlledPreference by ControlledPreference.Delegate(context, attrs),
        ZimPreferences.MutableListPrefChangeListener {

    private val providersPref = context.zimPrefs.eventProviders

    init {
        updateSummary()
    }

    fun setProviders(providers: List<String>) {
        context.zimPrefs.eventProviders.setAll(providers)
        context.zimApp.smartspace.onProviderChanged()
    }

    private fun updateSummary() {
        val providerNames = providersPref.getAll()
                .map { ZimSmartspaceController.getDisplayName(context, it) }
        if (providerNames.isNotEmpty()) {
            summary = TextUtils.join(", ", providerNames)
        } else {
            setSummary(R.string.weather_provider_disabled)
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryView = holder.findViewById(android.R.id.summary) as TextView
        summaryView.maxLines = 1
        summaryView.ellipsize = TextUtils.TruncateAt.END
    }

    override fun onAttached() {
        super.onAttached()
        providersPref.addListener(this)
    }

    override fun onDetached() {
        super.onDetached()
        providersPref.removeListener(this)
    }

    override fun onListPrefChanged(key: String) {
        runOnMainThread {
            updateSummary()
        }
    }

    override fun getDialogLayoutResource() = R.layout.dialog_preference_recyclerview
}
