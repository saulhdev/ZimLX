package com.android.launcher3.folder;

import android.content.Context;

/**
 * Handle the Folder Icon Shape customization.
 */
public class FolderCustomIcon {
    public static final String PIXEL_DEFAULT = "Default";
    public static final String PATH_SQUARE = "M50,0L100,0 100,100 0,100 0,0z";
    public static final String PATH_ROUNDED = "M50,0L70,0 A30,30,0,0 1 100,30 L100,70 A30,30,0,0 1 70,100 L30,100 A30,30,0,0 1 0,70 L 0,30 A30,30,0,0 1 30,0z";
    public static final String PATH_SQUIRCLE = "M50,0 C10,0 0,10 0,50 0,90 10,100 50,100 90,100 100,90 100,50 100,10 90,0 50,0 Z";
    public static final String PATH_TEARDROP = "M50,0A50,50,0,0 1 100,50 L100,85 A15,15,0,0 1 85,100 L50,100 A50,50,0,0 1 50,0z";
    private Context context;

    public FolderCustomIcon(Context context, int mask) {
        this.context = context;

    }
}
