package org.zimmob.zimlx.allapps.theme;

import android.content.Context;

import org.zimmob.zimlx.R;

public class AllAppsPagedTheme extends AllAppsBaseTheme {
    public Context mContext;

    public AllAppsPagedTheme(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public int getIconLayout() {
        return R.layout.all_apps_icon;
    }
}
