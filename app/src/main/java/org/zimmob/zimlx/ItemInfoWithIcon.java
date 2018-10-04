package org.zimmob.zimlx;

import android.graphics.Bitmap;

public abstract class ItemInfoWithIcon extends ItemInfo {
    /**
     * Indicates that the icon is disabled due to safe mode restrictions.
     */
    public static final int FLAG_DISABLED_SAFEMODE = 1 << 0;
    /**
     * Indicates that the icon is disabled as the app is not available.
     */
    public static final int FLAG_DISABLED_NOT_AVAILABLE = 1 << 1;
    /**
     * Indicates that the icon is disabled as the app is suspended
     */
    public static final int FLAG_DISABLED_SUSPENDED = 1 << 2;
    /**
     * Indicates that the icon is disabled as the user is in quiet mode.
     */
    public static final int FLAG_DISABLED_QUIET_USER = 1 << 3;
    /**
     * Indicates that the icon is disabled as the publisher has disabled the actual shortcut.
     */
    public static final int FLAG_DISABLED_BY_PUBLISHER = 1 << 4;
    /**
     * Indicates that the icon is disabled as the user partition is currently locked.
     */
    public static final int FLAG_DISABLED_LOCKED_USER = 1 << 5;
    public static final int FLAG_DISABLED_MASK = FLAG_DISABLED_SAFEMODE |
            FLAG_DISABLED_NOT_AVAILABLE | FLAG_DISABLED_SUSPENDED |
            FLAG_DISABLED_QUIET_USER | FLAG_DISABLED_BY_PUBLISHER | FLAG_DISABLED_LOCKED_USER;
    /**
     * The item points to a system app.
     */
    public static final int FLAG_SYSTEM_YES = 1 << 6;
    /**
     * The item points to a non system app.
     */
    public static final int FLAG_SYSTEM_NO = 1 << 7;
    public static final int FLAG_SYSTEM_MASK = FLAG_SYSTEM_YES | FLAG_SYSTEM_NO;
    /**
     * Flag indicating that the icon is an {@link android.graphics.drawable.AdaptiveIconDrawable}
     * that can be optimized in various way.
     */
    public static final int FLAG_ADAPTIVE_ICON = 1 << 8;
    /**
     * Flag indicating that the icon is badged.
     */
    public static final int FLAG_ICON_BADGED = 1 << 9;
    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;
    /**
     * Dominant color in the {@link #iconBitmap}.
     */
    public int iconColor;
    /**
     * Indicates whether we're using a low res icon
     */
    public boolean usingLowResIcon;
    /**
     * Status associated with the system state of the underlying item. This is calculated every
     * time a new info is created and not persisted on the disk.
     */
    public int runtimeStatusFlags = 0;

    protected ItemInfoWithIcon() {
    }

    protected ItemInfoWithIcon(ItemInfoWithIcon itemInfoWithIcon) {
        super(itemInfoWithIcon);
        this.iconBitmap = itemInfoWithIcon.iconBitmap;
        iconColor = itemInfoWithIcon.iconColor;
        this.usingLowResIcon = itemInfoWithIcon.usingLowResIcon;
    }
}