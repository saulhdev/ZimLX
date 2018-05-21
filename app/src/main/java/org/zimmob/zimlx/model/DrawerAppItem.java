package org.zimmob.zimlx.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mikepenz.fastadapter.items.AbstractItem;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.interfaces.FastItem;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.DragAction;
import org.zimmob.zimlx.widget.AppDrawerVertical;
import org.zimmob.zimlx.widget.AppItemView;
import org.zimmob.zimlx.widget.Desktop;

import java.util.List;

public class DrawerAppItem extends AbstractItem<DrawerAppItem, DrawerAppItem.ViewHolder> implements FastItem.AppItem<DrawerAppItem, DrawerAppItem.ViewHolder> {
    private App app;
    private AppItemView.LongPressCallBack onLongClickCallback;

    public DrawerAppItem(App app) {
        this.app = app;
        onLongClickCallback = new AppItemView.LongPressCallBack() {
            @Override
            public boolean readyForDrag(View view) {
                return Setup.appSettings().getDesktopStyle() != Desktop.DesktopMode.INSTANCE.getSHOW_ALL_APPS();
            }

            @Override
            public void afterDrag(View view) {
            }
        };
    }

    @Override
    public int getType() {
        return R.id.id_adapter_drawer_app_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_app;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public App getApp() {
        return app;
    }

    @Override
    public void bindView(DrawerAppItem.ViewHolder holder, List payloads) {
        holder.builder
                .setAppItem(app)
                .withOnLongClick(app, DragAction.Action.APP_DRAWER, onLongClickCallback)
                .withOnTouchGetPosition(null, null);
        holder.appItemView.load();
        super.bindView(holder, payloads);
    }

    @Override
    public void unbindView(DrawerAppItem.ViewHolder holder) {
        super.unbindView(holder);
        holder.appItemView.reset();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AppItemView appItemView;
        AppItemView.Builder builder;

        ViewHolder(View itemView) {
            super(itemView);
            appItemView = (AppItemView) itemView;
            appItemView.setTargetedWidth(AppDrawerVertical.itemWidth);

            appItemView.setTargetedHeightPadding(AppDrawerVertical.itemHeightPadding);

            builder = new AppItemView.Builder(appItemView, Setup.appSettings().getDrawerIconSize())
                    .withOnTouchGetPosition(null, null)
                    .setLabelVisibility(Setup.appSettings().isDrawerShowLabel())
                    .setTextColor(Setup.appSettings().getDrawerLabelColor())
                    .setFontSize(appItemView.getContext(), Setup.appSettings().getDrawerLabelFontSize())
                    .setFastAdapterItem();
        }
    }
}
