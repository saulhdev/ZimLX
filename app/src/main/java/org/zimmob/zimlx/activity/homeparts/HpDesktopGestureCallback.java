package org.zimmob.zimlx.activity.homeparts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.LauncherAction;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.DesktopGestureListener;
import org.zimmob.zimlx.widget.Desktop;

public class HpDesktopGestureCallback implements DesktopGestureListener.DesktopGestureCallback {
    private AppSettings _appSettings;

    HpDesktopGestureCallback(AppSettings appSettings) {
        _appSettings = appSettings;
    }

    @Override
    public boolean onDrawerGesture(Desktop desktop, DesktopGestureListener.Type event) {
        Context context = _appSettings.getContext();
        PackageManager packageManager = context.getPackageManager();
        int gestureId;
        LauncherAction.ActionItem gesture = null;
        switch (event) {
            case SwipeUp: {
                gestureId = _appSettings.getGestureSwipeUp();
                if (gestureId != 0) {
                    gesture = LauncherAction.getActionItem(gestureId - 1);
                    if (gestureId == 9) {
                        gesture.extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_swipe_up) + "__", "")));
                    }

                }
                break;
            }
            case SwipeDown: {
                gestureId = _appSettings.getGestureSwipeDown();
                if (gestureId != 0) {
                    gesture = LauncherAction.getActionItem(gestureId - 1);
                    if (gestureId == 9) {
                        gesture.extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_swipe_down) + "__", "")));
                    }
                }
                break;
            }
            case SwipeLeft:
            case SwipeRight: {
                break;
            }
            case Pinch: {
                gestureId = _appSettings.getGesturePinch();
                if (gestureId != 0) {
                    gesture = LauncherAction.getActionItem(gestureId - 1);

                    if (gestureId == 9) {
                        gesture.extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_pinch) + "__", "")));
                    }
                }
                break;
            }
            case Unpinch: {
                gestureId = _appSettings.getGestureUnpinch();
                if (gestureId != 0) {
                    gesture = LauncherAction.getActionItem(gestureId - 1);
                    if (gestureId == 9) {
                        gesture.extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_unpinch) + "__", "")));
                    }
                }
                break;
            }
            case DoubleTap: {
                gestureId = _appSettings.getGestureDoubleTap();
                if (gestureId != 0) {
                    gesture = LauncherAction.getActionItem(gestureId - 1);
                    if (gestureId == 9) {
                        gesture.extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_double_tap) + "__", "")));
                    }
                }
                break;
            }
            default: {
                throw new RuntimeException("Type not handled!");
            }
        }
        if (gesture != null) {
            if (_appSettings.isGestureFeedback()) {
                Tool.vibrate(desktop);
            }
            LauncherAction.RunAction(gesture, desktop.getContext());
            return true;
        }
        return false;
    }
}

