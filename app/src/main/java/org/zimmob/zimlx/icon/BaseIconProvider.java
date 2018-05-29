package org.zimmob.zimlx.icon;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class BaseIconProvider implements IconProvider{
    @Override
    public void loadIcon(IconTargetType type, int forceSize, Object target, Object... args) {

    }

    @Override
    public void cancelLoad(IconTargetType type, Object target) {

    }

    @Override
    public boolean isGroupIconDrawable() {
        return false;
    }

    @Override
    public Drawable getDrawableSynchronously(int forceSize) {
        return null;
    }
}
