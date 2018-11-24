package com.android.launcher3.allapps;

import android.content.Intent;
import android.view.View;

import com.android.launcher3.anim.SpringAnimationHandler;

public class AllAppsPagedAdapter {
    public static final String TAG = "AppsGridAdapter";

    // A normal icon
    public static final int VIEW_TYPE_ICON = 1 << 1;
    // A prediction icon
    public static final int VIEW_TYPE_PREDICTION_ICON = 1 << 2;
    // The message shown when there are no filtered results
    public static final int VIEW_TYPE_EMPTY_SEARCH = 1 << 3;
    // The message to continue to a market search when there are no filtered results
    public static final int VIEW_TYPE_SEARCH_MARKET = 1 << 4;

    // We use various dividers for various purposes.  They share enough attributes to reuse layouts,
    // but differ in enough attributes to require different view types

    // A divider that separates the apps list and the search market button
    public static final int VIEW_TYPE_SEARCH_MARKET_DIVIDER = 1 << 5;
    // The divider that separates prediction icons from the app list
    public static final int VIEW_TYPE_PREDICTION_DIVIDER = 1 << 6;
    public static final int VIEW_TYPE_APPS_LOADING_DIVIDER = 1 << 7;
    public static final int VIEW_TYPE_DISCOVERY_ITEM = 1 << 8;
    public static final int VIEW_TYPE_WORK_TAB_FOOTER = 1 << 9;
    // Common view type masks
    public static final int VIEW_TYPE_MASK_DIVIDER = VIEW_TYPE_SEARCH_MARKET_DIVIDER
            | VIEW_TYPE_PREDICTION_DIVIDER;
    public static final int VIEW_TYPE_MASK_ICON = VIEW_TYPE_ICON
            | VIEW_TYPE_PREDICTION_ICON;
    public static final int VIEW_TYPE_MASK_CONTENT = VIEW_TYPE_MASK_ICON
            | VIEW_TYPE_DISCOVERY_ITEM;
    public static final int VIEW_TYPE_MASK_HAS_SPRINGS = VIEW_TYPE_MASK_ICON
            | VIEW_TYPE_PREDICTION_DIVIDER;
    /*private final Launcher mLauncher;
    private final LayoutInflater mLayoutInflater;
    private final AlphabeticalAppsList mApps;
    private final GridLayoutManager mGridLayoutMgr;
    private final AllAppsGridAdapter.GridSpanSizer mGridSizer;
    private final View.OnClickListener mIconClickListener;
    private final View.OnLongClickListener mIconLongClickListener;*/
    private int mAppsPerRow;
    private AllAppsGridAdapter.BindViewCallback mBindViewCallback;
    private View.OnFocusChangeListener mIconFocusListener;
    // The text to show when there are no search results and no market search handler.
    private String mEmptySearchMessage;
    // The intent to send off to the market app, updated each time the search query changes.
    private Intent mMarketSearchIntent;
    private SpringAnimationHandler<AllAppsGridAdapter.ViewHolder> mSpringAnimationHandler;
}
