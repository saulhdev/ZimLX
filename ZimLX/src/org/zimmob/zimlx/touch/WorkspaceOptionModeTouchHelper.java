package org.zimmob.zimlx.touch;

import android.view.MotionEvent;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.Workspace;

public class WorkspaceOptionModeTouchHelper {
    private static final String TAG = "WorkspaceOptionMode";
    private long mCurrentMillis;
    private boolean mIsInTouchCycle;
    private boolean mIsStillPossibleClick;
    private int mLastTouchX;
    private int mLastTouchY;
    private Launcher mLauncher;
    private int mMinSnapDistance;
    private int mMinSnapVelocity;
    private float mPossibleClickDistanceSquare = 400.0f;
    private int mScreenWidth;
    private int mTouchDownWorkspaceCurrentPage;
    private int mTouchDownWorkspaceScrollX;
    private int mTouchDownX;
    private int mTouchDownY;
    private float mVelocity;
    private int mWorkspaceOptionModeEndPage;
    private int mWorkspaceOptionModeStartPage;

    private static float computeDampeningFactor(float f) {
        return f / (15.915494f + f);
    }

    private float disSquare(float f, float f2, float f3, float f4) {
        f -= f3;
        f2 -= f4;
        return (f * f) + (f2 * f2);
    }

    public static float interpolate(float f, float f2, float f3) {
        return ((1.0f - f3) * f) + (f3 * f2);
    }

    public WorkspaceOptionModeTouchHelper(Launcher launcher) {
        mLauncher = launcher;
        mScreenWidth = launcher.getDeviceProfile().widthPx;
        mMinSnapDistance = mScreenWidth / 3;
        mMinSnapVelocity = 1;
    }

    private boolean handleTouchMove(MotionEvent motionEvent) {
        if (!mIsInTouchCycle) {
            return false;
        }
        mLauncher.getWorkspace().scrollTo(
                (int) (mTouchDownX - motionEvent.getX() + mTouchDownWorkspaceScrollX), 0);
        if (mIsStillPossibleClick) {
            mIsStillPossibleClick = isPossibleClick(motionEvent);
        }
        computeVelocity(Math.abs(mLastTouchX - motionEvent.getX()), System.currentTimeMillis());
        mLastTouchX = (int) motionEvent.getX();
        mLastTouchY = (int) motionEvent.getY();
        return true;
    }

    private boolean handleTouchOther(MotionEvent motionEvent) {
        if (!mIsInTouchCycle) {
            return false;
        }
        Workspace workspace = mLauncher.getWorkspace();
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            float x = ((float) mTouchDownX) - motionEvent.getX();
            boolean isPossibleClick = isPossibleClick(motionEvent);
            if ((Math.abs(x) > mMinSnapDistance || mVelocity >= ((float) mMinSnapVelocity)) && !(mIsStillPossibleClick && isPossibleClick)) {
                workspace.snapToPage(getNextPage(mTouchDownWorkspaceCurrentPage, x > 0));
            } else {
                if (mIsStillPossibleClick && isPossibleClick(motionEvent)) {
                    workspace.snapToPage(getNextPage(mTouchDownWorkspaceCurrentPage,
                            mTouchDownX > mLauncher.getDeviceProfile().widthPx / 2));
                    mLauncher.getStateManager().goToState(LauncherState.NORMAL, true);
                } else {
                    workspace.snapToPage(mTouchDownWorkspaceCurrentPage);
                }
            }
        } else {
            workspace.snapToPage(mTouchDownWorkspaceCurrentPage);
        }
        mIsInTouchCycle = false;
        return true;
    }

    private int getNextPage(int page, boolean positive) {
        page += positive ? 1 : -1;
        if (page < mWorkspaceOptionModeStartPage) return mWorkspaceOptionModeStartPage;
        if (page > mWorkspaceOptionModeEndPage) return mWorkspaceOptionModeEndPage;
        return page;
    }

    private boolean isPossibleClick(MotionEvent motionEvent) {
        return disSquare(mTouchDownX, mTouchDownY, motionEvent.getX(), motionEvent.getY()) <= mPossibleClickDistanceSquare;
    }

    public float computeVelocity(float f, long j) {
        long j2 = mCurrentMillis;
        mCurrentMillis = j;
        float f2 = (float) (mCurrentMillis - j2);
        float f3 = 0.0f;
        if (f2 > 0.0f) {
            f3 = f / f2;
        }
        if (Math.abs(mVelocity) < 0.001f) {
            mVelocity = f3;
        } else {
            mVelocity = interpolate(mVelocity, f3, computeDampeningFactor(f2));
        }
        return mVelocity;
    }
}