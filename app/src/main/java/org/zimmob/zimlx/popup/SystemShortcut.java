package org.zimmob.zimlx.popup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

import org.zimmob.zimlx.AbstractFloatingView;
import org.zimmob.zimlx.EditAppDialog;
import org.zimmob.zimlx.EditableItemInfo;
import org.zimmob.zimlx.InfoDropTarget;
import org.zimmob.zimlx.ItemInfo;
import org.zimmob.zimlx.Launcher;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.util.PackageUserKey;
import org.zimmob.zimlx.util.Themes;
import org.zimmob.zimlx.widget.WidgetsBottomSheet;

public abstract class SystemShortcut {
    private final int mIconResId;
    private final int mLabelResId;

    public SystemShortcut(int i, int i2) {
        this.mIconResId = i;
        this.mLabelResId = i2;
    }

    public abstract OnClickListener getOnClickListener(Launcher launcher, ItemInfo itemInfo);

    public Drawable getIcon(Context context, int i) {
        Drawable mutate = context.getResources().getDrawable(this.mIconResId, context.getTheme()).mutate();
        mutate.setTint(Themes.getAttrColor(context, i));
        return mutate;
    }

    public String getLabel(Context context) {
        return context.getString(this.mLabelResId);
    }

    public static class AppInfo extends SystemShortcut {
        public AppInfo() {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label);
        }

        @Override
        public OnClickListener getOnClickListener(final Launcher launcher, final ItemInfo itemInfo) {
            return view -> InfoDropTarget.startDetailsActivityForInfo(itemInfo, launcher, null, launcher.getViewBounds(view), launcher.getActivityLaunchOptions(view));
        }
    }

    public static class Widgets extends SystemShortcut {
        public Widgets() {
            super(R.drawable.ic_widget, R.string.widget_button_text);
        }

        @Override
        public OnClickListener getOnClickListener(final Launcher launcher, final ItemInfo itemInfo) {
            if (launcher.isEditingDisabled())
                return null;
            if (itemInfo.getTargetComponent() == null ||
                    launcher.getWidgetsForPackageUser(new PackageUserKey(itemInfo.getTargetComponent().getPackageName(), itemInfo.user)) == null) {
                return null;
            }
            return view -> {
                launcher.closeFolder();
                AbstractFloatingView.closeAllOpenViews(launcher);
                ((WidgetsBottomSheet) launcher.getLayoutInflater().inflate(R.layout.widgets_bottom_sheet, launcher.getDragLayer(), false)).populateAndShow(itemInfo);
            };
        }
    }

    public static class Edit extends SystemShortcut {
        public Edit() {
            super(R.drawable.ic_edit_no_shadow, R.string.edit_drop_target_label);
        }

        @Override
        public OnClickListener getOnClickListener(final Launcher launcher, final ItemInfo itemInfo) {
            if (launcher.isEditingDisabled())
                return null;
            return view -> {
                AbstractFloatingView.closeAllOpenViews(launcher);
                launcher.openDialog(new EditAppDialog(launcher, (EditableItemInfo) itemInfo, launcher));
            };
        }
    }
}