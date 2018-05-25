package org.zimmob.zimlx.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.dragndrop.DragAction;
import org.zimmob.zimlx.dragndrop.DragHandler;
import org.zimmob.zimlx.util.Tool;

import java.util.Objects;

public class DragOptionView extends CardView {
    public boolean _isDraggedFromDrawer = false;
    private boolean _dragging = false;
    private View[] _hideViews;
    private LinearLayout _dragOptions;
    private TextView _editIcon;
    private TextView _removeIcon;
    private TextView _infoIcon;
    private TextView _deleteIcon;
    private HomeActivity _home;
    private Long _animSpeed = 120L;

    public DragOptionView(Context context) {
        super(context);
        init();
    }

    public DragOptionView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public void setHome(HomeActivity home) {
        _home = home;
    }

    public void setAutoHideView(View... v) {
        _hideViews = v;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        ((MarginLayoutParams) getLayoutParams()).topMargin = insets.getSystemWindowInsetTop() + Tool.dp2px(14, getContext());
        return insets;
    }

    private void init() {
        setCardElevation(Tool.dp2px(4, getContext()));
        setRadius(Tool.dp2px(2, getContext()));

        _dragOptions = (LinearLayout) ((LayoutInflater) Objects.requireNonNull(getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(R.layout.view_drag_option, this, false);
        addView(_dragOptions);

        _editIcon = _dragOptions.findViewById(R.id.editIcon);
        _editIcon.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return ((DragAction) dragEvent.getLocalState()).action != DragAction.Action.APP_DRAWER;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    final Item item = DragHandler.INSTANCE.getDraggedObject(dragEvent);

                    Setup.eventHandler().showEditDialog(getContext(), item, name -> {
                        Objects.requireNonNull(item).setLabel(name);
                        HomeActivity.Companion.getDb().saveItem(item);

                        Objects.requireNonNull(HomeActivity.Companion.getLauncher()).getDesktop().addItemToCell(item, item.getX(), item.getY());
                        HomeActivity.Companion.getLauncher().getDesktop().removeItem(HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().coordinateToChildView(new Point(item.getX(), item.getY())), false);
                    });
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return false;
        });
        _removeIcon = _dragOptions.findViewById(R.id.removeIcon);
        _removeIcon.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    switch (((DragAction) dragEvent.getLocalState()).action) {
                        case GROUP:
                        case APP:
                        case WIDGET:
                        case SHORTCUT:
                        case APP_DRAWER:
                        case ACTION:
                            return true;
                    }
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    Item item = DragHandler.INSTANCE.getDraggedObject(dragEvent);

                    // remove all items from the database
                    HomeActivity.Companion.getDb().deleteItem(item, true);

                    _home.getDesktop().consumeRevert();
                    _home.getDock().consumeRevert();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return false;
        });
        _infoIcon = _dragOptions.findViewById(R.id.infoIcon);
        _infoIcon.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    switch (((DragAction) dragEvent.getLocalState()).action) {
                        case APP_DRAWER:
                        case APP:
                            return true;
                    }
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    Item item = DragHandler.INSTANCE.getDraggedObject(dragEvent);
                    if (Objects.requireNonNull(item).getType() == Item.Type.APP) {
                        try {
                            getContext().startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + Objects.requireNonNull(item.getIntent().getComponent()).getPackageName())));
                        } catch (Exception e) {
                            Tool.toast(getContext(), R.string.toast_app_uninstalled);
                        }
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return false;
        });
        _deleteIcon = _dragOptions.findViewById(R.id.deleteIcon);
        _deleteIcon.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    switch (((DragAction) dragEvent.getLocalState()).action) {
                        case APP_DRAWER:
                        case APP:
                            return true;
                    }
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    //Setup.eventHandler().showDeletePackageDialog(getContext(), dragEvent);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return false;
        });

        _editIcon.setText(_editIcon.getText(), TextView.BufferType.SPANNABLE);
        _removeIcon.setText(_removeIcon.getText(), TextView.BufferType.SPANNABLE);
        _infoIcon.setText(_infoIcon.getText(), TextView.BufferType.SPANNABLE);
        _deleteIcon.setText(_deleteIcon.getText(), TextView.BufferType.SPANNABLE);

        for (int i = 0; i < _dragOptions.getChildCount(); i++) {
            _dragOptions.getChildAt(i).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchDragEvent(DragEvent ev) {
        final DragEvent event = ev;
        boolean r = super.dispatchDragEvent(ev);
        if (r && (ev.getAction() == DragEvent.ACTION_DRAG_STARTED || ev.getAction() == DragEvent.ACTION_DRAG_ENDED)) {
            // If we got a start or end and the return value is true, our
            // onDragEvent wasn't called by ViewGroup.dispatchDragEvent
            // So we do it here.
            this.post(() -> onDragEvent(event));


            // fix crash on older versions of android
            try {
                super.dispatchDragEvent(ev);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    private void animShowView() {
        if (_hideViews != null) {
            _isDraggedFromDrawer = true;

            if (Setup.get().getAppSettings().getSearchBarEnable())
                Tool.invisibleViews(Math.round(_animSpeed / 1.3f), _hideViews);

            animate().alpha(1);
        }
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                _dragging = true;
                animShowView();
                boolean desktopHideGrid = Setup.appSettings().isDesktopHideGrid();
                _home.getDock().setHideGrid(desktopHideGrid);
                for (CellContainer cellContainer : _home.getDesktop().getPages()) {
                    cellContainer.setHideGrid(desktopHideGrid);
                }
                switch (((DragAction) event.getLocalState()).action) {
                    case ACTION:
                        _editIcon.setVisibility(View.VISIBLE);
                        _removeIcon.setVisibility(View.VISIBLE);
                        return true;
                    case APP:
                        _editIcon.setVisibility(View.VISIBLE);
                        _removeIcon.setVisibility(View.VISIBLE);
                        _infoIcon.setVisibility(View.VISIBLE);
                        _deleteIcon.setVisibility(View.VISIBLE);
                    case APP_DRAWER:
                        _removeIcon.setVisibility(View.VISIBLE);
                        _infoIcon.setVisibility(View.VISIBLE);
                        _deleteIcon.setVisibility(View.VISIBLE);
                        return true;
                    case WIDGET:
                        _removeIcon.setVisibility(View.VISIBLE);
                        return true;
                    case GROUP:
                        _editIcon.setVisibility(View.VISIBLE);
                        _removeIcon.setVisibility(View.VISIBLE);
                        return true;
                    case SHORTCUT:
                        _removeIcon.setVisibility(View.VISIBLE);
                        return true;
                }
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                return true;
            case DragEvent.ACTION_DROP:
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                _dragging = false;
                _home.getDock().setHideGrid(true);
                for (CellContainer cellContainer : _home.getDesktop().getPages()) {
                    cellContainer.setHideGrid(true);
                }

                animate().alpha(0);
                _editIcon.setVisibility(View.GONE);
                _removeIcon.setVisibility(View.GONE);
                _infoIcon.setVisibility(View.GONE);
                _deleteIcon.setVisibility(View.GONE);

                if (Setup.get().getAppSettings().getSearchBarEnable())
                    Tool.visibleViews(Math.round(_animSpeed / 1.3f), _hideViews);

                // the search view might be disabled
                Objects.requireNonNull(HomeActivity.Companion.getLauncher()).updateSearchBar(true);

                _isDraggedFromDrawer = false;

                _home.getDock().revertLastItem();
                _home.getDesktop().revertLastItem();
                return true;
        }
        return false;
    }
}
