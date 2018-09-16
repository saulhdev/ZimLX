package org.zimmob.zimlx.gestures

import org.zimmob.zimlx.Launcher

abstract class GestureHandler(val launcher: Launcher) {

    abstract fun onGestureTrigger()
}