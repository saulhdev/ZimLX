package org.zimmob.zimlx.util;

import org.zimmob.zimlx.Workspace;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class DesktopGestureListener implements SimpleFingerGestures.OnFingerGestureListener {

    private final DesktopGestureCallback callback;
    private final Workspace desktop;

    public DesktopGestureListener(Workspace desktop, DesktopGestureCallback callback) {
        this.desktop = desktop;
        this.callback = callback;
    }

    @Override
    public boolean onSwipeUp(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeUp);
    }

    @Override
    public boolean onSwipeDown(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeDown);
    }

    @Override
    public boolean onSwipeLeft(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeLeft);
    }

    @Override
    public boolean onSwipeRight(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeRight);
    }

    @Override
    public boolean onPinch(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.Pinch);
    }

    @Override
    public boolean onUnpinch(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.Unpinch);
    }

    @Override
    public boolean onDoubleTap(int i) {
        return callback.onDrawerGesture(desktop, Type.DoubleTap);
    }

    public enum Type {
        SwipeUp,
        SwipeDown,
        SwipeLeft,
        SwipeRight,
        Pinch,
        Unpinch,
        DoubleTap
    }

    public interface DesktopGestureCallback {
        boolean onDrawerGesture(Workspace desktop, Type event);
    }
}

