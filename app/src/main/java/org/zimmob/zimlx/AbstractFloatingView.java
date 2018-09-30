/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zimmob.zimlx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.zimmob.zimlx.dragndrop.DragLayer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
import static org.zimmob.zimlx.compat.AccessibilityManagerCompat.isAccessibilityEnabled;
import static org.zimmob.zimlx.compat.AccessibilityManagerCompat.sendCustomAccessibilityEvent;



/**
 * Base class for a View which shows a floating UI on top of the launcher UI.
 */
public abstract class AbstractFloatingView extends LinearLayout {
    public static final int TYPE_ACTION_POPUP = 1 << 1;
    public static final int TYPE_FOLDER = 1 << 0;
    public static final int TYPE_WIDGET_RESIZE_FRAME = 1 << 3;
    public static final int TYPE_WIDGETS_BOTTOM_SHEET = 1 << 2;
    public static final int TYPE_WIDGETS_FULL_SHEET = 1 << 4;
    public static final int TYPE_ON_BOARD_POPUP = 1 << 5;
    public static final int TYPE_DISCOVERY_BOUNCE = 1 << 6;
    // Popups related to quickstep UI
    public static final int TYPE_QUICKSTEP_PREVIEW = 1 << 7;
    public static final int TYPE_TASK_MENU = 1 << 8;
    public static final int TYPE_OPTIONS_POPUP = 1 << 9;
    public static final int TYPE_ALL = TYPE_FOLDER | TYPE_ACTION_POPUP
            | TYPE_WIDGETS_BOTTOM_SHEET | TYPE_WIDGET_RESIZE_FRAME | TYPE_WIDGETS_FULL_SHEET
            | TYPE_QUICKSTEP_PREVIEW | TYPE_ON_BOARD_POPUP | TYPE_DISCOVERY_BOUNCE | TYPE_TASK_MENU
            | TYPE_OPTIONS_POPUP;
    // Type of popups which should be kept open during launcher rebind
    public static final int TYPE_REBIND_SAFE = TYPE_WIDGETS_FULL_SHEET
            | TYPE_QUICKSTEP_PREVIEW | TYPE_ON_BOARD_POPUP | TYPE_DISCOVERY_BOUNCE;
    // Usually we show the back button when a floating view is open. Instead, hide for these types.
    public static final int TYPE_HIDE_BACK_BUTTON = TYPE_ON_BOARD_POPUP | TYPE_DISCOVERY_BOUNCE;
    public static final int TYPE_ACCESSIBLE = TYPE_ALL & ~TYPE_DISCOVERY_BOUNCE;

    public static AbstractFloatingView getTopOpenView(Launcher launcher) {
        return getOpenView(launcher, TYPE_FOLDER | TYPE_WIDGETS_BOTTOM_SHEET);
    }

    protected boolean mIsOpen;

    public AbstractFloatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractFloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void announceAccessibilityChanges() {
        Pair<View, String> targetInfo = getAccessibilityTarget();
        if (targetInfo == null || !isAccessibilityEnabled(getContext())) {
            return;
        }
        sendCustomAccessibilityEvent(
                targetInfo.first, TYPE_WINDOW_STATE_CHANGED, targetInfo.second);

        if (mIsOpen) {
            sendAccessibilityEvent(TYPE_VIEW_FOCUSED);
        }
        //BaseDraggingActivity.fromContext(getContext()).getDragLayer()
        //        .sendAccessibilityEvent(TYPE_WINDOW_CONTENT_CHANGED);
    }

    protected Pair<View, String> getAccessibilityTarget() {
        return null;
    }

    protected static <T extends AbstractFloatingView> T getOpenView(
            Launcher launcher, @FloatingViewType int type) {
        DragLayer dragLayer = launcher.getDragLayer();
        // Iterate in reverse order. AbstractFloatingView is added later to the dragLayer,
        // and will be one of the last views.
        for (int i = dragLayer.getChildCount() - 1; i >= 0; i--) {
            View child = dragLayer.getChildAt(i);
            if (child instanceof AbstractFloatingView) {
                AbstractFloatingView view = (AbstractFloatingView) child;
                if (view.isOfType(type) && view.isOpen()) {
                    return (T) view;
                }
            }
        }
        return null;
    }

    public static void closeOpenContainer(Launcher launcher, @FloatingViewType int type) {
        AbstractFloatingView view = getOpenView(launcher, type);
        if (view != null) {
            view.close(true);
        }
    }

    public static void closeAllOpenViews(Launcher launcher, boolean animate) {
        DragLayer dragLayer = launcher.getDragLayer();
        // Iterate in reverse order. AbstractFloatingView is added later to the dragLayer,
        // and will be one of the last views.
        for (int i = dragLayer.getChildCount() - 1; i >= 0; i--) {
            View child = dragLayer.getChildAt(i);
            if (child instanceof AbstractFloatingView) {
                ((AbstractFloatingView) child).close(animate);
            }
        }
    }

    public static void closeAllOpenViews(Launcher launcher) {
        closeAllOpenViews(launcher, true);
    }

    public abstract void logActionCommand(int command);

    /**
     * We need to handle touch events to prevent them from falling through to the workspace below.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return true;
    }

    public void close(boolean animate) {
        animate &= !Utilities.isPowerSaverOn(getContext());
        handleClose(animate);
    }

    protected abstract void handleClose(boolean animate);

    /**
     * If the view is current handling keyboard, return the active target, null otherwise
     */
    public ExtendedEditText getActiveTextView() {
        return null;
    }

    /**
     * Any additional view (outside of this container) where touch should be allowed while this
     * view is visible.
     */
    public View getExtendedTouchView() {
        return null;
    }

    @IntDef(flag = true, value = {
            TYPE_FOLDER,
            TYPE_ACTION_POPUP,
            TYPE_WIDGETS_BOTTOM_SHEET,
            TYPE_WIDGET_RESIZE_FRAME,
            TYPE_WIDGETS_FULL_SHEET,
            TYPE_ON_BOARD_POPUP,
            TYPE_DISCOVERY_BOUNCE,

            TYPE_QUICKSTEP_PREVIEW,
            TYPE_TASK_MENU,
            TYPE_OPTIONS_POPUP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FloatingViewType {
    }

    public final boolean isOpen() {
        return mIsOpen;
    }

    protected void onWidgetsBound() {
    }

    protected abstract boolean isOfType(@FloatingViewType int type);
}