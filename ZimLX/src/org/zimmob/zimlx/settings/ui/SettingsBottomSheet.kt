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

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Property
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import com.android.launcher3.Insettable
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.anim.Interpolators
import com.android.launcher3.anim.Interpolators.scrollInterpolatorForVelocity
import com.android.launcher3.graphics.ColorScrim
import com.android.launcher3.touch.SwipeDetector
import com.android.launcher3.util.TouchController
import org.zimmob.zimlx.getColorAttr

/**
 * Base class for custom popups
 */
class SettingsBottomSheet(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
        Insettable, TouchController, SwipeDetector.Listener {

    private val activity = SettingsBaseActivity.getActivity(context)

    private val mInsets: Rect

    private val colorScrim = createScrim()
    private var translationShift = TRANSLATION_SHIFT_CLOSED
        set(value) {
            field = value
            colorScrim.setProgress(1 - this.translationShift)
            content.translationY = value * content.height
        }

    private var isOpen = false
    private var scrollInterpolator = Interpolators.SCROLL_CUBIC
    private val mSwipeDetector = SwipeDetector(context, this, SwipeDetector.VERTICAL)
    private val content = this

    init {
        setWillNotDraw(false)
        mInsets = Rect()
    }

    private fun createScrim(): ColorScrim {
        val color = ColorUtils.setAlphaComponent(context.getColorAttr(R.attr.bottomSheetScrimColor), 153)
        return ColorScrim(this, color, Interpolators.LINEAR).apply { attach() }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        translationShift = translationShift
    }

    fun show(view: View, animate: Boolean) {
        (findViewById<View>(R.id.sheet_contents) as ViewGroup).addView(view)

        activity.dragLayer.addView(this)
        isOpen = false
        animateOpen(animate)
    }

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        val directionsToDetectScroll = if (mSwipeDetector.isIdleState)
            SwipeDetector.DIRECTION_NEGATIVE
        else
            0
        mSwipeDetector.setDetectableScrollConditions(
                directionsToDetectScroll, false)
        mSwipeDetector.onTouchEvent(ev)
        return mSwipeDetector.isDraggingOrSettling || !activity.dragLayer.isEventOverView(content, ev)
    }

    override fun onControllerTouchEvent(ev: MotionEvent): Boolean {
        mSwipeDetector.onTouchEvent(ev)
        if (ev.action == MotionEvent.ACTION_UP && mSwipeDetector.isIdleState) {
            // If we got ACTION_UP without ever starting swipe, close the panel.
            val isOpening = isOpen
            if (!activity.dragLayer.isEventOverView(content, ev)) {
                close(true)
            }
        }
        return true
    }

    private fun animateOpen(animate: Boolean) {
        if (isOpen) {
            return
        }
        isOpen = true
    }

    override fun onDragStart(start: Boolean) {}

    fun onDrag(displacement: Float, velocity: Float): Boolean {
        val range = content.height.toFloat()
        val bounded = Utilities.boundToRange(displacement, 0f, range)
        translationShift = bounded / range
        return true
    }

    override fun onDragEnd(velocity: Float, fling: Boolean) {
        if (fling && velocity > 0 || translationShift > 0.5f) {
            scrollInterpolator = scrollInterpolatorForVelocity(velocity)
            close(true)
        }
    }

    override fun onDrag(displacement: Float): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun close(animate: Boolean) {
        handleClose(animate and !Utilities.isPowerSaverPreventingAnimation(context))
        isOpen = false
    }

    fun handleClose(animate: Boolean) {
        handleClose(animate, DEFAULT_CLOSE_DURATION.toLong())
    }

    private fun handleClose(animate: Boolean, defaultDuration: Long) {
        if (isOpen && !animate) {
            translationShift = TRANSLATION_SHIFT_CLOSED
            onCloseComplete()
            return
        }
        if (!isOpen) {
            return
        }
    }

    private fun onCloseComplete() {
        isOpen = false
        activity.dragLayer.removeView(this)
    }

    override fun setInsets(insets: Rect) {
        // Extend behind left, right, and bottom insets.
        val leftInset = insets.left - mInsets.left
        val rightInset = insets.right - mInsets.right
        var bottomInset = insets.bottom - mInsets.bottom
        mInsets.set(insets)

        if (!Utilities.ATLEAST_OREO) {
            val navBarBg = findViewById<View>(R.id.nav_bar_bg)
            val navBarBgLp = navBarBg.layoutParams
            navBarBgLp.height = bottomInset
            navBarBg.layoutParams = navBarBgLp
            bottomInset = 0
        }

        setPadding(paddingLeft + leftInset, paddingTop,
                paddingRight + rightInset, paddingBottom + bottomInset)
    }

    companion object {

        private var TRANSLATION_SHIFT: Property<SettingsBottomSheet, Float> =
                object : Property<SettingsBottomSheet, Float>(Float::class.java, "translationShift") {

                    override fun get(view: SettingsBottomSheet): Float {
                        return view.translationShift
                    }

                    override fun set(view: SettingsBottomSheet, value: Float) {
                        view.translationShift = value
                    }
                }
        private const val TRANSLATION_SHIFT_CLOSED = 1f
        private const val TRANSLATION_SHIFT_OPENED = 0f

        private const val DEFAULT_CLOSE_DURATION = 200

        fun inflate(context: Context): SettingsBottomSheet {
            val activity = SettingsBaseActivity.getActivity(context)
            return LayoutInflater.from(context)
                    .inflate(R.layout.settings_bottom_sheet, activity.dragLayer, false) as SettingsBottomSheet
        }
    }

}
