package org.zimmob.zimlx.minibar;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;

import org.zimmob.zimlx.minibar.DashAction.Action;

public class DashItem {
    public static final String TAG = "DashItem";
    public static final int VIEW_TYPE_DASH_APP = 1 << 1;
    public static final int VIEW_TYPE_DASH_ITEM = 1 << 2;

    public String title;
    public String description;
    public String component;
    public int id;
    public int icon;
    public Action action;
    public int viewType;

    public static DashItem asApp(AppInfo appInfo, int id) {
        DashItem item = new DashItem();
        item.title = appInfo.title.toString();
        item.description = appInfo.contentDescription.toString();
        item.component = appInfo.componentName.toString();
        item.id = id;
        item.icon = R.drawable.ic_cortana;
        item.viewType = VIEW_TYPE_DASH_APP;
        return item;
    }

    public static DashItem asCustomItem(Action action, String label, String description, int icon, int id) {
        DashItem item = new DashItem();
        item.action = action;
        item.title = label;
        item.description = description;
        item.icon = icon;
        item.id = id;
        item.viewType = VIEW_TYPE_DASH_ITEM;
        return item;
    }
}
