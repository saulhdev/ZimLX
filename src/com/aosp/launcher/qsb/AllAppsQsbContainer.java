/*
 * Copyright (C) 2019 Paranoid Android
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
package com.aosp.launcher.qsb;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.graphics.NinePatchDrawHelper;
import com.android.launcher3.icons.ShadowGenerator.Builder;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.android.launcher3.util.TransformingTouchDelegate;
import com.android.quickstep.WindowTransformSwipeHandler;
import com.aosp.launcher.AospLauncher;
import com.aosp.launcher.qsb.configs.ConfigurationBuilder;
import com.aosp.launcher.search.SearchHandler;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.ZimPreferences;
import org.zimmob.zimlx.globalsearch.SearchProvider;
import org.zimmob.zimlx.globalsearch.SearchProviderController;
import org.zimmob.zimlx.globalsearch.providers.AppSearchSearchProvider;
import org.zimmob.zimlx.globalsearch.providers.GoogleSearchProvider;
import org.zimmob.zimlx.globalsearch.providers.web.WebSearchProvider;
import org.zimmob.zimlx.qsb.k;

import static com.android.launcher3.LauncherState.ALL_APPS_HEADER;
import static com.android.launcher3.LauncherState.HOTSEAT_SEARCH_BOX;

public class AllAppsQsbContainer extends AbstractQsbLayout implements Insettable, WallpaperColorInfo.OnChangeListener,
        SearchUiManager {

    private static final long SEARCH_TASK_DELAY_MS = 450;

    private AllAppsContainerView mAppsView;
    private Context mContext;
    private static final Rect mSrcRect = new Rect();
    private TransformingTouchDelegate mDelegate;

    private boolean mKeepDefaultQsb;
    private boolean mIsMainColorDark;
    private boolean mIsRtl;
    private float mFixedTranslationY;
    private float mSearchIconStrokeWidth;
    private int mAlpha;
    private int mColor;
    private int mMarginAdjusting;
    protected final NinePatchDrawHelper mClearShadowHelper;
    private int mSearchIconWidth;
    private final Paint mShadowPaint = new Paint(1);
    private final NinePatchDrawHelper mShadowHelper = new NinePatchDrawHelper();
    public AospLauncher mLauncher;
    private Paint mSearchIconStrokePaint = new Paint(1);
    public ImageView mMicIconView;
    public boolean mDoNotRemoveFallback;
    public Bitmap mShadowBitmap;
    protected int mResult;
    protected Bitmap mClearBitmap;
    ZimPreferences prefs;
    private int mMarginTop;
    private ImageView mLogoIconView;
    private boolean mShowAssistant;
    private float mRadius = -1.0f;
    private Bitmap mQsbScroll;
    private boolean mUseFallbackSearch;
    private TextView mHint;

    public AllAppsQsbContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsQsbContainer(Context context, AttributeSet attributeSet, int res) {
        super(context, attributeSet, res);
        mContext = context;
        prefs = Utilities.getZimPrefs(context);
        mResult = 0;
        mAlpha = 0;
        mLauncher = (AospLauncher) Launcher.getLauncher(context);
        setOnClickListener(this);
        mIsMainColorDark = Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark);
        mMarginAdjusting = mContext.getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        mMarginTop = mContext.getResources().getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset);
        mSearchIconWidth = getResources().getDimensionPixelSize(R.dimen.qsb_mic_width);
        mIsRtl = Utilities.isRtl(getResources());
        mDelegate = new TransformingTouchDelegate(this);
        mShadowPaint.setColor(-1);
        mFixedTranslationY = Math.round(getTranslationY());

        this.Ds = k.getInstance(context);

        mClearShadowHelper = new NinePatchDrawHelper();
        mClearShadowHelper.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        setClipToPadding(false);
        setTranslationY(0);
    }

    @Override
    public void setInsets(Rect insets) {
        loadFallback();
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.topMargin = getTopMargin(insets);
        requestLayout();
        if (mActivity.getDeviceProfile().isVerticalBarLayout()) {
            mActivity.getAllAppsController().setScrollRangeDelta(0);
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mHint = findViewById(R.id.qsb_hint);
        mMicIconView = findViewById(R.id.mic_icon);
        setTouchDelegate(mDelegate);
        requestLayout();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDelegate.setDelegateView(mMicIconView);
        SearchProviderController.Companion.getInstance(getContext()).addOnProviderChangeListener(this);
        reloadPreferences().registerOnSharedPreferenceChangeListener(this);
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(getContext());
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);
        dN();
        updateConfiguration();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMicIconView.getHitRect(mSrcRect);
        if (this.mIsRtl) {
            mSrcRect.left -= mShadowMargin;
        } else {
            mSrcRect.right += mShadowMargin;
        }
        mDelegate.setBounds(mSrcRect.left, mSrcRect.top, mSrcRect.right, mSrcRect.bottom);
        View view = (View) getParent();
        setTranslationX((float) ((view.getPaddingLeft() + (
                (((view.getWidth() - view.getPaddingLeft()) - view.getPaddingRight()) - (right - left)) / 2)) - left));
        offsetTopAndBottom((int) mFixedTranslationY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            View gIcon = findViewById(R.id.g_icon);
            int result = 0;
            int newResult = 1;
            if (mIsRtl) {
                if (Float.compare(motionEvent.getX(), (float) (gIcon.getLeft())) >= 0) {
                    result = 1;
                }
            } else {
                if (Float.compare(motionEvent.getX(), (float) (gIcon.getRight())) <= 0) {
                    result = 1;
                }
            }
            if (result == 0) {
                newResult = 2;
            }
            mResult = newResult;
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override
    public void onDetachedFromWindow() {
        WallpaperColorInfo.getInstance(getContext()).removeOnChangeListener(this);
        Utilities.getPrefs(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        SearchProviderController.Companion.getInstance(getContext()).removeOnProviderChangeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DeviceProfile dp = mLauncher.getDeviceProfile();
        int round = Math.round(((float) dp.iconSizePx) * 0.92f);
        setMeasuredDimension(calculateMeasuredDimension(dp, round, widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            measureChildWithMargins(childAt, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (childAt.getWidth() <= round) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int measuredWidth = (round - childAt.getWidth()) / 2;
                layoutParams.rightMargin = measuredWidth;
                layoutParams.leftMargin = measuredWidth;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mAlpha > 0) {
            if (mQsbScroll == null) {
                mQsbScroll = createBitmap(
                        mContext.getResources().getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                        mContext.getResources().getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset), 0);
            }
            mShadowHelper.paint.setAlpha(mAlpha);
            drawShadow(mQsbScroll, canvas);
            mShadowHelper.paint.setAlpha(255);
        }

        drawCanvas(canvas, getWidth());

        loadBitmap();
        clearMainPillBg(canvas);
        drawShadow(mShadowBitmap, canvas);
        int i;

        if (mUseTwoBubbles) {
            int paddingLeft;
            int paddingLeft2;
            if (Db == null) {
                Bitmap bitmap;
                if (Dc == Dd) {
                    i = 1;
                } else {
                    i = 0;
                }
                if (i != 0) {
                    bitmap = mShadowBitmap;
                } else {
                    bitmap = getShadowBitmap(Dd);
                }
                Db = bitmap;
            }
            Bitmap bitmap2 = Db;
            i = getShadowDimens(bitmap2);
            int paddingTop = getPaddingTop() - ((bitmap2.getHeight() - getHeightWithoutPadding()) / 2);
            if (mIsRtl) {
                paddingLeft = getPaddingLeft() - i;
                paddingLeft2 = getPaddingLeft() + i;
                i = dG();
            } else {
                paddingLeft = ((getWidth() - getPaddingRight()) - dG()) - i;
                paddingLeft2 = getWidth() - getPaddingRight();
            }
            clearPillBg(canvas, paddingLeft, paddingTop, paddingLeft2 + i);
            mShadowHelper.draw(bitmap2, canvas, (float) paddingLeft, (float) paddingTop, (float) (paddingLeft2 + i));
        }
        if (mSearchIconStrokeWidth > 0.0f && mMicIconView.getVisibility() == View.VISIBLE) {
            float i2;
            i = mIsRtl ? getPaddingLeft() : (getWidth() - getPaddingRight()) - dG();
            int paddingTop2 = getPaddingTop();
            int paddingLeft3 = mIsRtl ? getPaddingLeft() + dG() : getWidth() - getPaddingRight();
            int paddingBottom = LauncherAppState.getInstance(getContext()).getInvariantDeviceProfile().iconBitmapSize - getPaddingBottom();
            float f = ((float) (paddingBottom - paddingTop2)) * 0.5f;
            float i3 = mSearchIconStrokeWidth / 2.0f;
            if (mUseTwoBubbles) {
                i2 = i3;
            } else {
                i2 = i3;
                canvas.drawRoundRect(i + i3, paddingTop2 + i3, paddingLeft3 - i3, (paddingBottom - i3) + 1, f, f, CV);
            }
            canvas.drawRoundRect(i + i2, paddingTop2 + i2, paddingLeft3 - i2, (paddingBottom - i2) + 1, f, f, mMicStrokePaint);
        }
        super.draw(canvas);
    }

    private void dN() {
        az(Dc);
        addOrUpdateSearchPaint(Ds.micStrokeWidth());
        this.Dh = Ds.hintIsForAssistant();
        mUseTwoBubbles = useTwoBubbles();
        setHintText(Ds.hintTextValue(), mHint);
        addOrUpdateSearchRipple();
    }

    /*public final void h(float f) {
        micStrokeWidth = TypedValue.applyDimension(1, f, getResources().getDisplayMetrics());
        mMicStrokePaint.setStrokeWidth(this.micStrokeWidth);
        mMicStrokePaint.setStyle(Style.STROKE);
        mMicStrokePaint.setColor(0xFFBDC1C6);
    }*/

    protected final boolean dE() {
        if (!Dh) {
            return mUseTwoBubbles;
        }
        return true;
    }

    public final void az(int i) {
        Dd = i;
        if (Dd != Dc || Db != mShadowBitmap) {
            Db = null;
            invalidate();
        }
    }

    protected int dF() {
        return mUseTwoBubbles ? dG() + twoBubbleGap : 0;
    }

    protected int dG() {
        if (!mUseTwoBubbles || TextUtils.isEmpty(Dg)) {
            return qsbMicWidth;
        }
        return (Math.round(qsbHint.measureText(Dg)) + qsbTextSpacing) + qsbMicWidth;
    }

    protected int dD() {
        return mUseTwoBubbles ? qsbMicWidth : qsbMicWidth + qsbTextSpacing;
    }

    protected final void setHintText(String str, TextView textView) {
        String str2;
        if (TextUtils.isEmpty(str) || !dE()) {
            str2 = str;
        } else {
            str2 = TextUtils.ellipsize(str, qsbHint, (float) qsbMaxHintLength, TextUtils.TruncateAt.END).toString();
        }
        this.Dg = str2;
        textView.setText(this.Dg);
        int i = 17;
        if (dE()) {
            i = 8388629;
            if (this.mIsRtl) {
                textView.setPadding(dD(), 0, 0, 0);
            } else {
                textView.setPadding(0, 0, dD(), 0);
            }
        }
        textView.setGravity(i);
        ((LayoutParams) textView.getLayoutParams()).gravity = i;
        textView.setContentDescription(str);
    }

    public int getTopMargin(@NotNull Rect rect) {
        return Math.max(-mMarginTop, rect.top - mMarginAdjusting);
    }

    @Override
    public void onExtractedColorsChanged(@NotNull WallpaperColorInfo wallpaperColorInfo) {
        setColor(ColorUtils.compositeColors(ColorUtils.compositeColors(
                Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark)
                        ? -650362813 : -855638017, Themes.getAttrColor(mLauncher, R.attr.allAppsScrimColor)), wallpaperColorInfo.getMainColor()));
    }

    @Override
    public void initialize(AllAppsContainerView allAppsContainerView) {
        mAppsView = allAppsContainerView;
        mAppsView.addElevationController(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                updateAlpha(((BaseRecyclerView) recyclerView).getCurrentScrollY());
            }
        });
        mAppsView.setRecyclerViewVerticalFadingEdgeEnabled(true);
    }

    private void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            mShadowBitmap = null;
            invalidate();
        }
    }

    private void addOrUpdateSearchPaint(float value) {
        mSearchIconStrokeWidth = TypedValue.applyDimension(1, value, getResources().getDisplayMetrics());
        mSearchIconStrokePaint.setStrokeWidth(mSearchIconStrokeWidth);
        mSearchIconStrokePaint.setStyle(Style.STROKE);
        mSearchIconStrokePaint.setColor(-4341306);
    }

    private void updateConfiguration() {
        addOrUpdateSearchPaint(0.0f);
        addOrUpdateSearchRipple();
    }

    /*@Override
    public void onClick(View view) {
        if (mLauncher.isInState(ALL_APPS)) {
            startSearch("", mResult);
        } else {
            mLauncher.getStateManager().goToState(ALL_APPS);
            new Handler().postDelayed(() -> startSearch("", mResult), SEARCH_TASK_DELAY_MS);
        }
    }*/
    public void onClick(View view) {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mActivity);
        SearchProvider provider = controller.getSearchProvider();
        if (view == mMicIconView) {
            if (controller.isGoogle()) {
                fallbackSearch(mShowAssistant ? Intent.ACTION_VOICE_COMMAND : "android.intent.action.VOICE_ASSIST");
            } else if (mShowAssistant && provider.getSupportsAssistant()) {
                provider.startAssistant(intent -> {
                    getContext().startActivity(intent);
                    return null;
                });
            } else if (provider.getSupportsVoiceSearch()) {
                provider.startVoiceSearch(intent -> {
                    getContext().startActivity(intent);
                    return null;
                });
            }
        } else if (view == mLogoIconView) {
            if (provider.getSupportsFeed() && logoCanOpenFeed()) {
                provider.startFeed(intent -> {
                    getContext().startActivity(intent);
                    return null;
                });
            } else {
                startSearch("", mResult);
            }
        }
    }

    protected boolean logoCanOpenFeed() {
        return true;
    }

    public void startSearch(String initialQuery, int result) {
        /*ConfigurationBuilder config = new ConfigurationBuilder(this, true);
        if (mLauncher.getLauncherCallbacks().getClient().startSearch(config.build(), config.getExtras())) {
            mLauncher.getLauncherCallbacks().getQsbController().playQsbAnimation();
        } else {
            warmUpDefaultQsb(initialQuery);
        }
        mResult = 0;*/
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mActivity);
        SearchProvider provider = controller.getSearchProvider();
        if (shouldUseFallbackSearch(provider)) {
            searchFallback(initialQuery);
        } else if (controller.isGoogle()) {
            ConfigurationBuilder f = new ConfigurationBuilder(this, true);
            if (!mActivity.getGoogleNow().startSearch(f.build(), f.getExtras())) {
                searchFallback(initialQuery);
                if (mFallback != null) {
                    mFallback.setHint(null);
                }
            }
        } else {
            provider.startSearch(intent -> {
                mActivity.startActivity(intent);
                return null;
            });
        }
    }

    private boolean shouldUseFallbackSearch(SearchProvider provider) {
        return !Utilities
                .getZimPrefs(getContext()).getAllAppsGlobalSearch()
                || provider instanceof AppSearchSearchProvider
                || provider instanceof WebSearchProvider
                || (!Utilities.ATLEAST_NOUGAT && provider instanceof GoogleSearchProvider);
    }

    protected void fallbackSearch(String action) {
        try {
            getContext().startActivity(new Intent(action)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setPackage(GOOGLE_QSB));
        } catch (ActivityNotFoundException e) {
            //noGoogleAppSearch();
        }
    }

    public void searchFallback(String query) {
        ensureFallbackView();
        mFallback.setText(query);
        mFallback.showKeyboard();
    }

    private void ensureFallbackView() {
        if (mFallback == null) {
            setOnClickListener(null);
            mFallback = (FallbackAppsSearchView) this.mActivity.getLayoutInflater()
                    .inflate(R.layout.all_apps_google_search_fallback, this, false);
            AllAppsContainerView allAppsContainerView = this.mAppsView;
            mFallback.DJ = this;
            mFallback.mApps = allAppsContainerView.getApps();
            mFallback.mAppsView = allAppsContainerView;
            mFallback.DI.initialize(new SearchHandler(mFallback.getContext()), mFallback,
                    Launcher.getLauncher(mFallback.getContext()), mFallback);
            addView(this.mFallback);
            //mFallback.setTextColor(mForegroundColor);
        }
    }

    protected final void loadFallback() {
        if (mUseFallbackSearch) {
            removeFallbackView();
            mUseFallbackSearch = false;
        }
    }

    public void resetSearch() {
        updateAlpha(0);
        if (mUseFallbackSearch) {
            resetFallbackView();
        } else if (!mDoNotRemoveFallback) {
            removeFallbackView();
        }
    }

    private void removeFallbackView() {
        if (mFallback != null) {
            mFallback.clearSearchResult();
            setOnClickListener(this);
            removeView(mFallback);
            mFallback = null;
        }
    }

    private void resetFallbackView() {
        if (mFallback != null) {
            mFallback.reset();
            mFallback.clearSearchResult();
        }
    }

    /*private void removeDefaultQsb() {
        if (mDefaultQsb != null) {
            mDefaultQsb.clearSearchResult();
            setOnClickListener(this);
            removeView(mDefaultQsb);
            mDefaultQsb = null;
        }
    }*/
