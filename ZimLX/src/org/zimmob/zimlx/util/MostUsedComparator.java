package org.zimmob.zimlx.util;

import android.util.Log;

import com.android.launcher3.AppInfo;

import org.zimmob.zimlx.model.AppCountInfo;

import java.util.Comparator;
import java.util.List;

public class MostUsedComparator implements Comparator<AppInfo> {
    private String TAG = "MostUsedComparator";
    private List<AppCountInfo> mApps;

    public MostUsedComparator(List<AppCountInfo> apps) {
        mApps = apps;
    }

    @Override
    public int compare(AppInfo app1, AppInfo app2) {
        int item1 = 0;
        int item2 = 0;

        for (int i = 0; i < mApps.size(); i++) {
            if (mApps.get(i).getPackageName().equals(app1.componentName.getPackageName())) {
                item1 = mApps.get(i).getCount();
                Log.e(TAG, "Sorting Apps 1 " + item1);

            }
            if (mApps.get(i).getPackageName().equals(app2.componentName.getPackageName())) {
                item2 = mApps.get(i).getCount();
                Log.e(TAG, "Sorting Apps 2 " + item2);
            }
        }

        if (item1 < item2) {
            return 1;
        } else if (item2 < item1) {
            return -1;
        } else {
            return 0;
        }
    }

}
