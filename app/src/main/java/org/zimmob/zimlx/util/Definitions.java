package org.zimmob.zimlx.util;

public class Definitions {
    public static final int ACTION_LAUNCHER = 8;
    public static final int ACTION_CALL = 7;
    // default dock size is 5 so the center is pos 2
    public static final int DOCK_DEFAULT_CENTER_ITEM_INDEX_X = 2;
    public static final int DOCK_DEFAULT_ZERO_ITEM_INDEX_X = 0;
    public static final int DOCK_DEFAULT_ONE_ITEM_INDEX_X = 1;
    public static final int DOCK_DEFAULT_FOUR_ITEM_INDEX_X = 3;
    public static final int DOCK_DEFAULT_FIVE_ITEM_INDEX_X = 4;
    public static final int NO_SCALE = -1;

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

    // doesn't work reliably yet
    public static final boolean ENABLE_ITEM_TOUCH_LISTENER = false;
}
