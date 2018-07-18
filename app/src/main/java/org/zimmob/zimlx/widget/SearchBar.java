package org.zimmob.zimlx.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.dragndrop.DragAction;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.CircleDrawable;
import org.zimmob.zimlx.viewutil.IconLabelItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class SearchBar extends FrameLayout {
    public TextView searchClock;
    private AppCompatImageView switchButton;
    private AppCompatImageView searchButton;
    public AppCompatEditText searchInput;
    private boolean expanded;
    private boolean searchInternetEnabled = true;
    private int searchClockTextSize = 28;
    private float searchClockSubTextFactor = 0.5f;
    private Mode mode = Mode.DateAll;
    public CallBack callback;
    private FastItemAdapter<IconLabelItem> adapter = new FastItemAdapter<>();
    private CircleDrawable icon;
    private static final long ANIM_TIME = 200;
    public RecyclerView searchRecycler;
    private CardView searchCardContainer;

    public SearchBar(@NonNull Context context) {
        super(context);
        init();
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private SearchBar setSearchInternetEnabled(Boolean enabled) {
        searchInternetEnabled = enabled;
        return this;
    }

    private SearchBar setSearchClockTextSize(int size) {
        searchClockTextSize = size;
        if (searchClock != null) {
            searchClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float) searchClockTextSize);
        }
        return this;
    }

    private SearchBar setSearchClockSubTextFactor(float factor) {
        searchClockSubTextFactor = factor;
        return this;
    }

    private SearchBar setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public void setCallback(CallBack callback) {
        this.callback = callback;
    }

    public boolean collapse() {
        if (!expanded) {
            return false;
        }
        searchButton.callOnClick();
        return !expanded;
    }

    private void clearFilter() {
        adapter.filter(" ");
    }

    private void collapseInternal() {
        if (callback != null) {
            callback.onCollapse();
        }
        icon.setIcon(getResources().getDrawable(R.drawable.ic_search_light_24dp));
        Tool.visibleViews(ANIM_TIME, searchClock);
        Tool.goneViews(ANIM_TIME, searchCardContainer, searchRecycler, switchButton);
        searchInput.getText().clear();
    }

    private void expandInternal() {
        if (callback != null) {
            callback.onExpand();
        }
        if (Setup.appSettings().isResetSearchBarOnOpen()) {
            RecyclerView.LayoutManager lm = searchRecycler.getLayoutManager();
            if (lm instanceof LinearLayoutManager) {
                ((LinearLayoutManager) searchRecycler.getLayoutManager()).scrollToPositionWithOffset(0, 0);
            } else if (lm instanceof GridLayoutManager) {
                ((GridLayoutManager) searchRecycler.getLayoutManager()).scrollToPositionWithOffset(0, 0);
            }
        }
        icon.setIcon(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
        Tool.visibleViews(ANIM_TIME, searchCardContainer, searchRecycler, switchButton);
        Tool.goneViews(ANIM_TIME, searchClock);
    }

    private void updateSwitchIcon() {
        switchButton.setImageResource(Setup.appSettings().isSearchUseGrid() ? R.drawable.ic_view_comfy_white_24dp : R.drawable.ic_view_list_white_24dp);
    }

    private void updateRecyclerViewLayoutManager() {
        for (int i = 0; i < adapter.getAdapterItems().size(); i++) {
            IconLabelItem item = adapter.getAdapterItem(i);
            updateItemGravity(i, item);
        }
        adapter.notifyAdapterDataSetChanged();
        int gridSize = Setup.appSettings().isSearchUseGrid() ? Setup.appSettings().getSearchGridSize() : 1;
        if (gridSize == 1) {
            searchRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        } else {
            searchRecycler.setLayoutManager(new GridLayoutManager(getContext(), gridSize, GridLayoutManager.VERTICAL, false));
        }
        searchRecycler.getLayoutManager().setAutoMeasureEnabled(false);
    }

    private void initRecyclerView() {
        searchRecycler = new RecyclerView(getContext());
        searchRecycler.setItemAnimator(null);
        searchRecycler.setVisibility(View.GONE);
        searchRecycler.setAdapter(adapter);
        searchRecycler.setClipToPadding(false);
        searchRecycler.setHasFixedSize(true);
        updateRecyclerViewLayoutManager();
    }

    private void updateItemGravity(int position, IconLabelItem item) {
        if (position == 0) {
            item.withTextGravity(Setup.appSettings().isSearchUseGrid() ? Gravity.CENTER_HORIZONTAL : Gravity.RIGHT);
        } else {
            item.withTextGravity(Setup.appSettings().isSearchUseGrid() ? Gravity.CENTER_HORIZONTAL : Gravity.CENTER_VERTICAL);
        }
        item.withIconGravity(Setup.appSettings().isSearchUseGrid() ? Gravity.TOP : Gravity.LEFT);
    }

    private void startApp(Context context, App app) {
        Tool.startApp(context, app);
    }

    public void updateClock() {
        AppSettings appSettings = AppSettings.get();
        if (!appSettings.isSearchBarTimeEnabled()) {
            searchClock.setText("");
            return;
        }

        if (searchClock != null) {
            searchClock.setTextColor(appSettings.getDesktopDateTextColor());
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        SimpleDateFormat sdf = mode.sdf;

        int mode = appSettings.getDesktopDateMode();
        if (mode >= 0 && mode < Mode.getCount()) {
            sdf = Mode.getById(mode).sdf;
            if (mode == 0) {
                sdf = appSettings.getUserDateFormat();
            }
        }

        if (sdf == null) {
            sdf = Setup.appSettings().getUserDateFormat();
        }
        String text = sdf.format(calendar.getTime());
        String[] lines = text.split("\n");
        Spannable span = new SpannableString(text);
        span.setSpan(new RelativeSizeSpan(searchClockSubTextFactor), lines[0].length() + 1, lines[0].length() + 1 + lines[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchClock.setText(span);
    }

    public AppCompatImageView getSearchButton() {
        return searchButton;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            int topInset = insets.getSystemWindowInsetTop();
            setPadding(getPaddingLeft(), topInset + Tool.dp2px(10, getContext()), getPaddingRight(), getPaddingBottom());
        }
        return insets;
    }

    public enum Mode {
        DateAll(1, new SimpleDateFormat("MMMM dd'\n'EEEE',' yyyy", Locale.getDefault())),
        DateNoYearAndTime(2, new SimpleDateFormat("MMMM dd'\n'HH':'mm", Locale.getDefault())),
        DateAllAndTime(3, new SimpleDateFormat("MMMM dd',' yyyy'\n'HH':'mm", Locale.getDefault())),
        TimeAndDateAll(4, new SimpleDateFormat("HH':'mm'\n'MMMM dd',' yyyy", Locale.getDefault())),
        Custom(0, null);

        SimpleDateFormat sdf;
        int id;

        Mode(int id, SimpleDateFormat sdf) {
            this.id = id;
            this.sdf = sdf;
        }

        static Mode getById(int id) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].getId() == id)
                    return values()[i];
            }
            throw new RuntimeException("ID not found!");
        }

        int getId() {
            return id;
        }

        static int getCount() {
            return values().length;
        }
    }

    public interface CallBack {
        void onInternetSearch(String string);

        void onExpand();

        void onCollapse();
    }

    private void init() {
        int dp1 = Tool.dp2px(1, getContext());
        int iconMarginOutside = dp1 * 16;
        int iconMarginTop = dp1 * 13;
        int searchTextHorizontalMargin = dp1 * 8;
        int searchTextMarginTop = dp1 * 4;
        int iconSize = dp1 * 30;
        int iconPadding = dp1 * 6;

        searchClock = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.view_search_clock, this, false);
        searchClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, searchClockTextSize);
        LayoutParams clockParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clockParams.setMargins(iconMarginOutside, 0, 0, 0);
        clockParams.gravity = Gravity.START;

        switchButton = new AppCompatImageView(getContext());
        updateSwitchIcon();
        switchButton.setOnClickListener(view -> {
            Setup.appSettings().setSearchUseGrid(!Setup.appSettings().isSearchUseGrid());
            updateSwitchIcon();
            updateRecyclerViewLayoutManager();
        });
        switchButton.setVisibility(View.GONE);
        switchButton.setPadding(0, iconPadding, 0, iconPadding);

        LayoutParams switchButtonParams = null;
        switchButtonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switchButtonParams.setMargins(iconMarginOutside / 2, 0, 0, 0);
        switchButtonParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        searchButton = new AppCompatImageView(getContext());
        icon = new CircleDrawable(getContext(), getResources().getDrawable(R.drawable.ic_search_light_24dp), Color.BLACK);
        searchButton = new AppCompatImageView(getContext());
        searchButton.setImageDrawable(icon);
        searchButton.setOnClickListener(v -> {
            if (expanded && searchInput.getText().length() > 0) {
                searchInput.getText().clear();
                return;
            }
            expanded = !expanded;
            if (expanded) {
                expandInternal();
            } else {
                collapseInternal();
            }
        });

        LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, iconMarginTop, iconMarginOutside, 0);
        buttonParams.gravity = Gravity.END;

        searchCardContainer = new CardView(getContext());
        searchCardContainer.setCardBackgroundColor(Color.TRANSPARENT);
        searchCardContainer.setVisibility(View.GONE);
        searchCardContainer.setRadius(0);
        searchCardContainer.setCardElevation(0);
        searchCardContainer.setContentPadding(dp1 * 8, dp1 * 4, dp1 * 4, dp1 * 4);

        searchInput = new AppCompatEditText(getContext());
        searchInput.setBackground(null);
        searchInput.setHint(R.string.search_hint);
        searchInput.setHintTextColor(Color.WHITE);
        searchInput.setTextColor(Color.WHITE);
        searchInput.setSingleLine();
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchInput.setOnKeyListener((v, keyCode, event) -> {
            if ((event != null) && (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                callback.onInternetSearch(searchInput.getText().toString());
                searchInput.getText().clear();
                return true;
            }
            return false;
        });

        LayoutParams inputCardParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputCardParams.setMargins(10, searchTextMarginTop, 10, 0);

        LayoutParams inputParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(iconMarginOutside + iconSize, 0, 0, 0);

        searchCardContainer.addView(switchButton, switchButtonParams);
        searchCardContainer.addView(searchInput, inputParams);
        initRecyclerView();
        Setup.appLoader().addUpdateListener(apps -> {
            adapter.clear();
            if (Setup.appSettings().getSearchBarShouldShowHiddenApps()) {
                apps = Setup.appLoader().getAllApps(getContext(), true);
            }
            List<IconLabelItem> items = new ArrayList<>();
            if (searchInternetEnabled) {
                items.add(new IconLabelItem(getContext(), R.drawable.ic_search_light_24dp, R.string.search_online)
                        .withIconGravity(Setup.appSettings().isSearchUseGrid() ? Gravity.TOP : Gravity.LEFT)
                        .withTextGravity(Setup.appSettings().isSearchUseGrid() ? Gravity.CENTER_HORIZONTAL : Gravity.RIGHT)
                        .withOnClickListener(v -> {
                            callback.onInternetSearch(searchInput.getText().toString());
                            searchInput.getText().clear();
                        })
                        .withTextColor(Color.WHITE)
                        .withIconPadding(getContext(), 15)
                        .withBold(true)
                        .withMatchParent(true));
            }
            for (int i = 0; i < apps.size(); i++) {
                final App app = apps.get(i);
                final int finalI = i;
                items.add(new IconLabelItem(getContext(), app.getIconProvider(), app.getLabel(), app.getUniversalLabel(), 36)
                        .withIconGravity(Setup.appSettings().isSearchUseGrid() ? Gravity.TOP : Gravity.LEFT)
                        .withTextGravity(Setup.appSettings().isSearchUseGrid() ? Gravity.CENTER_HORIZONTAL : Gravity.CENTER_VERTICAL)
                        .withOnClickListener(v -> {
                            startApp(v.getContext(), app);
                        })
                        .withOnLongClickListener(AppItemView.Builder.getLongClickDragAppListener(Item.newAppItem(app), DragAction.Action.SEARCH_RESULT, new AppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                if (finalI == -1) return true;
                                expanded = !expanded;
                                collapseInternal();
                                return false;
                            }

                            @Override
                            public void afterDrag(View view) {
                            }
                        }))
                        .withTextColor(Color.WHITE)
                        .withMatchParent(true)
                        .withIconPadding(getContext(), 15)
                        .withMaxTextLines(Setup.appSettings().getSearchLabelLines()));
            }
            adapter.set(items);
            return false;
        });

        adapter.getItemFilter().withFilterPredicate((IItemAdapter.Predicate<IconLabelItem>) (item, constraint) -> {
            updateItemGravity(item._label == getContext().getString(R.string.search_online) ? 0 : 1, item);

            if (item._label.equals(getContext().getString(R.string.search_online))) {
                return false;
            }

            if (constraint.length() == 0) {
                return true;
            }

            String s = constraint.toString().toLowerCase();
            if (item._label.toLowerCase().contains(s)) {
                return true;
            }

            if (item._searchInfo != null && item._searchInfo.toLowerCase().contains(s)) {
                return true;
            }

            return false;
        });

        final LayoutParams recyclerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(searchClock, clockParams);
        addView(searchRecycler, recyclerParams);
        addView(searchCardContainer, inputCardParams);
        addView(searchButton, buttonParams);

        searchInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                searchInput.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int marginTop = Tool.dp2px(56, getContext()) + searchInput.getHeight();
                int marginBottom = Desktop._bottomInset;
                recyclerParams.setMargins(0, marginTop, 0, marginBottom);
                recyclerParams.height = ((View) getParent()).getHeight() - marginTop - marginBottom / 2;
                searchRecycler.setLayoutParams(recyclerParams);
                searchRecycler.setPadding(0, 0, 0, (int) (marginBottom * 1.5f));
            }
        });
    }
}