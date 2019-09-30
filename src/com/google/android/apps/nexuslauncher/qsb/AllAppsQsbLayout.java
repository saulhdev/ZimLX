package com.google.android.apps.nexuslauncher.qsb;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.google.android.apps.nexuslauncher.search.SearchThread;

import org.zimmob.zimlx.ZimPreferences;
import org.zimmob.zimlx.globalsearch.SearchProvider;
import org.zimmob.zimlx.globalsearch.SearchProviderController;
import org.zimmob.zimlx.globalsearch.providers.AppSearchSearchProvider;
import org.zimmob.zimlx.globalsearch.providers.GoogleSearchProvider;
import org.zimmob.zimlx.globalsearch.providers.web.WebSearchProvider;
import org.zimmob.zimlx.theme.ThemeManager;

import java.util.Objects;

public class AllAppsQsbLayout extends AbstractQsbLayout implements SearchUiManager, o {

    private final k Ds;
    private final int Dt;
    private int mShadowAlpha;
    private Bitmap Dv;
    private boolean mUseFallbackSearch;
    private FallbackAppsSearchView mFallback;
    public float Dy;
    private TextView mHint;
    private AllAppsContainerView mAppsView;
    boolean mDoNotRemoveFallback;
    private ZimPreferences prefs;
    private int mForegroundColor;
    private int mBackgroundColor;
    private Context mContext;

    private final boolean mLowPerformanceMode;

