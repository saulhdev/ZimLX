/*
 * Copyright (C) 2020 Zim Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zimmob.zimlx.dash;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;

import org.zimmob.zimlx.dash.DashAction.Action;

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
