package org.zimmob.zimlx.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;

import org.zimmob.zimlx.config.Config;

//Important!! ReadMe
//We are now using the old method to detect widget long press, this fixed all the "randomly disappearing" behaviour of widgets
//However, you will need to move a bit to trigger the long press, when dragging. But this can be useful, as we can implement a
//popup menu of the widget when it was being pressed.
public class WidgetView extends AppWidgetHostView {
    private OnTouchListener onTouchListener;
    private OnLongClickListener longClick;
    private long down;
    private float mSlop;


    public WidgetView(Context context) {
        super(context);
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this.longClick = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (onTouchListener != null)
            onTouchListener.onTouch(this, ev);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                down = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean upVal = System.currentTimeMillis() - down > 300L;
                if (upVal) {
                    longClick.onLongClick(WidgetView.this);
                }
                break;
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        // If the widget does not handle touch, then cancel
        // long press when we release the touch
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Config.pointInView(this, ev.getX(), ev.getY(), mSlop)) {
                    //mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (final RuntimeException e) {
            post(() -> {
                //switchToErrorView();
            });
        }

        //mIsScrollable = checkScrollableRecursively(this);
    }

}