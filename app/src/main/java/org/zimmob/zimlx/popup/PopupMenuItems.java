package org.zimmob.zimlx.popup;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.activity.homeparts.HpAppEditApplier;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.dragndrop.DragAction;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.widget.CellContainer;
import org.zimmob.zimlx.widget.Desktop;
import org.zimmob.zimlx.widget.DragOptionLayout;

import java.util.ArrayList;

public class PopupMenuItems {
    private final int uninstallItemIdentifier = 83;
    private final int infoItemIdentifier = 84;
    private final int editItemIdentifier = 85;
    private final int removeItemIdentifier = 86;

    private PopupIconLabelItem uninstallItem = new PopupIconLabelItem(R.string.uninstall, R.drawable.ic_delete_dark_24dp).withIdentifier(uninstallItemIdentifier);
    private PopupIconLabelItem infoItem = new PopupIconLabelItem(R.string.info, R.drawable.ic_info_outline_dark_24dp).withIdentifier(infoItemIdentifier);
    private PopupIconLabelItem editItem = new PopupIconLabelItem(R.string.edit, R.drawable.ic_edit_black_24dp).withIdentifier(editItemIdentifier);
    private PopupIconLabelItem removeItem = new PopupIconLabelItem(R.string.remove, R.drawable.ic_close_dark_24dp).withIdentifier(removeItemIdentifier);

    public void initDragNDrop(HomeActivity launcher, View leftDragHandle, View rightDragHandle, DragOptionLayout dragNDropView) {
        //dragHandle's drag event
        final Handler dragHandler = new Handler();

        dragNDropView.registerDropTarget(new DragOptionLayout.DropTargetListener(leftDragHandle) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int i = launcher.getDesktop().getCurrentItem();
                    if (i > 0) {
                        launcher.getDesktop().setCurrentItem(i - 1);
                    } else if (i == 0) {
                        launcher.getDesktop().addPageLeft(true);
                    }
                    dragHandler.postDelayed(this, 1000);
                }
            };

            @Override
            public boolean onStart(@NonNull DragAction.Action action, @NonNull PointF location, boolean isInside) {
                switch (action) {
                    case APP:
                    case WIDGET:
                    case SEARCH_RESULT:
                    case APP_DRAWER:
                    case GROUP:
                    case SHORTCUT:
                    case ACTION:
                        return true;
                }
                return false;
            }

            @Override
            public void onStartDrag(@NonNull DragAction.Action action, @NonNull PointF location) {
                if (leftDragHandle.getAlpha() >= -0.01 && leftDragHandle.getAlpha() <= 0.01)
                    leftDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnter(@NonNull DragAction.Action action, @NonNull PointF location) {
                dragHandler.post(runnable);
                leftDragHandle.animate().alpha(0.9f);
            }

            @Override
            public void onExit(@NonNull DragAction.Action action, @NonNull PointF location) {
                dragHandler.removeCallbacksAndMessages(null);
                leftDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnd() {
                dragHandler.removeCallbacksAndMessages(null);
                leftDragHandle.animate().alpha(0f);
            }
        });

