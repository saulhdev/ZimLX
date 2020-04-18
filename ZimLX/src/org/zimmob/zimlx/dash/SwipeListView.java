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

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;

import org.zimmob.zimlx.ZimPreferences;

import java.util.ArrayList;

public class SwipeListView extends ListView {
    private Context mContext;
    private ArrayList<DashItem> dashItems = new ArrayList<>();
    private ZimPreferences prefs;
    public SwipeListView(Context context) {
        this(context, null, 0);
    }

    public SwipeListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        prefs = Utilities.getZimPrefs(mContext);

        for (String action : prefs.getDashItems()) {
            DashItem item = null;
            if (action.length() == 2) {
                item = DashUtils.getDashItemFromString(action);
            } else {
                ComponentKey keyMapper = new ComponentKey(new ComponentName(mContext, action), null );
                AppInfo app = Launcher.getLauncher(mContext).getAppsView().getAppsStore().getApp(keyMapper);
                if (app != null) {
                    item = DashItem.asApp(app, 0);
                }
            }

            if (item != null) {
                dashItems.add(item);
            }
        }

        SwipeListView minibar = findViewById(R.id.minibar);
        minibar.setAdapter(new DashAdapter(mContext, dashItems));

        minibar.setOnItemClickListener((parent, view, i, id) -> {
            DashAction.Action action = DashAction.Action.valueOf(dashItems.get(i).action.name());
            DashUtils.RunAction(action, mContext);
            if (action != DashAction.Action.DeviceSettings && action != DashAction.Action.LauncherSettings && action != DashAction.Action.EditMinibar) {
                Launcher.getLauncher(mContext).getDrawerLayout().closeDrawers();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((FrameLayout) getMinibar().getParent()).setBackgroundColor(prefs.getDashColor());
    }

    private SwipeListView getMinibar() {
        return findViewById(R.id.minibar);
    }
}
