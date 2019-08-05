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

package org.zimmob.zimlx.smartspace

import android.service.notification.StatusBarNotification
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import com.android.launcher3.R
import com.android.launcher3.notification.NotificationInfo
import com.android.launcher3.util.PackageUserKey
import org.zimmob.zimlx.flowerpot.Flowerpot
import org.zimmob.zimlx.flowerpot.FlowerpotApps
import org.zimmob.zimlx.loadSmallIcon
import org.zimmob.zimlx.runOnUiWorkerThread
import org.zimmob.zimlx.smartspace.ZimSmartspaceController.*
import org.zimmob.zimlx.toBitmap

@Keep
class NotificationUnreadProvider(controller: ZimSmartspaceController) :
        NotificationBasedDataProvider(controller),
        NotificationsManager.OnChangeListener {

    private val manager = NotificationsManager.instance
    private var flowerpotLoaded = false
    private var flowerpotApps: FlowerpotApps? = null
    private val tmpKey = PackageUserKey(null, null)
    private var zenModeEnabled = false
        set(value) {
            if (field != value) {
                field = value
                onNotificationsChanged()
            }
        }
    private val zenModeListener = ZenModeListener(controller.context.contentResolver) {
        zenModeEnabled = it
    }

    override fun waitForSetup() {
        super.waitForSetup()

        manager.addListener(this)
        zenModeListener.startListening()
        runOnUiWorkerThread {
            flowerpotApps = Flowerpot.Manager.getInstance(controller.context)
                    .getPot("COMMUNICATION", true)?.apps
            flowerpotLoaded = true
            onNotificationsChanged()
        }
    }

    override fun onNotificationsChanged() {
        updateData(null, getEventCard())
    }

    private fun isCommunicationApp(sbn: StatusBarNotification): Boolean {
        return tmpKey.updateFromNotification(sbn)
                && flowerpotApps?.packageMatches?.contains(tmpKey) != false
    }

    private fun getEventCard(): CardData? {
        if (!flowerpotLoaded) return null

        val sbn = manager.notifications
                .asSequence()
                .filter { !it.isOngoing }
                .filter { it.notification.priority >= PRIORITY_DEFAULT }
                .filter { isCommunicationApp(it) }
                .maxWith(compareBy(
                        { it.notification.priority },
                        { it.notification.`when` })) ?: return null

        if (zenModeEnabled) {
            return CardData(
                    context.getDrawable(R.drawable.ic_zen_mode)!!.toBitmap(),
                    listOf(Line(context.getString(R.string.zen_mode_enabled))))
        }

        val context = controller.context
        val notif = NotificationInfo(context, sbn)
        val app = getApp(sbn).toString()
        val title = notif.title?.toString() ?: ""
        val splitted = splitTitle(title)
        val body = notif.text?.toString()?.trim()?.split("\n")?.firstOrNull() ?: ""

        val lines = mutableListOf<Line>()
        if (!TextUtils.isEmpty(body)) {
            lines.add(Line(body))
        }
        lines.addAll(splitted.reversed().map { Line(it) })

        val appLine = Line(app)
        if (!lines.contains(appLine)) {
            lines.add(appLine)
        }
        return CardData(
                sbn.loadSmallIcon(context)?.toBitmap(), lines,
                NotificationClickListener(sbn))
    }

    private fun splitTitle(title: String): Array<String> {
        val delimiters = arrayOf(": ", " - ", " • ")
        for (del in delimiters) {
            if (title.contains(del)) {
                return title.split(del.toRegex(), 2).toTypedArray()
            }
        }
        return arrayOf(title)
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.removeListener(this)
        zenModeListener.stopListening()
    }
}
