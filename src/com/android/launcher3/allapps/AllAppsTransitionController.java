package com.android.launcher3.allapps;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DeviceProfile.OnDeviceProfileChangeListener;
import com.android.launcher3.Hotseat;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager.AnimationConfig;
import com.android.launcher3.LauncherStateManager.StateHandler;
import com.android.launcher3.R;
import com.android.launcher3.anim.AnimationSuccessListener;
import com.android.launcher3.anim.AnimatorSetBuilder;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.graphics.GradientView;
import com.android.launcher3.util.SystemUiController;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.ScrimView;

import static com.android.launcher3.LauncherState.ALL_APPS_CONTENT;
import static com.android.launcher3.LauncherState.ALL_APPS_HEADER;
import static com.android.launcher3.LauncherState.ALL_APPS_HEADER_EXTRA;
import static com.android.launcher3.LauncherState.OVERVIEW;
import static com.android.launcher3.LauncherState.VERTICAL_SWIPE_INDICATOR;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_ALL_APPS_FADE;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_OVERVIEW_SCALE;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_VERTICAL_PROGRESS;
import static com.android.launcher3.anim.Interpolators.FAST_OUT_SLOW_IN;
import static com.android.launcher3.anim.Interpolators.LINEAR;
import static com.android.launcher3.anim.PropertySetter.NO_ANIM_PROPERTY_SETTER;
import static com.android.launcher3.util.SystemUiController.UI_STATE_ALL_APPS;

/**
 * Handles AllApps view transition.
 * 1) Slides all apps view using direct manipulation
 * 2) When finger is released, animate to either top or bottom accordingly.
 * <p/>
 * Algorithm:
 * If release velocity > THRES1, snap according to the direction of movement.
 * If release velocity < THRES1, snap according to either top or bottom depending on whether it's
 * closer to top or closer to the page indicator.
 */
public class AllAppsTransitionController implements StateHandler, OnDeviceProfileChangeListener {

    public static final Property<AllAppsTransitionController, Float> ALL_APPS_PROGRESS =
            new Property<AllAppsTransitionController, Float>(Float.class, "allAppsProgress") {

                @Override
                public Float get(AllAppsTransitionController controller) {
                    return controller.mProgress;
                }

                @Override
                public void set(AllAppsTransitionController controller, Float progress) {
                    controller.setProgress(progress);
                }
            };

    private AllAppsContainerView mAppsView;
    private ScrimView mScrimView;

    private final Launcher mLauncher;
    private final boolean mIsDarkTheme;
    private boolean mIsVerticalLayout;

    // Animation in this class is controlled by a single variable {@link mProgress}.
    // Visually, it represents top y coordinate of the all apps container if multiplied with
    // {@link mShiftRange}.

    // When {@link mProgress} is 0, all apps container is pulled up.
    // When {@link mProgress} is 1, all apps container is pulled down.
    private float mShiftRange;      // changes depending on the orientation
    private float mProgress;        // [0, 1], mShiftRange * mProgress = shiftCurrent

    private float mScrollRangeDelta = 0;
    private float mStatusBarHeight;

    private GradientView mGradientView;

    private Hotseat mHotseat;
    private int mHotseatBackgroundColor;

