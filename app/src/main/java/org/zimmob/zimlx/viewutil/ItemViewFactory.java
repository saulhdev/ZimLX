package org.zimmob.zimlx.viewutil;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.Home;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.App;
import org.zimmob.zimlx.util.DragAction;
import org.zimmob.zimlx.util.DragHandler;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.widget.AppItemView;
import org.zimmob.zimlx.widget.CellContainer;
import org.zimmob.zimlx.widget.WidgetView;

/**
 *
 */
public class ItemViewFactory {

    public static final int NO_FLAGS = 0x01;
    public static final int NO_LABEL = 0x02;

    public static View getItemView(Context context, Item item, boolean showLabels, DesktopCallBack callBack, int iconSize) {
        int flag = showLabels ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
        return getItemView(context, callBack, item, iconSize, flag);
    }

    /**
     * @param context
     * @param callBack
     * @param item
     * @param iconSize
     * @param flags
     * @return
     */
    private static View getItemView(final Context context, final DesktopCallBack callBack, final Item item, int iconSize, int flags) {
        View view = null;
        switch (item.getType()) {
            case APP:
                final App app = Setup.appLoader().findItemApp(item);
                if (app == null) {
                    break;
                }
                view = new AppItemView.Builder(context, iconSize)
                        .setAppItem(item, app)
                        .withOnTouchGetPosition(item, Setup.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.APP, new AppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case SHORTCUT:
                view = new AppItemView.Builder(context, iconSize)
                        .setShortcutItem(item)
                        .withOnTouchGetPosition(item, Setup.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.SHORTCUT, new AppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case GROUP:
                view = new AppItemView.Builder(context, iconSize)
                        .setGroupItem(context, callBack, item, iconSize)
                        .withOnTouchGetPosition(item, Setup.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.GROUP, new AppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                break;
            case ACTION:
                view = new AppItemView.Builder(context, iconSize)
                        .setActionItem(item)
                        .withOnTouchGetPosition(item, Setup.itemGestureCallback())
                        .vibrateWhenLongPress()
                        .withOnLongClick(item, DragAction.Action.ACTION, new AppItemView.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case WIDGET:
                if (Home.Companion.getAppWidgetHost() == null) break;
                final AppWidgetProviderInfo appWidgetInfo = Home.Companion.getAppWidgetManager().getAppWidgetInfo(item.getWidgetValue());
                final WidgetView widgetView = (WidgetView) Home.Companion.getAppWidgetHost().createView(context, item.getWidgetValue(), appWidgetInfo);

                widgetView.setAppWidget(item.getWidgetValue(), appWidgetInfo);
                widgetView.post(() -> updateWidgetOption(item));

                final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_widget_container, null);
                widgetContainer.addView(widgetView);

                final View ve = widgetContainer.findViewById(R.id.vertexpand);
                ve.bringToFront();
                final View he = widgetContainer.findViewById(R.id.horiexpand);
                he.bringToFront();
                final View vl = widgetContainer.findViewById(R.id.vertless);
                vl.bringToFront();
                final View hl = widgetContainer.findViewById(R.id.horiless);
                hl.bringToFront();

                ve.animate().scaleY(1).scaleX(1);
                he.animate().scaleY(1).scaleX(1);
                vl.animate().scaleY(1).scaleX(1);
                hl.animate().scaleY(1).scaleX(1);

                final Runnable action = () -> {
                    ve.animate().scaleY(0).scaleX(0);
                    he.animate().scaleY(0).scaleX(0);
                    vl.animate().scaleY(0).scaleX(0);
                    hl.animate().scaleY(0).scaleX(0);
                };

                widgetContainer.postDelayed(action, 2000);
                widgetView.setOnTouchListener(Tool.getItemOnTouchListener(item, Setup.itemGestureCallback()));
                widgetView.setOnLongClickListener(view15 -> {
                    if (Setup.appSettings().isDesktopLock()) {
                        return false;
                    }
                    view15.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    DragHandler.startDrag(view15, item, DragAction.Action.WIDGET, null);

                    callBack.setLastItem(item, widgetContainer);
                    return true;
                });

                ve.setOnClickListener(view14 -> {
                    if (view14.getScaleX() < 1) return;
                    item.setSpanY(item.getSpanY() + 1);
                    scaleWidget(widgetContainer, item);
                    widgetContainer.removeCallbacks(action);
                    widgetContainer.postDelayed(action, 2000);
                });
                he.setOnClickListener(view13 -> {
                    if (view13.getScaleX() < 1) return;
                    item.setSpanX(item.getSpanX() + 1);
                    scaleWidget(widgetContainer, item);
                    widgetContainer.removeCallbacks(action);
                    widgetContainer.postDelayed(action, 2000);
                });
                vl.setOnClickListener(view12 -> {
                    if (view12.getScaleX() < 1) return;
                    item.setSpanY(item.getSpanY() - 1);
                    scaleWidget(widgetContainer, item);
                    widgetContainer.removeCallbacks(action);
                    widgetContainer.postDelayed(action, 2000);
                });
                hl.setOnClickListener(view1 -> {
                    if (view1.getScaleX() < 1) return;
                    item.setSpanX(item.getSpanX() - 1);
                    scaleWidget(widgetContainer, item);
                    widgetContainer.removeCallbacks(action);
                    widgetContainer.postDelayed(action, 2000);
                });
                view = widgetContainer;
                break;
        }
        if (view != null) {
            view.setTag(item);
        }
        return view;
    }

    private static void scaleWidget(View view, Item item) {
        item.setSpanX(Math.min(item.getSpanX(), Home.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanH()));
        item.setSpanX(Math.max(item.getSpanX(), 1));
        item.setSpanY(Math.min(item.getSpanY(), Home.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanV()));
        item.setSpanY(Math.max(item.getSpanY(), 1));

        Home.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(false, (CellContainer.LayoutParams) view.getLayoutParams());
        if (!Home.Companion.getLauncher().getDesktop().getCurrentPage().checkOccupied(new Point(item.getX(), item.getY()), item.getSpanX(), item.getSpanY())) {
            CellContainer.LayoutParams newWidgetLayoutParams = new CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());

            // update occupied array
            Home.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, newWidgetLayoutParams);

            // update the view
            view.setLayoutParams(newWidgetLayoutParams);
            updateWidgetOption(item);

            // update the widget size in the database
            Home.Companion.getDb().saveItem(item);
        } else {
            Toast.makeText(Home.Companion.getLauncher().getDesktop().getContext(), R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();

            // add the old layout params to the occupied array
            Home.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, (CellContainer.LayoutParams) view.getLayoutParams());
        }
    }

    private static void updateWidgetOption(Item item) {
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.getSpanX() * Home.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.getSpanX() * Home.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.getSpanY() * Home.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight());
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.getSpanY() * Home.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight());
        Home.Companion.getAppWidgetManager().updateAppWidgetOptions(item.getWidgetValue(), newOps);
    }
}
