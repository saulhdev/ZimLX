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

package org.zimmob.zimlx.gestures

import android.annotation.TargetApi
import android.os.Build
import android.view.Choreographer
import android.view.MotionEvent
import com.android.quickstep.MotionEventQueue
import com.android.quickstep.TouchConsumer

@TargetApi(Build.VERSION_CODES.P)
open class PassThroughTouchConsumer(protected val target: TouchConsumer) : TouchConsumer {

    protected open var passThroughEnabled = true

    override fun accept(ev: MotionEvent) {
        if (passThroughEnabled || ev.actionMasked != MotionEvent.ACTION_MOVE) target.accept(ev)
    }

    override fun reset() {
        if (passThroughEnabled) target.reset()
    }

    override fun updateTouchTracking(interactionType: Int) {
        if (passThroughEnabled) target.updateTouchTracking(interactionType)
    }

    override fun onQuickScrubEnd() {
        if (passThroughEnabled) target.onQuickScrubEnd()
    }

    override fun onQuickScrubProgress(progress: Float) {
        if (passThroughEnabled) target.onQuickScrubProgress(progress)
    }

    override fun onQuickStep(ev: MotionEvent?) {
        if (passThroughEnabled) target.onQuickStep(ev)
    }

    override fun onCommand(command: Int) {
        if (passThroughEnabled) target.onCommand(command)
    }

    override fun preProcessMotionEvent(ev: MotionEvent?) {
        if (passThroughEnabled) target.preProcessMotionEvent(ev)
    }

    override fun getIntrimChoreographer(queue: MotionEventQueue?): Choreographer? {
        return if (passThroughEnabled) target.getIntrimChoreographer(queue) else null
    }

    override fun deferInit() {
        if (passThroughEnabled) target.deferInit()
    }

    override fun deferNextEventToMainThread(): Boolean {
        return if (passThroughEnabled) target.deferNextEventToMainThread() else false
    }

    override fun forceToLauncherConsumer(): Boolean {
        return if (passThroughEnabled) target.forceToLauncherConsumer() else false
    }

    override fun onShowOverviewFromAltTab() {
        target.onShowOverviewFromAltTab()
    }
}