        dragNDropView.registerDropTarget(new DragOptionLayout.DropTargetListener(rightDragHandle) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int i = launcher.getDesktop().getCurrentItem();
                    if (i < launcher.getDesktop().getPageCount() - 1) {
                        launcher.getDesktop().setCurrentItem(i + 1);
                    } else if (i == launcher.getDesktop().getPageCount() - 1) {
                        launcher.getDesktop().addPageRight(true);
                    }
                    dragHandler.postDelayed(this, 1000);
                }
            };

            @Override
            public boolean onStart(@NonNull DragAction.Action action, @NonNull PointF location, boolean isInside) {
                switch (action) {
                    case APP:
                    case WIDGET:
                    case SEARCH_RESULT:
                    case APP_DRAWER:
                    case GROUP:
                    case SHORTCUT:
                    case ACTION:
                        return true;
                }
                return false;
            }

            @Override
            public void onStartDrag(@NonNull DragAction.Action action, @NonNull PointF location) {
                if (rightDragHandle.getAlpha() >= -0.01 && rightDragHandle.getAlpha() <= 0.01)
                    rightDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnter(@NonNull DragAction.Action action, @NonNull PointF location) {
                dragHandler.post(runnable);
                rightDragHandle.animate().alpha(0.9f);
            }

            @Override
            public void onExit(@NonNull DragAction.Action action, @NonNull PointF location) {
                dragHandler.removeCallbacksAndMessages(null);
                rightDragHandle.animate().alpha(0.5f);
            }

            @Override
            public void onEnd() {
                dragHandler.removeCallbacksAndMessages(null);
                rightDragHandle.animate().alpha(0f);
            }
        });

        //desktop's drag event
        dragNDropView.registerDropTarget(new DragOptionLayout.DropTargetListener(launcher.getDesktop()) {
            @Override
            public boolean onStart(@NonNull DragAction.Action action, @NonNull PointF location, boolean isInside) {
                if (!DragAction.Action.SEARCH_RESULT.equals(action)) {
                    //if (dragNDropView.getDragItem().getType() == Item.Type.APP) {
                    //    launcher.openDialog(new EditAppDialog(HomeActivity.companion.getLauncher(),dragNDropView.getDragItem(),launcher));
                    //} else {
                    showItemPopup(dragNDropView, launcher);
                    //}
                }
                return true;
            }

            @Override
            public void onStartDrag(@NonNull DragAction.Action action, @NonNull PointF location) {
                super.onStartDrag(action, location);
                launcher.closeAppDrawer();
            }

            @Override
            public void onDrop(@NonNull DragAction.Action action, @NonNull PointF location, @NonNull Item item) {
                // this statement makes sure that adding an app multiple times from the app drawer works
                // the app will get a new id every time
                if (DragAction.Action.APP_DRAWER.equals(action)) {
                    if (launcher.getAppDrawerController().isOpen) return;
                    item.reset();
                }

                int x = (int) location.x;
                int y = (int) location.y;
                if (launcher.getDesktop().addItemToPoint(item, x, y)) {
                    launcher.getDesktop().consumeRevert();
                    launcher.getDock().consumeRevert();
                    // add the item to the database
                    HomeActivity._db.saveItem(item, launcher.getDesktop()
                            .getCurrentItem(), Config.ItemPosition.Desktop);
                } else {
                    Point pos = new Point();
                    launcher.getDesktop().getCurrentPage()
                            .touchPosToCoordinate(pos, x, y, item.getSpanX(), item.getSpanY(), false);
                    View itemView = launcher.getDesktop().getCurrentPage().coordinateToChildView(pos);
                    if (itemView != null && Desktop.handleOnDropOver(item, (Item) itemView.getTag(), itemView, launcher.getDesktop().getCurrentPage(), launcher.getDesktop().getCurrentItem(), Config.ItemPosition.Desktop, launcher.getDesktop())) {
                        launcher.getDesktop().consumeRevert();
                        launcher.getDock().consumeRevert();
                    } else {
                        Tool.toast(launcher, R.string.toast_not_enough_space);
                        launcher.getDesktop().revertLastItem();
                        launcher.getDock().revertLastItem();
                    }

                }
            }

            @Override
            public void onMove(DragAction.Action action, PointF location) {
                if (action != DragAction.Action.SEARCH_RESULT && action != DragAction.Action.WIDGET)
                    launcher.getDesktop().updateIconProjection((int) location.x, (int) location.y);
            }

            @Override
            public void onExit(@NonNull DragAction.Action action, @NonNull PointF location) {
                for (CellContainer page : launcher.getDesktop().getPages()) {
                    page.clearCachedOutlineBitmap();
                }
                dragNDropView.cancelFolderPreview();
            }

            @Override
            public void onEnd() {
                launcher.getDesktopIndicator().hideDelay();
                for (CellContainer page : launcher.getDesktop().getPages()) {
                    page.clearCachedOutlineBitmap();
                }
            }
        });

        //dock's drag event
        dragNDropView.registerDropTarget(new DragOptionLayout.DropTargetListener(launcher.getDock()) {
            @Override
            public boolean onStart(@NonNull DragAction.Action action, @NonNull PointF location, boolean isInside) {
                boolean ok = !DragAction.Action.WIDGET.equals(action);
                return ok;
            }

            @Override
            public void onStartDrag(@NonNull DragAction.Action action, @NonNull PointF location) {
                super.onStartDrag(action, location);
            }

            @Override
            public void onDrop(@NonNull DragAction.Action action, @NonNull PointF location, @NonNull Item item) {
                if (DragAction.Action.APP_DRAWER.equals(action)) {
                    if (launcher.getAppDrawerController().isOpen) {
                        return;
                    }
                    item.reset();
                }

                int x = (int) location.x;
                int y = (int) location.y;

                if (launcher.getDock().addItemToPoint(item, x, y)) {
                    launcher.getDesktop().consumeRevert();
                    launcher.getDock().consumeRevert();

                    // add the item to the database
                    HomeActivity._db.saveItem(item, 0, Config.ItemPosition.Dock);
                } else {
                    Point pos = new Point();
                    launcher.getDock().touchPosToCoordinate(pos, x, y, item.spanX, item.spanY, false);
                    View itemView = launcher.getDock().coordinateToChildView(pos);
                    if (itemView != null) {
                        if (Desktop.handleOnDropOver(item, (Item) itemView.getTag(), itemView, launcher.getDock(), 0, Config.ItemPosition.Dock, launcher.getDock())) {
                            launcher.getDesktop().consumeRevert();
                            launcher.getDock().consumeRevert();
                        } else {
                            Tool.toast(launcher, R.string.toast_not_enough_space);
                            launcher.getDesktop().revertLastItem();
                            launcher.getDock().revertLastItem();
                        }
                    } else {
                        Tool.toast(launcher, R.string.toast_not_enough_space);
                        launcher.getDesktop().revertLastItem();
                        launcher.getDock().revertLastItem();
                    }
                }
            }

            @Override
            public void onMove(@NonNull DragAction.Action action, @NonNull PointF location) {
                if (action != DragAction.Action.SEARCH_RESULT) {
                    launcher.getDock().updateIconProjection((int) location.x, (int) location.y);
                }
            }

            @Override
            public void onEnter(@NonNull DragAction.Action action, @NonNull PointF location) {
                super.onEnter(action, location);
            }

            @Override
            public void onExit(@NonNull DragAction.Action action, @NonNull PointF location) {
                launcher.getDock().clearCachedOutlineBitmap();
                dragNDropView.cancelFolderPreview();
            }

            @Override
            public void onEnd() {
                if (DragAction.Action.WIDGET.equals(dragNDropView.getDragAction())) {
                    launcher.getDesktop().revertLastItem();
                }
                launcher.getDock().clearCachedOutlineBitmap();
            }
        });

    }

    private void showItemPopup(@NonNull final DragOptionLayout dragNDropView, final HomeActivity home) {
        ArrayList<PopupIconLabelItem> itemList = new ArrayList<>();
        switch (dragNDropView.getDragItem().getType()) {
            case APP:
            case SHORTCUT:
            case GROUP: {
                itemList.add(editItem);
                itemList.add(removeItem);
                itemList.add(infoItem);

                break;
            }
            case ACTION: {
                itemList.add(editItem);
                itemList.add(removeItem);
                break;
            }
            case WIDGET: {
                itemList.add(removeItem);
                break;
            }
        }

        float x = dragNDropView.getDragLocation().x - HomeActivity._itemTouchX + Tool.toPx(10);
        float y = dragNDropView.getDragLocation().y - HomeActivity._itemTouchY - Tool.toPx((46 * itemList.size()));

        if ((x + Tool.toPx(200)) > dragNDropView.getWidth()) {
            dragNDropView.setPopupMenuShowDirection(false);
            x = dragNDropView.getDragLocation().x - HomeActivity._itemTouchX
                    + home.getDesktop().getCurrentPage().getCellWidth() - Tool.toPx(200) - Tool.toPx(10);
        } else {
            dragNDropView.setPopupMenuShowDirection(true);
        }

        if (y < 0)
            y = dragNDropView.getDragLocation().y
                    - HomeActivity._itemTouchY
                    + home.getDesktop().getCurrentPage().getCellHeight()
                    + Tool.toPx(4);
        else
            y -= Tool.toPx(2);

        dragNDropView.showPopupMenuForItem(x, y, itemList, (v, adapter, item, position) -> {
            Item dragItem;
            if ((dragItem = dragNDropView.getDragItem()) != null) {
                switch ((int) item.getIdentifier()) {
                    case uninstallItemIdentifier: {
                        home.onUninstallItem(dragItem);
                        break;
                    }
                    case editItemIdentifier: {
                        new HpAppEditApplier(home).onEditItem(dragItem);
                        break;
                    }
                    case removeItemIdentifier: {
                        home.onRemoveItem(dragItem);
                        break;
                    }
                    case infoItemIdentifier: {
                        home.onInfoItem(dragItem);
                        break;
                    }
                }
            }
            dragNDropView.hidePopupMenu();
            return true;
        });
    }
}