    public AllAppsQsbLayout(Context context) {
        this(context, null);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShadowAlpha = 0;
        setOnClickListener(this);
        this.Ds = k.getInstance(context);
        this.Dt = getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        this.Dy = getResources().getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset);
        setClipToPadding(false);
        mLowPerformanceMode = prefs.getLowPerformanceMode();
        applyTheme();
    }

    private void applyTheme() {
        prefs = ZimPreferences.Companion.getInstanceNoCreate();
        mForegroundColor = prefs.getAccentColor();
        boolean themeBlack = ThemeManager.Companion.isBlack(ThemeManager.Companion.getInstance(mContext).getCurrentFlags());
        boolean themeDark = ThemeManager.Companion.isDark(ThemeManager.Companion.getInstance(mContext).getCurrentFlags()) ||
                ThemeManager.Companion.isDarkText(ThemeManager.Companion.getInstance(mContext).getCurrentFlags());

        int theme = prefs.getLauncherTheme();
        if (themeBlack)
            theme = 12;
        else if (themeDark)
            theme = 4;

        switch (theme) {
            case 0: //light theme
                mBackgroundColor = mContext.getResources().getColor(R.color.qsb_background_drawer_default);
                break;

            case 4://dark theme
                mBackgroundColor = mContext.getResources().getColor(R.color.qsb_background_drawer_dark);
                break;

            case 12://black theme
                mBackgroundColor = mContext.getResources().getColor(R.color.qsb_background_drawer_dark_bar);
                break;

            default:
                mBackgroundColor = mContext.getResources().getColor(R.color.qsb_background_drawer_default);

        }

        ay(mBackgroundColor);
        az(this.Dc);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mHint = findViewById(R.id.qsb_hint);
    }

    public void setInsets(Rect rect) {
        c(Utilities.getDevicePrefs(getContext()));
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.topMargin = getTopMargin(rect);
        requestLayout();
        if (mActivity.getDeviceProfile().isVerticalBarLayout()) {
            mActivity.getAllAppsController().setScrollRangeDelta(0);
        } else {
            float delta = HotseatQsbWidget.getBottomMargin(mActivity) + Dy;
            if (!prefs.getDockHide()) {
                delta += mlp.height + mlp.topMargin;
                if (!prefs.getDockSearchBar()) {
                    delta -= mlp.height;
                    delta -= mlp.topMargin;
                    delta -= mlp.bottomMargin;
                    delta += Dy;
                }
            } else {
                delta -= mActivity.getResources().getDimensionPixelSize(R.dimen.vertical_drag_handle_size);
            }
            mActivity.getAllAppsController().setScrollRangeDelta(Math.round(delta));
        }
    }

    public int getTopMargin(Rect rect) {
        return Math.max((int) (-this.Dy), rect.top - this.Dt);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        dN();
        Ds.a(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if (key.equals("pref_allAppsGoogleSearch")) {
            loadPreferences(sharedPreferences);
        }
    }

    @Override
    protected Drawable getIcon(boolean colored) {
        if (prefs.getAllAppsGlobalSearch()) {
            return super.getIcon(colored);
        } else {
            return new AppSearchSearchProvider(getContext()).getIcon(colored);
        }
    }

    @Override
    protected boolean logoCanOpenFeed() {
        return super.logoCanOpenFeed() && prefs.getAllAppsGlobalSearch();
    }

    @Override
    protected Drawable getMicIcon(boolean colored) {
        if (prefs.getAllAppsGlobalSearch()) {
            mMicIconView.setVisibility(View.VISIBLE);
            return super.getMicIcon(colored);
        } else {
            mMicIconView.setVisibility(View.GONE);
            return null;
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Ds.b(this);
    }

    protected final int aA(int i) {
        if (this.mActivity.getDeviceProfile().isVerticalBarLayout()) {
            return (i - this.mAppsView.getActiveRecyclerView().getPaddingLeft()) - this.mAppsView
                    .getActiveRecyclerView().getPaddingRight();
        }
        View view = this.mActivity.getHotseat().getLayout();
        return (i - view.getPaddingLeft()) - view.getPaddingRight();
    }

    public final void initialize(AllAppsContainerView allAppsContainerView) {
        this.mAppsView = allAppsContainerView;
        mAppsView.addElevationController(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                setShadowAlpha(((BaseRecyclerView) recyclerView).getCurrentScrollY());
            }
        });
        mAppsView.setRecyclerViewVerticalFadingEdgeEnabled(!mLowPerformanceMode);
    }

    public final void dM() {
        dN();
        invalidate();
    }

    private void dN() {
        az(this.Dc);
        h(this.Ds.micStrokeWidth());
        this.Dh = this.Ds.hintIsForAssistant();
        mUseTwoBubbles = useTwoBubbles();
        setHintText(this.Ds.hintTextValue(), this.mHint);
        dH();
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view == this) {
            startSearch("", this.Di);
        }
    }

    public final void l(String str) {
        startSearch(str, 0);
    }

    @Override
    public void startSearch() {
        post(() -> startSearch("", Di));
    }

    @Override
    public final void startSearch(String str, int i) {
        SearchProviderController controller = SearchProviderController.Companion
                .getInstance(mActivity);
        SearchProvider provider = controller.getSearchProvider();
        if (shouldUseFallbackSearch(provider)) {
            searchFallback(str);
        } else if (controller.isGoogle()) {
            final ConfigBuilder f = new ConfigBuilder(this, true);
            if (!Objects.requireNonNull(mActivity.getGoogleNow()).startSearch(f.build(), f.getExtras())) {
                searchFallback(str);
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

    private boolean shouldUseFallbackSearch() {
        SearchProviderController controller = SearchProviderController.Companion
                .getInstance(mActivity);
        SearchProvider provider = controller.getSearchProvider();
        return shouldUseFallbackSearch(provider);
    }

    private boolean shouldUseFallbackSearch(SearchProvider provider) {
        return !Utilities
                .getZimPrefs(getContext()).getAllAppsGlobalSearch()
                || provider instanceof AppSearchSearchProvider
                || provider instanceof WebSearchProvider
                || (!Utilities.ATLEAST_NOUGAT && provider instanceof GoogleSearchProvider);
    }

    public void searchFallback(String query) {
        ensureFallbackView();
        mFallback.setText(query);
        mFallback.showKeyboard();
    }

    public final void resetSearch() {
        setShadowAlpha(0);
        if (mUseFallbackSearch) {
            resetFallbackView();
        } else if (!mDoNotRemoveFallback) {
            removeFallbackView();
        }
    }

    private void ensureFallbackView() {
        setOnClickListener(null);
        mFallback = (FallbackAppsSearchView) this.mActivity.getLayoutInflater()
                .inflate(R.layout.all_apps_google_search_fallback, this, false);
        AllAppsContainerView allAppsContainerView = this.mAppsView;
        mFallback.DJ = this;
        mFallback.mApps = allAppsContainerView.getApps();
        mFallback.mAppsView = allAppsContainerView;
        mFallback.DI.initialize(new SearchThread(mFallback.getContext()), mFallback,
                Launcher.getLauncher(mFallback.getContext()), mFallback);
        addView(this.mFallback);
        mFallback.setTextColor(mForegroundColor);
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

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        View view = (View) getParent();
        setTranslationX((float) ((view.getPaddingLeft() + (
                (((view.getWidth() - view.getPaddingLeft()) - view.getPaddingRight()) - (i3 - i))
                        / 2)) - i));
    }

    public void draw(Canvas canvas) {
        if (this.mShadowAlpha > 0) {
            if (this.Dv == null) {
                this.Dv = c(
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset),
                        0);
            }
            this.mShadowHelper.paint.setAlpha(this.mShadowAlpha);
            a(this.Dv, canvas);
            this.mShadowHelper.paint.setAlpha(255);
        }
        super.draw(canvas);
    }

    final void setShadowAlpha(int i) {
        i = Utilities.boundToRange(i, 0, 255);
        if (this.mShadowAlpha != i) {
            this.mShadowAlpha = i;
            invalidate();
        }
    }

    protected final boolean dK() {
        if (this.mFallback != null) {
            return false;
        }
        return super.dK();
    }

    protected final void c(SharedPreferences sharedPreferences) {
        if (mUseFallbackSearch) {
            removeFallbackView();
            this.mUseFallbackSearch = false;
            if (this.mUseFallbackSearch) {
                ensureFallbackView();
            }
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent keyEvent) {

    }

    @Nullable
    @Override
    protected String getClipboardText() {
        return shouldUseFallbackSearch() ? super.getClipboardText() : null;
    }
}