package com.google.android.apps.nexuslauncher.search;

import android.content.Intent;

import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.util.ComponentKey;

import org.jetbrains.annotations.NotNull;

public class AppItemInfoWithIcon extends ItemInfoWithIcon {
    private Intent mIntent;

    public AppItemInfoWithIcon(@NotNull final ComponentKey componentKey) {
        mIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(componentKey.componentName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        user = componentKey.user;
        itemType = 0;
    }

    public Intent getIntent() {
        return this.mIntent;
    }
}
