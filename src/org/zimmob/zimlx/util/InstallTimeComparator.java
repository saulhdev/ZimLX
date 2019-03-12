package org.zimmob.zimlx.util;

import android.content.pm.PackageManager;

import com.android.launcher3.AppInfo;

import java.util.Comparator;

public class InstallTimeComparator implements Comparator<AppInfo> {
    private final PackageManager mPackageManager;

    public InstallTimeComparator(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Override
    public int compare(AppInfo app1, AppInfo app2) {
        try {
            long app1InstallTime = mPackageManager.getPackageInfo(app1.componentName.getPackageName(), 0).firstInstallTime;
            long app2InstallTime = mPackageManager.getPackageInfo(app2.componentName.getPackageName(), 0).firstInstallTime;
            if (app1InstallTime < app2InstallTime) {
                return 1;
            } else if (app2InstallTime < app1InstallTime) {
                return -1;
            } else {
                return 0;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