    public AllAppsTransitionController(Launcher l) {
        mLauncher = l;
        mShiftRange = mLauncher.getDeviceProfile().heightPx;
        mProgress = 1f;

        mIsDarkTheme = Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark);
        mIsVerticalLayout = mLauncher.getDeviceProfile().isVerticalBarLayout();
        mLauncher.addOnDeviceProfileChangeListener(this);
    }

    public float getShiftRange() {
        return mShiftRange;
    }

    private void onProgressAnimationStart() {
        // Initialize values that should not change until #onDragEnd
        mAppsView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeviceProfileChanged(DeviceProfile dp) {
        mIsVerticalLayout = dp.isVerticalBarLayout();
        setScrollRangeDelta(mScrollRangeDelta);

        if (mIsVerticalLayout) {
            mAppsView.setAlpha(1);
            mLauncher.getHotseat().setTranslationY(0);
            mLauncher.getWorkspace().getPageIndicator().setTranslationY(0);
        }
    }

    private void updateAllAppsBg(float progress) {
        // gradient
        if (mGradientView == null) {
            mGradientView = mLauncher.findViewById(R.id.gradient_bg);
            mGradientView.setVisibility(View.VISIBLE);
        }
        mGradientView.setProgress(progress, mShiftRange);
    }

    /**
     * @param start {@code true} if start of new drag.
     */
    public void preparePull(boolean start) {
        if (start) {
            if (!FeatureFlags.LEGACY_ALL_APPS_BACKGROUND)
                ((InputMethodManager) mLauncher.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mGradientView.getWindowToken(), 0);
            // Initialize values that should not change until #onDragEnd
            mStatusBarHeight = mLauncher.getDragLayer().getInsets().top;
            mHotseat.setVisibility(View.VISIBLE);
            mHotseatBackgroundColor = mHotseat.getBackgroundDrawableColor();
            mHotseat.setBackgroundTransparent(true /* transparent */);
            /*if (!mLauncher.isAllAppsVisible()) {
                mLauncher.tryAndUpdatePredictedApps();
                mAppsView.setVisibility(View.VISIBLE);
                if (FeatureFlags.LEGACY_ALL_APPS_BACKGROUND) {
                    mAppsView.setRevealDrawableColor(mHotseatBackgroundColor);
                }
            }*/
        }
    }


    private void updateLightStatusBar(float shift) {
        // Do not modify status bar on landscape as all apps is not full bleed.
        boolean verticalBarLayout = mLauncher.getDeviceProfile().isVerticalBarLayout();
        if (!FeatureFlags.LAUNCHER3_GRADIENT_ALL_APPS
                && verticalBarLayout) {
            return;
        }

        // Use a light system UI (dark icons) if all apps is behind at least half of the status bar.
        boolean forceChange = FeatureFlags.LAUNCHER3_GRADIENT_ALL_APPS ?
                shift <= mShiftRange / 4 :
                shift <= mStatusBarHeight / 2;
        if (forceChange) {
            mLauncher.getSystemUiController().updateUiState(
                    SystemUiController.UI_STATE_ALL_APPS, !mIsDarkTheme);
        } else {
            mLauncher.getSystemUiController().updateUiState(
                    SystemUiController.UI_STATE_ALL_APPS, 0);
        }
        //mCaretController.setHidden(FeatureFlags.LAUNCHER3_P_ALL_APPS && !verticalBarLayout);
        //mCaretController.setForceDark(verticalBarLayout && forceChange && !mIsDarkTheme);
    }
    /**
     * Note this method should not be called outside this class. This is public because it is used
     * in xml-based animations which also handle updating the appropriate UI.
     *
     * @param progress value between 0 and 1, 0 shows all apps and 1 shows workspace
     *
     * @see #setState(LauncherState)
     * @see #setStateWithAnimation(LauncherState, AnimatorSetBuilder, AnimationConfig)
     */
    public void setProgress(float progress) {
        mProgress = progress;
        mScrimView.setProgress(progress);
        float shiftCurrent = progress * mShiftRange;

        mAppsView.setTranslationY(shiftCurrent);
        float hotseatTranslation = -mShiftRange + shiftCurrent;

        if (!mIsVerticalLayout) {
            mLauncher.getHotseat().setTranslationY(hotseatTranslation);
            mLauncher.getWorkspace().getPageIndicator().setTranslationY(hotseatTranslation);
        }

        // Use a light system UI (dark icons) if all apps is behind at least half of the
        // status bar.
        boolean forceChange = shiftCurrent - mScrimView.getDragHandleSize()
                <= mLauncher.getDeviceProfile().getInsets().top / 2;
        if (forceChange) {
            mLauncher.getSystemUiController().updateUiState(UI_STATE_ALL_APPS, !mIsDarkTheme);
        } else {
            mLauncher.getSystemUiController().updateUiState(UI_STATE_ALL_APPS, 0);
        }
        updateLightStatusBar(shiftCurrent);
    }

    public float getProgress() {
        return mProgress;
    }

    /**
     * Sets the vertical transition progress to {@param state} and updates all the dependent UI
     * accordingly.
     */
    @Override
    public void setState(LauncherState state) {
        setProgress(state.getVerticalProgress(mLauncher));
        setAlphas(state, null, new AnimatorSetBuilder());
        onProgressAnimationEnd();
    }

    /**
     * Creates an animation which updates the vertical transition progress and updates all the
     * dependent UI using various animation events
     */
    @Override
    public void setStateWithAnimation(LauncherState toState,
                                      AnimatorSetBuilder builder, AnimationConfig config) {
        float targetProgress = toState.getVerticalProgress(mLauncher);
        if (Float.compare(mProgress, targetProgress) == 0) {
            setAlphas(toState, config, builder);
            // Fail fast
            onProgressAnimationEnd();
            return;
        }

        if (!config.playNonAtomicComponent()) {
            // There is no atomic component for the all apps transition, so just return early.
            return;
        }

        Interpolator interpolator = config.userControlled ? LINEAR : toState == OVERVIEW
                ? builder.getInterpolator(ANIM_OVERVIEW_SCALE, FAST_OUT_SLOW_IN)
                : FAST_OUT_SLOW_IN;
        ObjectAnimator anim =
                ObjectAnimator.ofFloat(this, ALL_APPS_PROGRESS, mProgress, targetProgress);
        anim.setDuration(config.duration);
        anim.setInterpolator(builder.getInterpolator(ANIM_VERTICAL_PROGRESS, interpolator));
        anim.addListener(getProgressAnimatorListener());

        builder.play(anim);

        setAlphas(toState, config, builder);
    }

    private void setAlphas(LauncherState toState, AnimationConfig config,
                           AnimatorSetBuilder builder) {
        PropertySetter setter = config == null ? NO_ANIM_PROPERTY_SETTER
                : config.getPropertySetter(builder);
        int visibleElements = toState.getVisibleElements(mLauncher);
        boolean hasHeader = (visibleElements & ALL_APPS_HEADER) != 0;
        boolean hasHeaderExtra = (visibleElements & ALL_APPS_HEADER_EXTRA) != 0;
        boolean hasContent = (visibleElements & ALL_APPS_CONTENT) != 0;

        Interpolator allAppsFade = builder.getInterpolator(ANIM_ALL_APPS_FADE, LINEAR);
        setter.setViewAlpha(mAppsView.getSearchView(), hasHeader ? 1 : 0, allAppsFade);
        setter.setViewAlpha(mAppsView.getContentView(), hasContent ? 1 : 0, allAppsFade);
        setter.setViewAlpha(mAppsView.getScrollBar(), hasContent ? 1 : 0, allAppsFade);
        mAppsView.getFloatingHeaderView().setContentVisibility(hasHeaderExtra, hasContent, setter,
                allAppsFade);

        setter.setInt(mScrimView, ScrimView.DRAG_HANDLE_ALPHA,
                (visibleElements & VERTICAL_SWIPE_INDICATOR) != 0 ? 255 : 0, LINEAR);
    }

    public AnimatorListenerAdapter getProgressAnimatorListener() {
        return new AnimationSuccessListener() {
            @Override
            public void onAnimationSuccess(Animator animator) {
                onProgressAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                onProgressAnimationStart();
            }
        };
    }

    public void setupViews(AllAppsContainerView appsView) {
        mAppsView = appsView;
        mScrimView = mLauncher.findViewById(R.id.scrim_view);
    }

    /**
     * Updates the total scroll range but does not update the UI.
     */
    public void setScrollRangeDelta(float delta) {
        mScrollRangeDelta = delta;
        mShiftRange = mLauncher.getDeviceProfile().heightPx - mScrollRangeDelta;

        if (mScrimView != null) {
            mScrimView.reInitUi();
        }
    }

    /**
     * Set the final view states based on the progress.
     * TODO: This logic should go in {@link LauncherState}
     */
    private void onProgressAnimationEnd() {
        if (Float.compare(mProgress, 1f) == 0) {
            mAppsView.setVisibility(View.INVISIBLE);
            mAppsView.reset(false /* animate */);
        } else if (Float.compare(mProgress, 0f) == 0) {
            mAppsView.setVisibility(View.VISIBLE);
            mAppsView.onScrollUpEnd();
        } else {
            mAppsView.setVisibility(View.VISIBLE);
        }
    }
}
