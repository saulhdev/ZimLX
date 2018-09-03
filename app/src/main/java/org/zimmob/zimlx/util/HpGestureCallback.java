package org.zimmob.zimlx.util;

import org.zimmob.zimlx.Workspace;
import org.zimmob.zimlx.minibar.Minibar;
import org.zimmob.zimlx.settings.AppSettings;

public class HpGestureCallback implements DesktopGestureListener.DesktopGestureCallback {
    private AppSettings appSettings;

    public HpGestureCallback(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    public boolean onDrawerGesture(Workspace desktop, DesktopGestureListener.Type event) {
        int gestureIndex;
        Minibar.ActionItem gesture = null;
        switch (event) {
            case SwipeUp: {
                gestureIndex = appSettings.getGestureSwipeUp();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            }
            case SwipeDown:
                gestureIndex = appSettings.getGestureSwipeDown();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            case SwipeLeft:
            case SwipeRight: {
                break;
            }
            case Pinch: {
                gestureIndex = appSettings.getGesturePinch();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            }
            case Unpinch: {
                gestureIndex = appSettings.getGestureUnpinch();
                if (gestureIndex != 0) {
                    gesture = Minibar.getActionItem(gestureIndex - 1);
                }
                break;
            }
            case DoubleTap: {
                gestureIndex = appSettings.getGestureDoubleTap();
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
            if (appSettings.isGestureFeedback()) {
                Tool.vibrate(desktop);
            }
            Minibar.RunAction(gesture, desktop.getContext());
            return true;
        }
        return false;
    }
}
