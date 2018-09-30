package org.zimmob.zimlx.allapps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.zimmob.zimlx.PagedViewX;

public class AllAppsPagedView extends PagedViewX<PersonalWorkSlidingTabStrip> {
    final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
    final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
    final static float TOUCH_SLOP_DAMPING_FACTOR = 4;

    public AllAppsPagedView(Context context) {
        this(context, null);
    }

    public AllAppsPagedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsPagedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mPageIndicator.setScroll(l, mMaxScrollX);
    }

    protected void determineScrollingStart(MotionEvent ev) {
        //float absDeltaX = Math.abs(ev.getX() - getDownMotionX());
        float absDeltaX = Math.abs(ev.getX());
        //float absDeltaY = Math.abs(ev.getY() - getDownMotionY());
        float absDeltaY = Math.abs(ev.getY());

        if (Float.compare(absDeltaX, 0f) == 0) return;

        float slope = absDeltaY / absDeltaX;
        float theta = (float) Math.atan(slope);

        if (absDeltaX > mTouchSlop || absDeltaY > mTouchSlop) {
            cancelCurrentPageLongPress();
        }

        if (theta > MAX_SWIPE_ANGLE) {
            return;
        } else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE) {
            theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
            float extraRatio = (float)
                    Math.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
            super.determineScrollingStart(ev, 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio);
        } else {
            super.determineScrollingStart(ev);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
