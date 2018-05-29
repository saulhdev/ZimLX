package org.zimmob.zimlx.icon;


import android.graphics.drawable.Drawable;

public interface IconDrawer {
    void onIconAvailable(Drawable drawable, int index);

    void onIconCleared(Drawable placeholder, int index);
}