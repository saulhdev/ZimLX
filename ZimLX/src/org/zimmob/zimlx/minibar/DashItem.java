package org.zimmob.zimlx.minibar;

import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.R;

import org.zimmob.zimlx.minibar.DashAction.Action;

public class DashItem {
    public String title;
    public String description;
    public int id;
    public int icon;
    public Action action;
    // The type of this item
    public int viewType;

    public static DashItem asApp(ItemInfoWithIcon appInfo, int id) {
        DashItem item = new DashItem();
        item.title = appInfo.title.toString();
        item.description = appInfo.contentDescription.toString();
        item.id = id;
        item.icon = R.drawable.ic_cortana;
        return item;
    }

    public static DashItem asCustomItem(Action action, String label, String description, int icon, int id) {
        DashItem item = new DashItem();
        item.action = action;
        item.title = label;
        item.description = description;
        item.icon = icon;
        item.id = id;
        return item;
    }
}
