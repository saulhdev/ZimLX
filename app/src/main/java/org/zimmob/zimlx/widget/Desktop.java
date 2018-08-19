package org.zimmob.zimlx.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.dragndrop.DragAction.Action;
import org.zimmob.zimlx.dragndrop.DragHandler;
import org.zimmob.zimlx.folder.Folder;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.model.Item.Type;
import org.zimmob.zimlx.pageindicator.PageIndicator;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.DesktopGestureListener;
import org.zimmob.zimlx.viewutil.IDesktopCallback;
import org.zimmob.zimlx.viewutil.ItemViewFactory;
import org.zimmob.zimlx.viewutil.SmoothPagerAdapter;
import org.zimmob.zimlx.widget.CellContainer.DragState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.championswimmer.sfg.lib.SimpleFingerGestures;
import in.championswimmer.sfg.lib.SimpleFingerGestures.OnFingerGestureListener;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class Desktop extends SmoothViewPager implements IDesktopCallback<View> {
    private static final Companion _companion = new Companion();
    public static int _bottomInset;
    public static int _topInset;
    private final Point _coordinate = new Point(-1, -1);

    @Nullable
    private OnDesktopEditListener _desktopEditListener;
    private HomeActivity _home;
    private boolean _inEditMode;
    private int _pageCount;
    private PageIndicator _pageIndicator;

    @NonNull
    private final List<CellContainer> _pages = new ArrayList<>();
    private final Point _previousDragPoint = new Point();

    private View _previousItemView;
    private int _previousPage;

    public static boolean handleOnDropOver(Item dropItem, Item item, View itemView, CellContainer parent, int page, Config.ItemPosition itemPosition, IDesktopCallback<?> callback) {
        if (item != null) {
            if (dropItem != null) {
                Type type = item.getType();
                if (type != null) {
                    switch (type) {
                        case APP:
                        case SHORTCUT:
                            if (Type.APP.equals(dropItem.getType()) || Type.SHORTCUT.equals(dropItem.getType())) {
                                parent.removeView(itemView);
                                Item group = Item.newGroupItem();
                                group.getGroupItems().add(item);
                                group.getGroupItems().add(dropItem);
                                group.setX(item.getX());
                                group.setY(item.getY());
                                HomeActivity.companion.getDb().saveItem(dropItem, page, itemPosition);
                                HomeActivity.companion.getDb().saveItem(item, Config.ItemState.Hidden);
                                HomeActivity.companion.getDb().saveItem(dropItem, Config.ItemState.Hidden);
                                HomeActivity.companion.getDb().saveItem(group, page, itemPosition);
                                callback.addItemToPage(group, page);
                                HomeActivity homeActivity = HomeActivity.companion.getLauncher();
                                if (homeActivity != null) {
                                    homeActivity.getDesktop().consumeRevert();
                                    homeActivity.getDock().consumeRevert();
                                }
                                return true;
                            }
                        case GROUP:
                            if ((Item.Type.APP.equals(dropItem.getType()) || Type.SHORTCUT.equals(dropItem.getType())) && item.getGroupItems().size() < Folder.GroupDef.maxItem) {
                                parent.removeView(itemView);
                                item.getGroupItems().add(dropItem);
                                HomeActivity.companion.getDb().saveItem(dropItem, page, itemPosition);
                                HomeActivity.companion.getDb().saveItem(dropItem, Config.ItemState.Hidden);
                                HomeActivity.companion.getDb().saveItem(item, page, itemPosition);
                                callback.addItemToPage(item, page);
                                HomeActivity homeActivity = HomeActivity.companion.getLauncher();
                                if (homeActivity != null) {
                                    homeActivity.getDesktop().consumeRevert();
                                    homeActivity.getDock().consumeRevert();
                                }
                                return true;
                            }
                        default:
                            break;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public interface OnDesktopEditListener {
        void onDesktopEdit();

        void onFinishDesktopEdit();
    }

    @NonNull
    public final Dock getDock() {
        return findViewById(R.id.dock);
    }

    public Desktop(@NonNull Context context) {
        super(context, null);
    }

    public Desktop(@NonNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
    }

    @NonNull
    public final List<CellContainer> getPages() {
        return _pages;
    }

    @Nullable
    private OnDesktopEditListener getDesktopEditListener() {
        return _desktopEditListener;
    }

    public final void setDesktopEditListener(@Nullable OnDesktopEditListener v) {
        _desktopEditListener = v;
    }

    public final boolean getInEditMode() {
        return _inEditMode;
    }

    private void setInEditMode(boolean v) {
        _inEditMode = v;
    }

    public final int getPageCount() {
        return _pageCount;
    }

    public final boolean isCurrentPageEmpty() {
        return getCurrentPage().getChildCount() == 0;
    }

    @NonNull
    public final CellContainer getCurrentPage() {
        return _pages.get(getCurrentItem());
    }

    public final void setPageIndicator(@NonNull PageIndicator pageIndicator) {
        _pageIndicator = pageIndicator;
    }

    public final void init() {
        if (!isInEditMode()) {
            _pageCount = HomeActivity.companion.getDb().getDesktop().size();
            if (_pageCount == 0) {
                _pageCount = 1;
            }
            setCurrentItem(Setup.appSettings().getDesktopPageCurrent());
        }
    }

    public final void initDesktopNormal(@NonNull HomeActivity home) {
        setAdapter(new DesktopAdapter(this));
        if (Setup.appSettings().isDesktopShowIndicator() && _pageIndicator != null) {
            _pageIndicator.setViewPager(this);
        }
        _home = home;
        int columns = Setup.appSettings().getDesktopColumnCount();
        int rows = Setup.appSettings().getDesktopRowCount();
        List desktopItems = HomeActivity.companion.getDb().getDesktop();
        int size = desktopItems.size();
        int pageCount = 0;
        while (pageCount < size) {
            if (_pages.size() > pageCount) {
                _pages.get(pageCount).removeAllViews();
                List items = (List) desktopItems.get(pageCount);
                int size2 = items.size();
                for (int j = 0; j < size2; j++) {
                    Item item = (Item) items.get(j);
                    if (item.getX() + item.getSpanX() <= columns && item.getY() + item.getSpanY() <= rows) {
                        addItemToPage(item, pageCount);
                    }
                }
                pageCount++;
            } else {
                return;
            }
        }
    }

    public final void initDesktopShowAll(@NonNull Context context, @NonNull HomeActivity home) {
        Desktop desktop = this;
        ArrayList apps = new ArrayList();
        for (App app : Setup.appLoader().getAllApps(context, false)) {
            apps.add(Item.newAppItem(app));
        }
        int appsSize = apps.size();
        desktop._pageCount = 0;
        int columns = Setup.appSettings().getDesktopColumnCount();
        int rows = Setup.appSettings().getDesktopRowCount();
        appsSize -= columns * rows;
        while (true) {
            if (appsSize < columns * rows) {
                if (appsSize <= (-(columns * rows))) {
                    break;
                }
            }
            desktop._pageCount++;
        }
        setAdapter(new DesktopAdapter(desktop));
        if (Setup.appSettings().isDesktopShowIndicator() && desktop._pageIndicator != null) {
            desktop._pageIndicator.setViewPager(desktop);
        }
        desktop._home = home;
        for (int i = 0; i < _pageCount; i++) {
            for (int x = 0; x < columns; x++) {
                for (int y = 0; y < rows; y++) {
                    int pagePos = y * rows + x;
                    int pos = columns * rows * i + pagePos;
                    if (pos < apps.size()) {
                        Item appItem = (Item) apps.get(pos);
                        appItem.setX(x);
                        appItem.setY(y);
                        addItemToPage(appItem, i);
                    }
                }
            }
        }
    }

    public final void removeCurrentPage() {
        if (Setup.appSettings().getDesktopStyle() != DesktopMode.INSTANCE.getSHOW_ALL_APPS()) {
            _pageCount--;
            int previousPage = getCurrentItem();
            SmoothPagerAdapter adapter = getAdapter();
            ((DesktopAdapter) adapter).removePage(getCurrentItem(), true);
            for (CellContainer v : _pages) {
                v.setAlpha(0.0f);
                v.animate().alpha(1.0f);
                v.setScaleX(0.85f);
                v.setScaleY(0.85f);
                v.animateBackgroundShow();
            }
            if (_pageCount == 0) {
                addPageRight(false);
                adapter = getAdapter();
                ((DesktopAdapter) adapter).exitDesktopEditMode();
            } else {
                setCurrentItem(previousPage);
                _pageIndicator.invalidate();
            }
        }
    }

    public final void updateIconProjection(int x, int y) {
        HomeActivity homeActivity;
        DragOptionLayout dragNDropView;
        DragState state = getCurrentPage().peekItemAndSwap(x, y, _coordinate);
        if (_previousDragPoint != null && !_previousDragPoint.equals(_coordinate)) {
            homeActivity = _home;
            if (homeActivity != null) {
                dragNDropView = homeActivity.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
        }
        Objects.requireNonNull(_previousDragPoint).set(_coordinate.x, _coordinate.y);
        switch (state) {
            case CurrentNotOccupied:
                getCurrentPage().projectImageOutlineAt(_coordinate, DragHandler.cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                HomeActivity homeActivity2;
                DragOptionLayout dragNDropView2;
                for (CellContainer page : _pages) {
                    page.clearCachedOutlineBitmap();
                }
                homeActivity = HomeActivity.companion.getLauncher();
                if (homeActivity != null) {
                    dragNDropView = homeActivity.getDragNDropView();
                    action = dragNDropView.getDragAction();
                    if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (getCurrentPage().coordinateToChildView(_coordinate) instanceof AppItemView)) {
                        homeActivity2 = HomeActivity.companion.getLauncher();
                        if (homeActivity2 != null) {
                            dragNDropView2 = homeActivity2.getDragNDropView();
                            if (dragNDropView2 != null) {
                                dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) _coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) _coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
                                break;
                            }
                        }
                        break;
                    }
                }
                homeActivity2 = HomeActivity.companion.getLauncher();
                if (homeActivity2 != null) {
                    dragNDropView2 = homeActivity2.getDragNDropView();
                    //if (dragNDropView2 != null) {
                        /*if (Setup.appSettings().isDesktopShowLabel()) {
                        }*/
                    dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) _coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) _coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
                    //}
                }
                break;
            default:
                break;
        }
    }

    public void setLastItem(@NonNull Object... args) {
        View v = (View) args[1];
        _previousPage = getCurrentItem();
        _previousItemView = v;
        getCurrentPage().removeView(v);
    }

    public void revertLastItem() {
        if (_previousItemView != null) {
            SmoothPagerAdapter adapter = getAdapter();
            if (adapter.getCount() >= _previousPage && _previousPage > -1) {
                CellContainer cellContainer = _pages.get(_previousPage);
                cellContainer.addViewToGrid(_previousItemView);
                _previousItemView = null;
                _previousPage = -1;
            }
        }
    }

    public void consumeRevert() {
        _previousItemView = null;
        _previousPage = -1;
    }

    public boolean addItemToPage(@NonNull Item item, int page) {
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), this, Setup.appSettings().getDesktopIconSize());
        if (itemView == null) {
            HomeActivity.companion.getDb().deleteItem(item, true);
            return false;
        }
        item._locationInLauncher = 0;
        _pages.get(page).addViewToGrid(itemView, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());
        return true;
    }

    public boolean addItemToPoint(@NonNull Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().coordinateToLayoutParams(x, y, item.getSpanX(), item.getSpanY());
        if (positionToLayoutPrams == null) {
            return false;
        }
        item._locationInLauncher = 0;
        item.setX(positionToLayoutPrams.getX());
        item.setY(positionToLayoutPrams.getY());
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), this, Setup.appSettings().getDesktopIconSize());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            getCurrentPage().addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NonNull Item item, int x, int y) {
        item._locationInLauncher = 0;
        item.setX(x);
        item.setY(y);
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), this, Setup.appSettings().getDesktopIconSize());
        if (itemView == null) {
            return false;
        }
        getCurrentPage().addViewToGrid(itemView, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@Nullable MotionEvent ev) {
        if (ev != null && ev.getActionMasked() == 0) {
            HomeActivity homeActivity = HomeActivity.companion.getLauncher();
            if (homeActivity != null) {
                PageIndicator desktopIndicator = homeActivity.getDesktopIndicator();
                desktopIndicator.showNow();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev != null && ev.getActionMasked() == 1) {
            HomeActivity homeActivity = HomeActivity.companion.getLauncher();
            if (homeActivity != null) {
                homeActivity.getDesktopIndicator().hideDelay();
            }
        }
        return super.onTouchEvent(ev);
    }

    public void removeItem(final View view, boolean animate) {
        Tool.print("Start Removing a view from Desktop");
        if (animate) {
            view.animate().setDuration(100).scaleX(0.0f).scaleY(0.0f).withEndAction(() -> {
                Tool.print("Ok Removing a view from Desktop");
                if (getParent() != null && getParent().equals(getCurrentPage())) {
                    getCurrentPage().removeView(view);
                }
            });
        } else if (getCurrentPage().equals(view.getParent())) {
            getCurrentPage().removeView(view);
        }
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        if (!isInEditMode()) {
            HomeActivity homeActivity = HomeActivity.companion.getLauncher();
            if (homeActivity != null) {
                DragOptionLayout dragNDropView = homeActivity.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
            WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (((float) position) + offset) / ((float) (_pageCount - 1)), 0.0f);
            super.onPageScrolled(position, offset, offsetPixels);
        }
    }

    public final void addPageRight(boolean showGrid) {
        _pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        ((DesktopAdapter) adapter).addPageRight();
        setCurrentItem(previousPage + 1);
        if (!Setup.appSettings().isDesktopHideGrid()) {
            for (CellContainer cellContainer : _pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        _pageIndicator.invalidate();
    }

    public final void addPageLeft(boolean showGrid) {
        _pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        ((DesktopAdapter) adapter).addPageLeft();
        setCurrentItem(previousPage + 1, false);
        setCurrentItem(previousPage - 1);
        if (!Setup.appSettings().isDesktopHideGrid()) {
            for (CellContainer cellContainer : _pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        _pageIndicator.invalidate();
    }

    @Override
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {
        if (VERSION.SDK_INT >= 20) {
            _companion.setTopInset(insets.getSystemWindowInsetTop());
            _companion.setBottomInset(insets.getSystemWindowInsetBottom());
            HomeActivity homeActivity = HomeActivity.companion.getLauncher();
            if (homeActivity != null) {
                homeActivity.updateHomeLayout();
            }
        }
        return insets;
    }

    public static final class Companion {
        private Companion() {
        }

        private void setTopInset(int v) {
            Desktop._topInset = v;
        }

        private void setBottomInset(int v) {
            Desktop._bottomInset = v;
        }

        @Nullable
        final Item getItemFromCoordinate(@NonNull Point point, int page) {

            List pageData = HomeActivity.companion.getDb().getDesktop().get(page);
            int size = pageData.size();
            for (int i = 0; i < size; i++) {
                Item item = (Item) pageData.get(i);
                if (item.getX() == point.x && item.getY() == point.y && item.getSpanX() == 1 && item.getSpanY() == 1) {
                    return (Item) pageData.get(i);
                }
            }
            return null;
        }
    }

    public final class DesktopAdapter extends SmoothPagerAdapter {
        private MotionEvent _currentEvent;
        private final Desktop _desktop;
        private float _scaleFactor = 1.0f;
        private float _translateFactor;
        private DesktopAdapter(Desktop desktop) {
            _desktop = desktop;
            _desktop.getPages().clear();
            int count = getCount();
            for (int i = 0; i < count; i++) {
                _desktop.getPages().add(getItemLayout());
            }
        }

        @NonNull
        private OnFingerGestureListener getGestureListener() {
            return new DesktopGestureListener(_desktop, Setup.desktopGestureCallback());
        }

        private CellContainer getItemLayout() {
            Context context = _desktop.getContext();
            CellContainer layout = new CellContainer(context);
            layout.setSoundEffectsEnabled(false);
            SimpleFingerGestures mySfg = new SimpleFingerGestures();
            mySfg.setOnFingerGestureListener(getGestureListener());
            layout.setGestures(mySfg);
            layout.setOnItemRearrangeListener((from, to) -> {
                Item itemFromCoordinate = Desktop._companion.getItemFromCoordinate(from, getCurrentItem());
                if (itemFromCoordinate != null) {
                    itemFromCoordinate.setX(to.x);
                    itemFromCoordinate.setY(to.y);
                }
            });
            layout.setOnTouchListener((v, event) -> {
                _currentEvent = event;
                return false;
            });
            layout.setGridSize(Setup.appSettings().getDesktopColumnCount(), Setup.appSettings().getDesktopRowCount());
            layout.setOnClickListener(view -> {
                if (!(_desktop.getInEditMode() || _currentEvent == null)) {
                    WallpaperManager instance = WallpaperManager.getInstance(view.getContext());
                    IBinder windowToken = view.getWindowToken();
                    String str = "android.wallpaper.tap";
                    MotionEvent access$getCurrentEvent$p = _currentEvent;
                    instance.sendWallpaperCommand(windowToken, str, (int) access$getCurrentEvent$p.getX(), (int) access$getCurrentEvent$p.getY(), 0, null);
                }
                exitDesktopEditMode();
            });
            layout.setOnLongClickListener(v -> {
                enterDesktopEditMode();
                return true;
            });
            return layout;
        }

        private void addPageLeft() {
            _desktop.getPages().add(0, getItemLayout());
            notifyDataSetChanged();
        }

        private void addPageRight() {
            _desktop.getPages().add(getItemLayout());
            notifyDataSetChanged();
        }

        private void removePage(int position, boolean deleteItems) {
            if (deleteItems) {
                for (View v : _desktop.getPages().get(position).getAllCells()) {
                    Object item = v.getTag();
                    if (item instanceof Item) {
                        HomeActivity.companion.getDb().deleteItem((Item) item, true);
                    }
                }
            }
            _desktop.getPages().remove(position);
            notifyDataSetChanged();
        }

        public int getItemPosition(Object object) {
            return -2;
        }

        public int getCount() {
            return _desktop.getPageCount();
        }

        public boolean isViewFromObject(@NonNull View p1, @NonNull Object p2) {
            return p1 == p2;
        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int pos) {
            CellContainer layout = _desktop.getPages().get(pos);
            container.addView(layout);
            return layout;
        }

        private void enterDesktopEditMode() {
            _scaleFactor = 0.8f;
            _translateFactor = (float) Tool.toPx(Setup.appSettings().getSearchBarEnable() ? 20 : 40);
            for (CellContainer v : _desktop.getPages()) {
                v.setBlockTouch(true);
                v.animateBackgroundShow();
                ViewPropertyAnimator translationY = v.animate().scaleX(_scaleFactor).scaleY(_scaleFactor).translationY(_translateFactor);
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            _desktop.setInEditMode(true);
            if (_desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = _desktop.getDesktopEditListener();
                desktopEditListener.onDesktopEdit();
            }
        }

        private void exitDesktopEditMode() {
            _scaleFactor = 1.0f;
            _translateFactor = 0.0f;
            for (CellContainer v : _desktop.getPages()) {
                v.setBlockTouch(false);
                v.animateBackgroundHide();
                ViewPropertyAnimator translationY = v.animate().scaleX(_scaleFactor).scaleY(_scaleFactor).translationY(_translateFactor);
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            _desktop.setInEditMode(false);
            if (_desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = _desktop.getDesktopEditListener();
                desktopEditListener.onFinishDesktopEdit();
            }
        }
    }

    public static final class DesktopMode {
        public static final DesktopMode INSTANCE = new DesktopMode();
        private static final int NORMAL = 0;
        private static final int SHOW_ALL_APPS = 1;

        private DesktopMode() {
        }

        public final int getNORMAL() {
            return NORMAL;
        }

        public final int getSHOW_ALL_APPS() {
            return SHOW_ALL_APPS;
        }
    }
}