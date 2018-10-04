package org.zimmob.zimlx.pageindicators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.util.Tool;

public class PageIndicatorLine extends PageIndicator {
    private static final float SHIFT_PER_ANIMATION = 0.5f;
    private static final float SHIFT_THRESHOLD = 0.1f;
    private static final long ANIMATION_DURATION = 150;
    private static final Property<PageIndicatorLine, Float> CURRENT_POSITION
            = new Property<PageIndicatorLine, Float>(float.class, "current_position") {
        @Override
        public Float get(PageIndicatorLine obj) {
            return obj.mCurrentPosition;
        }

        @Override
        public void set(PageIndicatorLine obj, Float pos) {
            obj.mCurrentPosition = pos;
            obj.invalidate();
            obj.invalidateOutline();
        }
    };
    private final boolean mIsRtl;
    private final int mActiveColor;
    private final int mInActiveColor;
    private float mCurrentPosition;
    private float mFinalPosition;
    private ObjectAnimator mAnimator;
    private final AnimatorListenerAdapter mAnimCycleListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimator = null;
            animateToPosition(mFinalPosition);
        }
    };
    private int mActivePage;
    private Paint _lineBgPaint = new Paint(1);
    private Paint _linePaint = new Paint(1);
    private Paint _linePaint2 = new Paint(1);

    public PageIndicatorLine(Context context) {
        this(context, null);
    }

    public PageIndicatorLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActiveColor = Utilities.getColorAccent(context);
        mInActiveColor = getResources().getColor(R.color.page_indicator_dot_color);

        mIsRtl = Utilities.isRtl(getResources());

        _linePaint.setColor(-1);
        _linePaint.setStrokeWidth((float) Tool.toPx(4));
        _linePaint.setAntiAlias(true);
        _lineBgPaint.setColor(Color.parseColor("#73ECEFF7"));
        _lineBgPaint.setStrokeWidth((float) Tool.toPx(4));
        _lineBgPaint.setAntiAlias(true);
        _linePaint2.setColor(Color.BLACK);
        _linePaint2.setStrokeWidth((float) Tool.toPx(4));
        _linePaint2.setAntiAlias(true);
    }

    @Override
    public void setScroll(int currentScroll, int totalScroll) {
        if (mNumPages > 1) {
            if (mIsRtl) {
                currentScroll = totalScroll - currentScroll;
            }
            int scrollPerPage = totalScroll / (mNumPages - 1);
            int absScroll = mActivePage * scrollPerPage;
            float scrollThreshold = SHIFT_THRESHOLD * scrollPerPage;

            if ((absScroll - currentScroll) > scrollThreshold) {
                // current scroll is before absolute scroll
                animateToPosition(mActivePage - SHIFT_PER_ANIMATION);
            } else if ((currentScroll - absScroll) > scrollThreshold) {
                // current scroll is ahead of absolute scroll
                animateToPosition(mActivePage + SHIFT_PER_ANIMATION);
            } else {
                animateToPosition(mActivePage);
            }
        }
    }

    public void stopAllAnimations() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
        mFinalPosition = mActivePage;
        CURRENT_POSITION.set(this, mFinalPosition);
    }

    private void animateToPosition(float position) {
        mFinalPosition = position;
        if (Math.abs(mCurrentPosition - mFinalPosition) < SHIFT_THRESHOLD) {
            mCurrentPosition = mFinalPosition;
        }
        if (mAnimator == null && Float.compare(mCurrentPosition, mFinalPosition) != 0) {
            float positionForThisAnim = mCurrentPosition > mFinalPosition ?
                    mCurrentPosition - SHIFT_PER_ANIMATION : mCurrentPosition + SHIFT_PER_ANIMATION;
            mAnimator = ObjectAnimator.ofFloat(this, CURRENT_POSITION, positionForThisAnim);
            mAnimator.addListener(mAnimCycleListener);
            mAnimator.setDuration(ANIMATION_DURATION);
            mAnimator.start();
        }
    }

    @Override
    public void setActiveMarker(int activePage) {
        if (mActivePage != activePage) {
            mActivePage = activePage;
        }
    }

    @Override
    protected void onPageCountChanged() {
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mNumPages > 0) {
            float lineWidth = 90;
            float sep = (mNumPages - 1) * 30;
            float center = getWidth() / 2;

            if (mNumPages % 2 == 0f) {
                float lineInit = center - (mNumPages / 2) * lineWidth - sep / 2;
                float lineEnd = lineInit + 90;
                for (int i = 0; i < mNumPages; i++) {
                    canvas.drawLine(lineInit, 30, lineEnd, 30, _lineBgPaint);
                    lineInit = lineEnd + 30;
                    lineEnd += 120;
                }
                drawSelectedLine(mActivePage, mNumPages, canvas, center, lineWidth);
            } else {
                float lineInit = center - ((mNumPages - 1) / 2) * lineWidth - sep / 2 - 45;
                float lineEnd = lineInit + 90;
                for (int i = 0; i < mNumPages; i++) {
                    canvas.drawLine(lineInit, 30, lineEnd, 30, _lineBgPaint);
                    lineInit = lineEnd + 30;
                    lineEnd += 120;
                }
                drawSelectedLine(mActivePage, mNumPages, canvas, center, lineWidth);
            }
        }
    }

    private void drawSelectedLine(int scrollPosition, float pagesCount, Canvas canvas, float center, float lineWidth) {
        float sep = (pagesCount - 1) * 30;
        if (scrollPosition > 0) {
            float selectedInit = center - (pagesCount / 2) * lineWidth - sep / 2 + mActivePage * (lineWidth + 30);
            float selectedEnd = selectedInit + 90;
            canvas.drawLine(selectedInit, 30, selectedEnd, 30, _linePaint);
        } else {
            float selectedInit = center - (pagesCount / 2) * lineWidth - sep / 2 + mActivePage * lineWidth;
            float selectedEnd = selectedInit + 90;
            canvas.drawLine(selectedInit, 30, selectedEnd, 30, _linePaint);
        }
    }
}
