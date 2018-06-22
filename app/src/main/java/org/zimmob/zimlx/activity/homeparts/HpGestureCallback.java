package org.zimmob.zimlx.activity.homeparts;

import android.content.Context;
import android.content.pm.PackageManager;

import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.widget.Minibar;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.DesktopGestureListener;
import org.zimmob.zimlx.widget.Desktop;

public class HpGestureCallback implements DesktopGestureListener.DesktopGestureCallback {
    private AppSettings _appSettings;

    HpGestureCallback(AppSettings appSettings) {
        _appSettings = appSettings;
    }

    @Override
    public boolean onDrawerGesture(Desktop desktop, DesktopGestureListener.Type event) {
        Context context = _appSettings.getContext();
        PackageManager packageManager = context.getPackageManager();
        int gestureIndex;
        Minibar.ActionItem gesture = null;
        switch (event) {
            case SwipeUp: {
                gestureIndex = _appSettings.getGestureSwipeUp();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            }
            case SwipeDown:
                gestureIndex = _appSettings.getGestureSwipeDown();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            case SwipeLeft:
            case SwipeRight: {
                break;
            }
            case Pinch: {
                gestureIndex = _appSettings.getGesturePinch();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            }
            case Unpinch: {
                gestureIndex = _appSettings.getGestureUnpinch();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            }
            case DoubleTap: {
                gestureIndex = _appSettings.getGestureDoubleTap();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
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
            Minibar.RunAction(gesture, desktop.getContext());
            return true;
        }
        return false;
    }
}

