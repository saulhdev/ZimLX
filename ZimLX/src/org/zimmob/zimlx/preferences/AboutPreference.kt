/*
 * 2020 Zim Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.zimmob.zimlx.preferences

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.R


class AboutPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    init {
        layoutResource = R.layout.pref_about_with_icon
        updateInfo()
    }

    private fun updateInfo() {
        try {
            title = context.getString(R.string.derived_app_name)
            summary = getSummaryInfo()
            icon = context.getDrawable(R.drawable.ic_launcher)
        } catch (ignored: IllegalStateException) {

        }
    }

    fun getSummaryInfo(): String? {
        var summary: String? = context.packageName
        summary = summary + "\nVersion: "
        return summary
    }

    fun getPackageName(strResKey: String?): String? {
        return try {
            val resId: Int = context.resources.getIdentifier(strResKey, "String", context.packageName)

            context.getString(resId)
        } catch (e: NotFoundException) {
            null
        }
    }

}