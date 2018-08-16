package org.zimmob.zimlx.icon;

import android.graphics.drawable.Drawable;

import org.zimmob.zimlx.compat.LauncherActivityInfoCompat;

public class DefaultIconPack extends IconPack {

    public DefaultIconPack() {
        super(null, null, null, null, null, null, 1f, null);
    }

    @Override
    public Drawable getIcon(LauncherActivityInfoCompat info) {
        return info.getIcon(0);
    }

    @Override
    public String getPackageName() {
        return "";
    }
}
