package org.zimmob.zimlx.icon;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class IconPackInfo {
    String packageName;
    CharSequence label;
    Drawable icon;

    public IconPackInfo(ResolveInfo r, PackageManager packageManager) {
        packageName = r.activityInfo.packageName;
        icon = r.loadIcon(packageManager);
        label = r.loadLabel(packageManager);
    }

    public IconPackInfo(String label, Drawable icon, String packageName) {
        this.label = label;
        this.icon = icon;
        this.packageName = packageName;
    }
}
