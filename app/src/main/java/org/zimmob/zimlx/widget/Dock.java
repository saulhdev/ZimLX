package org.zimmob.zimlx.widget;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.dragndrop.DragAction.Action;
import org.zimmob.zimlx.dragndrop.DragHandler;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.IDesktopCallback;
import org.zimmob.zimlx.viewutil.ItemViewFactory;

import java.util.List;

public final class Dock extends CellContainer implements IDesktopCallback<View> {
    private int _bottomInset;
    private final Point _coordinate = new Point();
    private HomeActivity _home;
    private final Point _previousDragPoint = new Point();
    @Nullable
    private Item _previousItem;
    @Nullable
    private View _previousItemView;
    private float _startPosX;
    private float _startPosY;

    public Dock(@NonNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
    }

    public void init() {
        if (!isInEditMode()) {
            super.init();
        }
    }

    public final void initDockItem(@NonNull HomeActivity home) {

        int columns = Setup.appSettings().getDockSize();
        setGridSize(columns, 1);
        List<Item> dockItems = HomeActivity.Companion.getDb().getDock();
        _home = home;
        removeAllViews();
        for (Item item : dockItems) {
            if (item.getX() < columns && item.getY() == 0) {
                addItemToPage(item, 0);
            }
        }
    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {

        detectSwipe(ev);
        super.dispatchTouchEvent(ev);
        return true;
    }

    private final void detectSwipe(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                Tool.print("ACTION_DOWN");
                _startPosX = ev.getX();
                _startPosY = ev.getY();
                break;
            case 1:
                Tool.print("ACTION_UP");
                Tool.print((int) ev.getX(), (int) ev.getY());
                if (_startPosY - ev.getY() > 150.0f && Setup.appSettings().getGestureDockSwipeUp()) {
                    Point p = new Point((int) ev.getX(), (int) ev.getY());
                    p = Tool.convertPoint(p, this, _home.getAppDrawerController());
                    if (Setup.appSettings().isGestureFeedback()) {
                        Tool.vibrate(this);
                    }
                    _home.openAppDrawer(this, p.x, p.y);
                    break;
                }
            default:
                break;
        }
    }

    /**
     * @param x
     * @param y
     */
    public final void updateIconProjection(int x, int y) {
        HomeActivity homeActivity;
        DragOptionLayout dragNDropView;
        DragState state = peekItemAndSwap(x, y, _coordinate);
        if (!_coordinate.equals(_previousDragPoint)) {
            homeActivity = _home;
            if (homeActivity != null) {
                dragNDropView = homeActivity.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
        }
        _previousDragPoint.set(_coordinate.x, _coordinate.y);
        switch (state) {
            case CurrentNotOccupied:
                projectImageOutlineAt(_coordinate, DragHandler._cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                HomeActivity homeActivity2;
                DragOptionLayout dragNDropView2;
                clearCachedOutlineBitmap();
                homeActivity = _home;
                if (homeActivity != null) {
                    dragNDropView = homeActivity.getDragNDropView();
                    if (dragNDropView != null) {
                        action = dragNDropView.getDragAction();
                        if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (coordinateToChildView(_coordinate) instanceof AppItemView)) {
                            homeActivity2 = _home;
                            if (homeActivity2 != null) {
                                dragNDropView2 = homeActivity2.getDragNDropView();
                                if (dragNDropView2 != null) {
                                    dragNDropView2.showFolderPreviewAt(
                                            this,
                                            ((float) getCellWidth()),
                                                    //* (((float) _coordinate.x) + 0.5f),
                                            (((float) getCellHeight())
                                                    //* (((float) _coordinate.y) + 0.5f))
                                                    - ((float) (Setup.appSettings().isDockShowLabel() ? Tool.toPx(7) : 0))));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                action = null;
                homeActivity2 = _home;
                if (homeActivity2 != null) {
                    dragNDropView2 = homeActivity2.getDragNDropView();
                    if (dragNDropView2 != null) {
                        /*if (Setup.appSettings().isDockShowLabel()) {
                        }*/
                        dragNDropView2.showFolderPreviewAt(this, ((float) getCellWidth()) * (((float) _coordinate.x) + 0.5f), (((float) getCellHeight()) * (((float) _coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDockShowLabel() ? Tool.toPx(7) : 0)));
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setLastItem(@NonNull Object... args) {
        Item item = (Item) args[0];
        View v = (View) args[1];
        _previousItemView = v;
        _previousItem = item;
        removeView(v);
    }

    @NonNull
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {

        _bottomInset = insets.getSystemWindowInsetBottom();
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), _bottomInset);
        return insets;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isInEditMode()) {
            int height;
            int height2 = View.getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int iconSize = Setup.appSettings().getDockIconSize();
            if (Setup.appSettings().isDockShowLabel()) {
                height = Tool.dp2px(((16 + iconSize) + 14) + 10, getContext()) + _bottomInset;
            } else {
                height = Tool.dp2px((16 + iconSize) + 10, getContext()) + _bottomInset;
            }
            getLayoutParams().height = height;
            setMeasuredDimension(View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void consumeRevert() {
        _previousItem = null;
        _previousItemView = null;
    }

    public void revertLastItem() {
        if (_previousItemView != null && _previousItem != null) {
            addViewToGrid(_previousItemView);
            _previousItem = null;
            _previousItemView = null;
        }
    }

    public boolean addItemToPage(@NonNull Item item, int page) {
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), this, Setup.appSettings().getDockIconSize());
        if (itemView == null) {
            HomeActivity.Companion.getDb().deleteItem(item, true);
            return false;
        }
        item._locationInLauncher = 1;
        addViewToGrid(itemView, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());
        return true;
    }

    public boolean addItemToPoint(@NonNull Item item, int x, int y) {

        LayoutParams positionToLayoutPrams = coordinateToLayoutParams(x, y, item.getSpanX(), item.getSpanY());
        if (positionToLayoutPrams == null) {
            return false;
        }
        item._locationInLauncher = 1;
        item.x = positionToLayoutPrams.getX();
        item.y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), this, Setup.appSettings().getDockIconSize());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NonNull Item item, int x, int y) {

        item._locationInLauncher = 1;
        item.x = x;
        item.y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), this, Setup.appSettings().getDockIconSize());
        if (itemView == null) {
            return false;
        }
        addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
        return true;
    }


    public void removeItem(final View view, boolean animate) {

        if (animate) {
            view.animate().setDuration(100).scaleX(0.0f).scaleY(0.0f).withEndAction(() -> {
                if (view.getParent().equals(Dock.this)) {
                    removeView(view);
                }
            });
        } else if (this.equals(view.getParent())) {
            removeView(view);
        }
    }

    public int getBottomInset() {
        return _bottomInset;
    }

    public void setHome(HomeActivity home) {
        _home = home;
    }
}