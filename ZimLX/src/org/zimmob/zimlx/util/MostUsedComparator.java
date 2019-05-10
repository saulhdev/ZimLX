package org.zimmob.zimlx.util;

import android.content.Context;

import com.android.launcher3.AppInfo;

import java.util.Comparator;

public class MostUsedComparator implements Comparator<AppInfo> {
    private DbHelper db;

    public MostUsedComparator(Context context) {

        db = new DbHelper(context);
    }

    @Override
    public int compare(AppInfo app1, AppInfo app2) {
        int item1 = db.getAppCount(app1.componentName.getPackageName());
        int item2 = db.getAppCount(app2.componentName.getPackageName());
        if (item1 < item2) {
            return 1;
        } else if (item2 < item1) {
            return -1;
        } else {
            return 0;
        }
    }

}