/*
    private void resetDefaultQsb() {
        if (mDefaultQsb != null) {
            mDefaultQsb.reset();
            mDefaultQsb.clearSearchResult();
        }
    }*/

    public int getMeasuredWidth(int width, DeviceProfile dp) {
        int leftRightPadding = dp.desiredWorkspaceLeftRightMarginPx
                + dp.cellLayoutPaddingLeftRightPx;
        return width - leftRightPadding * 2;
    }

    public int calculateMeasuredDimension(DeviceProfile dp, int round, int widthMeasureSpec) {
        int width = getMeasuredWidth(MeasureSpec.getSize(widthMeasureSpec), dp);
        int calculateCellWidth = width - ((width / dp.inv.numHotseatIcons) - round);
        return getPaddingRight() + getPaddingLeft() + calculateCellWidth;
    }

    private void loadBitmap() {
        if (mShadowBitmap == null) {
            mShadowBitmap = getShadowBitmap(mColor);
            mClearBitmap = null;
            if (Color.alpha(mColor) != 255) {
                mClearBitmap = getShadowBitmap(0xFF000000);
            }
        }
    }

    protected void clearMainPillBg(Canvas canvas) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            drawPill(mClearShadowHelper, mClearBitmap, canvas);
        }
    }

    protected void clearPillBg(Canvas canvas, int left, int top, int right) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            mClearShadowHelper.draw(mClearBitmap, canvas, left, top, right);
        }
    }

    protected void drawPill(NinePatchDrawHelper helper, Bitmap bitmap, Canvas canvas) {
        int a = getShadowDimens(bitmap);
        int left = getPaddingLeft() - a;
        int top = getPaddingTop() - ((bitmap.getHeight() - getHeightWithoutPadding()) / 2);
        int right = (getWidth() - getPaddingRight()) + a;
        if (mIsRtl) {
            left += dF();
        } else {
            right -= dF();
        }
        helper.draw(bitmap, canvas, (float) left, (float) top, (float) right);
    }

    public void drawCanvas(Canvas canvas, int width) {
        loadBitmap();
        drawShadow(mShadowBitmap, canvas);
        if (mSearchIconStrokeWidth > WindowTransformSwipeHandler.SWIPE_DURATION_MULTIPLIER && mMicIconView.getVisibility() == View.VISIBLE) {
            int paddingLeft = mIsRtl ? getPaddingLeft() : (width - getPaddingRight()) - getMicWidth();
            int paddingTop = getPaddingTop();
            int paddingRight = mIsRtl ? getPaddingLeft() + getMicWidth() : width - getPaddingRight();
            int paddingBottom = LauncherAppState.getIDP(getContext()).iconBitmapSize - getPaddingBottom();
            float height = ((float) (paddingBottom - paddingTop)) * 0.5f;
            int micStrokeWidth = (int) (mSearchIconStrokeWidth / 2.0f);
            if (mSearchIconStrokePaint == null) {
                mSearchIconStrokePaint = new Paint(1);
            }
            mSearchIconStrokePaint.setColor(-4341306);
            canvas.drawRoundRect((float) (paddingLeft + micStrokeWidth), (float) (paddingTop + micStrokeWidth), (float) (paddingRight - micStrokeWidth), (float) ((paddingBottom - micStrokeWidth) + 1), height, height, mSearchIconStrokePaint);
        }
    }

    private void drawShadow(Bitmap bitmap, Canvas canvas) {
        int shadowDimens = getShadowDimens(bitmap);
        int paddingTop = getPaddingTop() - ((bitmap.getHeight() - getHeightWithoutPadding()) / 2);
        int paddingLeft = getPaddingLeft() - shadowDimens;
        int width = (getWidth() - getPaddingRight()) + shadowDimens;
        if (mIsRtl) {
            paddingLeft += getRtlDimens();
        } else {
            width -= getRtlDimens();
        }
        mShadowHelper.draw(bitmap, canvas, (float) paddingLeft, (float) paddingTop, (float) width);
    }

    private Bitmap getShadowBitmap(int color) {
        int iconBitmapSize = LauncherAppState.getIDP(getContext()).iconBitmapSize;
        return createBitmap(((float) iconBitmapSize) / 96f, ((float) iconBitmapSize) / 48f, color);
    }

    private Bitmap createBitmap(float shadowBlur, float keyShadowDistance, int color) {
        int height = getHeightWithoutPadding();
        int heightSpec = height + 20;
        Builder builder = new Builder(color);
        builder.shadowBlur = shadowBlur;
        builder.keyShadowDistance = keyShadowDistance;
        builder.keyShadowAlpha = builder.ambientShadowAlpha;
        Bitmap pill = builder.createPill(heightSpec, height);
        if (Color.alpha(color) < 255) {
            Canvas canvas = new Canvas(pill);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            canvas.drawRoundRect(builder.bounds, (float) (height / 2), (float) (height / 2), paint);
            paint.setXfermode(null);
            paint.setColor(color);
            canvas.drawRoundRect(builder.bounds, (float) (height / 2), (float) (height / 2), paint);
            canvas.setBitmap(null);
        }
        if (Utilities.ATLEAST_OREO) {
            return pill.copy(Config.HARDWARE, false);
        }
        return pill;
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        if (mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            return WindowTransformSwipeHandler.SWIPE_DURATION_MULTIPLIER;
        }
        DeviceProfile dp = mLauncher.getWallpaperDeviceProfile();
        int height = (dp.hotseatBarSizePx - dp.hotseatCellHeightPx) - getLayoutParams().height;
        int marginBottom = insets.bottom;
        return getLayoutParams().height + Math.max(-mFixedTranslationY, insets.top - mMarginAdjusting) + mFixedTranslationY + marginBottom + ((int) (((float) (height - marginBottom)) * 0.45f));
    }

    private void addOrUpdateSearchRipple() {
        InsetDrawable insetDrawable = (InsetDrawable) getResources().getDrawable(R.drawable.qsb_icon_feedback_bg).mutate();
        RippleDrawable oldRipple = (RippleDrawable) insetDrawable.getDrawable();
        int width = mIsRtl ? getRtlDimens() : 0;
        int height = mIsRtl ? 0 : getRtlDimens();

        oldRipple.setLayerInset(0, width, 0, height, 0);
        setBackground(insetDrawable);
        RippleDrawable newRipple = (RippleDrawable) oldRipple.getConstantState().newDrawable().mutate();
        newRipple.setLayerInset(0, 0, mShadowMargin, 0, mShadowMargin);
        mMicIconView.setBackground(newRipple);
        mMicIconView.getLayoutParams().width = getMicWidth();

        int micWidth = mIsRtl ? 0 : getMicWidth() - mSearchIconWidth;
        int micHeight = mIsRtl ? getMicWidth() - mSearchIconWidth : 0;

        mMicIconView.setPadding(micWidth, 0, micHeight, 0);
        mMicIconView.requestLayout();
    }

    public void updateAlpha(int alpha) {
        alpha = Utilities.boundToRange(alpha, 0, 255);
        if (mAlpha != alpha) {
            mAlpha = alpha;
            invalidate();
        }
    }

    public int getShadowDimens(Bitmap bitmap) {
        return (bitmap.getWidth() - (getHeightWithoutPadding() + 20)) / 2;
    }

    public int getHeightWithoutPadding() {
        return (getHeight() - getPaddingTop()) - getPaddingBottom();
    }

    public int getRtlDimens() {
        return 0;
    }

    public int getMicWidth() {
        return mSearchIconWidth;
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter, Interpolator interpolator) {
        boolean hasSearchBoxContent = (visibleElements & HOTSEAT_SEARCH_BOX) != 0 && (visibleElements & ALL_APPS_HEADER) != 0;
        setter.setViewAlpha(this, 1, interpolator);
    }

    @Override
    public void startSearch() {
        post(() -> startSearch("", 0));
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent keyEvent) {
    }

    public void setKeepDefaultView(boolean canKeep) {
        mKeepDefaultQsb = canKeep;
    }

    protected final SharedPreferences reloadPreferences() {
        loadIcons();
        SharedPreferences devicePrefs = Utilities.getPrefs(getContext());
        loadPreferences(devicePrefs);
        return devicePrefs;
    }

    protected final void loadIcons() {
        mLogoIconView = findViewById(R.id.g_icon);
        mMicIconView = findViewById(R.id.mic_icon);
        mMicIconView.setOnClickListener(this);
        mLogoIconView.setOnClickListener(this);
    }

    protected void loadPreferences(SharedPreferences sharedPreferences) {
        post(() -> {
            mShowAssistant = sharedPreferences.getBoolean("opa_assistant", true);
            mLogoIconView.setImageDrawable(getIcon());
            mMicIconView.setVisibility(sharedPreferences.getBoolean("opa_enabled", true) ? View.VISIBLE : View.GONE);
            mMicIconView.setImageDrawable(getMicIcon());
            mUseTwoBubbles = useTwoBubbles();
            mRadius = Utilities.getZimPrefs(getContext()).getSearchBarRadius();
            invalidate();
        });
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "opa_enabled":
            case "opa_assistant":
            case "pref_bubbleSearchStyle":
                loadPreferences(sharedPreferences);
        }
        if (key.equals("pref_searchbarRadius")) {
            mShadowBitmap = null;
            loadPreferences(sharedPreferences);
        }
    }

    protected Drawable getMicIcon() {
        boolean colored = prefs.getDockColoredGoogle();
        if (prefs.getVoiceSearchIcon()) {
            mMicIconView.setVisibility(View.VISIBLE);
            return getColoredMicIcon(colored);
        } else {
            mMicIconView.setVisibility(View.GONE);
            return null;
        }
    }

    protected Drawable getColoredMicIcon(boolean colored) {
        SearchProvider provider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
        if (mShowAssistant && provider.getSupportsAssistant()) {
            return provider.getAssistantIcon(colored);
        } else if (provider.getSupportsVoiceSearch()) {
            return provider.getVoiceIcon(colored);
        } else {
            mMicIconView.setVisibility(GONE);
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    public boolean useTwoBubbles() {
        return mMicIconView.getVisibility() == View.VISIBLE && Utilities.getZimPrefs(mActivity).getDualBubbleSearch();
    }

    @Override
    public void onSearchProviderChanged() {
        loadPreferences(Utilities.getPrefs(getContext()));
    }
}