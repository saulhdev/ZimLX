package org.zimmob.zimlx.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.IconLabelItem;

import java.util.ArrayList;
import java.util.List;

public class DesktopOptionView extends FrameLayout {

    private RecyclerView[] _actionRecyclerViews = new RecyclerView[2];
    private FastItemAdapter<IconLabelItem>[] _actionAdapters = new FastItemAdapter[2];
    private DesktopOptionViewListener _desktopOptionViewListener;

    public DesktopOptionView(@NonNull Context context) {
        super(context);
        init();
    }

    public DesktopOptionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DesktopOptionView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDesktopOptionViewListener(DesktopOptionViewListener desktopOptionViewListener) {
        _desktopOptionViewListener = desktopOptionViewListener;
    }

    public void updateHomeIcon(final boolean home) {
        post(() -> {
            if (home) {
                _actionAdapters[0].getAdapterItem(2).setIcon(getContext().getDrawable(R.drawable.ic_home_black_36dp));
            } else {
                _actionAdapters[0].getAdapterItem(2).setIcon(getContext().getDrawable(R.drawable.ic_home_white_36dp));
            }
            _actionAdapters[0].notifyAdapterItemChanged(1);
        });
    }

    public void updateLockIcon(final boolean lock) {
        if (_actionAdapters.length == 0) return;
        if (_actionAdapters[0].getAdapterItemCount() == 0) return;
        post(() -> {
            if (lock) {
                _actionAdapters[1].getAdapterItem(2).setIcon(getContext().getDrawable(R.drawable.ic_lock_white_36dp));
            } else {
               _actionAdapters[1].getAdapterItem(2).setIcon(getContext().getDrawable(R.drawable.ic_lock_open_white_36dp));
            }
            _actionAdapters[1].notifyAdapterItemChanged(2);
        });
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
        return insets;
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        final int paddingHorizontal = Tool.dp2px(42, getContext());
        final Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");

        _actionAdapters[0] = new FastItemAdapter<>();
        _actionAdapters[1] = new FastItemAdapter<>();

        _actionRecyclerViews[0] = createRecyclerView(_actionAdapters[0], Gravity.TOP | Gravity.CENTER_HORIZONTAL, paddingHorizontal);
        _actionRecyclerViews[1] = createRecyclerView(_actionAdapters[1], Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, paddingHorizontal);
        final com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem> clickListener = (v, adapter, item, position) -> {
            if (_desktopOptionViewListener != null) {
                int id = (int) item.getIdentifier();
                switch (id) {
                    case R.string.home:
                        updateHomeIcon(true);
                        _desktopOptionViewListener.onSetPageAsHome();
                        break;
                    case R.string.remove:
                        if (!Setup.appSettings().isDesktopLock()) {
                            _desktopOptionViewListener.onRemovePage();
                        } else {
                            Tool.toast(getContext(), "Desktop is locked.");
                        }
                        break;
                    case R.string.widget:
                        if (!Setup.appSettings().isDesktopLock()) {
                            _desktopOptionViewListener.onPickWidget();
                        } else {
                            Tool.toast(getContext(), "Desktop is locked.");
                        }
                        break;
                    case R.string.action:
                        if (!Setup.appSettings().isDesktopLock()) {
                            _desktopOptionViewListener.onPickDesktopAction();
                        } else {
                            Tool.toast(getContext(), "Desktop is locked.");
                        }
                        break;
                    case R.string.lock:
                        Setup.appSettings().setDesktopLock(!Setup.appSettings().isDesktopLock());
                        updateLockIcon(Setup.appSettings().isDesktopLock());
                        break;
                    case R.string.add_left:
                        _desktopOptionViewListener.onAddPage(0);
                        break;
                    case R.string.add_right:
                        _desktopOptionViewListener.onAddPage(1);
                        break;
                    case R.string.settings:
                        _desktopOptionViewListener.onLaunchSettings();
                        break;
                    default:
                        return false;
                }
                return true;
            }
            return false;
        };

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int itemWidth = (getWidth() - 2 * paddingHorizontal) / 4;
                initItems(typeface, clickListener, itemWidth);
            }
        });
    }

    private void initItems(final Typeface typeface, final com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem> clickListener, int itemWidth) {
        List<IconLabelItem> itemsTop = new ArrayList<>();
        itemsTop.add(createItem(R.drawable.ic_add_to_queue_white_36dp, R.string.add_left, typeface, itemWidth));
        itemsTop.add(createItem(R.drawable.ic_delete_white_36dp, R.string.remove, typeface, itemWidth));
        itemsTop.add(createItem(R.drawable.ic_home_white_36dp, R.string.home, typeface, itemWidth));
        itemsTop.add(createItem(R.drawable.ic_add_to_queue_white_36dp, R.string.add_right, typeface, itemWidth));

        _actionAdapters[0].set(itemsTop);
        _actionAdapters[0].withOnClickListener(clickListener);

        List<IconLabelItem> itemsBottom = new ArrayList<>();
        itemsBottom.add(createItem(R.drawable.ic_dashboard_white_36dp, R.string.widget, typeface, itemWidth));
        itemsBottom.add(createItem(R.drawable.ic_launch_white_36dp, R.string.action, typeface, itemWidth));
        itemsBottom.add(createItem(R.drawable.ic_lock_open_white_36dp, R.string.lock, typeface, itemWidth));
        itemsBottom.add(createItem(R.drawable.ic_settings_launcher_white_36dp, R.string.settings, typeface, itemWidth));
        _actionAdapters[1].set(itemsBottom);
        _actionAdapters[1].withOnClickListener(clickListener);

        ((MarginLayoutParams) ((View) _actionRecyclerViews[0].getParent()).getLayoutParams()).topMargin = Tool.dp2px(Setup.appSettings().getSearchBarEnable() ? 36 : 4, getContext());
    }

    private RecyclerView createRecyclerView(FastAdapter adapter, int gravity, int paddingHorizontal) {
        RecyclerView actionRecyclerView = new RecyclerView(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        actionRecyclerView.setClipToPadding(false);
        actionRecyclerView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        actionRecyclerView.setLayoutManager(linearLayoutManager);
        actionRecyclerView.setAdapter(adapter);
        actionRecyclerView.setOverScrollMode(OVER_SCROLL_ALWAYS);
        LayoutParams actionRecyclerViewLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionRecyclerViewLP.gravity = gravity;

        addView(actionRecyclerView, actionRecyclerViewLP);
        return actionRecyclerView;
    }

    private IconLabelItem createItem(int icon, int label, Typeface typeface, int width) {
        return new IconLabelItem(getContext(), icon, getContext().getString(label), -1)
                .withIdentifier(label)
                .withOnClickListener(null)
                .withTextColor(Color.WHITE)
                .withIconPadding(getContext(), 0)
                .withIconGravity(Gravity.TOP)
                .withGravity(Gravity.CENTER)
                .withMatchParent(false)
                .withWidth(width)
                .withTypeface(typeface)
                .withTextGravity(Gravity.CENTER);
    }

    public interface DesktopOptionViewListener {
        void onRemovePage();

        void onAddPage(int option);

        void onSetPageAsHome();

        void onLaunchSettings();

        void onPickDesktopAction();

        void onPickWidget();
    }
}
