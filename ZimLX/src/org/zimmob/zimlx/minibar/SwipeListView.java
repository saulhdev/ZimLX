package org.zimmob.zimlx.minibar;

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

        for (String action : prefs.getMinibarItems()) {
            DashItem item = null;
            if (action.length() == 2) {
                item = DashUtils.getDashItemFromString(action);
            } else {
                ComponentKey keyMapper = new ComponentKey(mContext, action);
                AppInfo app = Launcher.getLauncher(mContext).mAllAppsController.getAppsView().getAppsStore().getApp(keyMapper);
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
        ((FrameLayout) getMinibar().getParent()).setBackgroundColor(prefs.getMinibarColor());
    }

    private SwipeListView getMinibar() {
        return findViewById(R.id.minibar);
    }
}
