package com.android.launcher3;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

public class StringSetAppFilter extends AppFilter {
    private final HashSet<String> mBlackList = new HashSet<>();
    private final HashSet<String> mWidgetBlackList = new HashSet<>();

    public StringSetAppFilter(Context context) {
        mBlackList.add("com.google.android.apps.wallpaper");
        mBlackList.add("com.google.android.launcher");
        mBlackList.add("com.google.android.as");
        mWidgetBlackList.add("com.google.android.apps.wallpaper");
        mWidgetBlackList.add("com.google.android.launcher");
    }

    public boolean shouldShowApp(String packageName, Context context, boolean isWidgetPanel) {

        Set<String> hiddenApps = Utilities.getZimPrefs(context).getHiddenPredictionAppSet();

        if (isWidgetPanel) {
            return !mWidgetBlackList.contains(packageName);
        }
        return !mBlackList.contains(packageName) && (hiddenApps == null || !hiddenApps.contains(packageName));
    }
}
