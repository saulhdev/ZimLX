package org.zimmob.zimlx;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.ViewDebug;

import org.zimmob.zimlx.util.Thunk;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CellLayout {
    public static final int WORKSPACE = 0;
    public static final int HOTSEAT = 1;
    public static final int FOLDER = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({WORKSPACE, HOTSEAT, FOLDER})
    public @interface ContainerType {
    }

    @ContainerType
    private final int mContainerType;
    /**
     * Indicates that this item should use the full extents of its parent.
     */
    public boolean isFullscreen = false;
    @ViewDebug.ExportedProperty(category = "launcher")
    @Thunk
    int mCellWidth;
    @ViewDebug.ExportedProperty(category = "launcher")
    @Thunk
    int mCellHeight;
    private int mFixedCellWidth;
    private int mFixedCellHeight;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mCountX;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mCountY;

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        //super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);
        mContainerType = a.getInteger(R.styleable.CellLayout_containerType, WORKSPACE);

        //mShortcutsAndWidgets = new ShortcutAndWidgetContainer(context, mContainerType);
        //mShortcutsAndWidgets.setCellDimensions(mCellWidth, mCellHeight, mCountX, mCountY);
        //addView(mShortcutsAndWidgets);
    }



}
