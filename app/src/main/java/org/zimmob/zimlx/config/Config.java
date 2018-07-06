package org.zimmob.zimlx.config;

import android.view.View;

/**
 * Created by saul on 05-06-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class Config {
    //App Drawer Mode
    public static final int DRAWER_HORIZONTAL = 0;
    public static final int DRAWER_VERTICAL = 1;

    //Folder Shape
    public static final int FOLDER_SHAPE_CIRCLE = 0;
    public static final int FOLDER_SHAPE_CIRCLE_SHADOW = 1;
    public static final int FOLDER_SHAPE_SQUARE = 2;
    public static final int FOLDER_SHAPE_SQUARE_SHADOW = 3;

    //SORT MODE
    public static final int APP_SORT_AZ = 0;
    public static final int APP_SORT_ZA = 1;
    public static final int APP_SORT_LI = 2;//last installer
    public static final int APP_SORT_MU = 3;//most used

    //INDICATOR MODE
    public static final int INDICATOR_DOTS = 0;
    public static final int INDICATOR_ARROW = 1;
    public static final int INDICATOR_LINE = 2;

    public static final int ACTION_LAUNCHER = 8;
    public static final int NO_SCALE = -1;

    public static final boolean DEBUG_MODE = true;

    // separates a list of integers
    public static final String INT_SEP = "#";

    // don't change the order, index is saved into db!
    public enum ItemPosition {
        Dock,
        Desktop
    }

    // don't change the order, index is saved into db!
    public enum ItemState {
        Hidden,
        Visible
    }

    public enum PeekDirection {
        UP, LEFT, RIGHT, DOWN
    }

    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    // doesn't work reliably yet
    public static final boolean ENABLE_ITEM_TOUCH_LISTENER = false;


    public static int boundToRange(int value, int lowerBound, int upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }
}
