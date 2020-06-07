/*
 * Copyright (c) 2020 Zim Launcher
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

package org.zimmob.zimlx.smartspace

import android.os.Handler
import android.util.Log
import com.google.android.apps.nexuslauncher.smartspace.ISmartspace
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceController
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceDataContainer

class SmartspacePixelBridge(controller: ZimSmartspaceController) :
        ZimSmartspaceController.DataProvider(controller), ISmartspace, Runnable {

    private val smartspaceController = SmartspaceController.get(controller.context)
    private val handler = Handler()
    private var data: SmartspaceDataContainer? = null
    private var ds = false

    override fun startListening() {
        super.startListening()

        updateData(null, null)
        smartspaceController.da(this)
    }

    override fun stopListening() {
        super.stopListening()
        smartspaceController.da(null)
    }

    override fun onGsaChanged() {
        ds = smartspaceController.cY()
        if (data != null) {
            cr(data)
        } else {
            Log.d("SmartspacePixelBridge", "onGsaChanged but no data present")
        }
    }

    override fun cr(data: SmartspaceDataContainer?) {
        this.data = data?.also { initListeners(it) }
    }

    private fun initListeners(e: SmartspaceDataContainer) {
        val weatherData: ZimSmartspaceController.WeatherData? = if (e.isWeatherAvailable) {
            SmartspaceDataWidget.parseWeatherData(e.dO.icon, e.dO.title)
        } else {
            null
        }
        val cardData: ZimSmartspaceController.CardData? = if (e.cS()) {
            val dp = e.dP
            ZimSmartspaceController.CardData(dp.icon, dp.title, dp.cx(true), dp.cy(), dp.cx(false))
        } else {
            null
        }

        handler.removeCallbacks(this)
        if (e.cS() && e.dP.cv()) {
            val cw = e.dP.cw()
            var min = 61000L - System.currentTimeMillis() % 60000L
            if (cw > 0L) {
                min = Math.min(min, cw)
            }
            handler.postDelayed(this, min)
        }

        updateData(weatherData, cardData)
    }

    override fun run() {
        data?.let { initListeners(it) }
    }
}
