package org.zimmob.zimlx.config;

import android.content.Context;
import android.os.Build;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.util.AppSettings;

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
    public static final String APP_SORT_AZ="az";
    public static final String APP_SORT_ZA="za";
    public static final String APP_SORT_LI="li";//last installer
    public static final String APP_SORT_MU="mu";//most used

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

    public enum PeekDirection {
        UP, LEFT, RIGHT, DOWN
    }

    public static final boolean ATLEAST_NOUGAT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

    public static int boundToRange(int value, int lowerBound, int upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }


    /**
     * The gesture is an application
     */
    public static final int ITEM_TYPE_APPLICATION = 0;

    /**
     * The gesture is an application created shortcut
     */
    public static final int ITEM_TYPE_SHORTCUT = 1;

    public static final int ITEM_TYPE_FOLDER = 2;

    /**
     * The favorite is a widget
     */
    public static final int ITEM_TYPE_APPWIDGET = 4;

    /**
     * The favorite is a custom widget provided by the launcher
     */
    public static final int ITEM_TYPE_CUSTOM_APPWIDGET = 5;

    /**
     * The gesture is an application created deep shortcut
     */
    public static final int ITEM_TYPE_DEEP_SHORTCUT = 6;
}
