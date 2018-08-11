package org.zimmob.zimlx.model;

import android.content.ComponentName;
import android.content.Intent;
import android.os.UserHandle;

public class ItemInfo {
    public static final int NO_ID = -1;
    public long id = NO_ID;
    public int itemType;
    public long container = NO_ID;
    public long screenId = -1;
    public int cellX = -1;
    public int cellY = -1;
    public int spanX = 1;
    public int spanY = 1;
    public int minSpanX = 1;
    /**
     * Indicates the minimum Y cell span.
     */
    public int minSpanY = 1;

    /**
     * Indicates the position in an ordered list.
     */
    public int rank = 0;

    /**
     * Title of the item
     */
    public CharSequence title;

    /**
     * Content description of the item.
     */
    public CharSequence contentDescription;

    public UserHandle user;

    public ItemInfo() {
        //user = Process.myUserHandle();
    }

    ItemInfo(ItemInfo info) {
        copyFrom(info);
    }

    public void copyFrom(ItemInfo info) {
        id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        rank = info.rank;
        screenId = info.screenId;
        itemType = info.itemType;
        container = info.container;
        user = info.user;
        contentDescription = info.contentDescription;
    }

    public Intent getIntent() {
        return null;
    }

    public ComponentName getTargetComponent() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getComponent();
        } else {
            return null;
        }
    }
/*
    public void writeToValues(ContentWriter writer) {
        writer.put(LauncherSettings.Favorites.ITEM_TYPE, itemType)
                .put(LauncherSettings.Favorites.CONTAINER, container)
                .put(LauncherSettings.Favorites.SCREEN, screenId)
                .put(LauncherSettings.Favorites.CELLX, cellX)
                .put(LauncherSettings.Favorites.CELLY, cellY)
                .put(LauncherSettings.Favorites.SPANX, spanX)
                .put(LauncherSettings.Favorites.SPANY, spanY)
                .put(LauncherSettings.Favorites.RANK, rank);
    }
/*
    public void readFromValues(ContentValues values) {
        itemType = values.getAsInteger(LauncherSettings.Favorites.ITEM_TYPE);
        container = values.getAsLong(LauncherSettings.Favorites.CONTAINER);
        screenId = values.getAsLong(LauncherSettings.Favorites.SCREEN);
        cellX = values.getAsInteger(LauncherSettings.Favorites.CELLX);
        cellY = values.getAsInteger(LauncherSettings.Favorites.CELLY);
        spanX = values.getAsInteger(LauncherSettings.Favorites.SPANX);
        spanY = values.getAsInteger(LauncherSettings.Favorites.SPANY);
        rank = values.getAsInteger(LauncherSettings.Favorites.RANK);
    }
*/
    /**
     * Write the fields of this item to the DB
     */
    /*public void onAddToDatabase(ContentWriter writer) {
        if (screenId == Workspace.EXTRA_EMPTY_SCREEN_ID) {
            // We should never persist an item on the extra empty screen.
            throw new RuntimeException("Screen id should not be EXTRA_EMPTY_SCREEN_ID");
        }

        writeToValues(writer);
        writer.put(LauncherSettings.Favorites.PROFILE_ID, user);
    }
    */

    //@Override
    //public final String toString() {
    //    return getClass().getSimpleName() + "(" + dumpProperties() + ")";
    //}

    /*protected String dumpProperties() {
        return "id=" + id
                + " type=" + LauncherSettings.Favorites.itemTypeToString(itemType)
                + " container=" + LauncherSettings.Favorites.containerToString((int)container)
                + " screen=" + screenId
                + " cell(" + cellX + "," + cellY + ")"
                + " span(" + spanX + "," + spanY + ")"
                + " minSpan(" + minSpanX + "," + minSpanY + ")"
                + " rank=" + rank
                + " user=" + user
                + " title=" + title;
    }
*/
    /**
     * Whether this item is disabled.
     */
    public boolean isDisabled() {
        return false;
    }
}
