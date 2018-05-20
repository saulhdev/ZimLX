package org.zimmob.zimlx.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.config.Config.ItemPosition.*;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper implements Setup.DataManager {
    private static final String DATABASE_HOME = "home.db";
    private static final String TABLE_HOME = "home";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_X_POS = "x";
    private static final String COLUMN_Y_POS = "y";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_PAGE = "page";
    private static final String COLUMN_DESKTOP = "desktop";
    private static final String COLUMN_STATE = "state";

    private static final String SQL_CREATE_HOME =
            "CREATE TABLE " + TABLE_HOME + " (" +
                    COLUMN_TIME + " INTEGER PRIMARY KEY," +
                    COLUMN_TYPE + " VARCHAR," +
                    COLUMN_LABEL + " VARCHAR," +
                    COLUMN_X_POS + " INTEGER," +
                    COLUMN_Y_POS + " INTEGER," +
                    COLUMN_DATA + " VARCHAR," +
                    COLUMN_PAGE + " INTEGER," +
                    COLUMN_DESKTOP + " INTEGER," +
                    COLUMN_STATE + " INTEGER)";
    private static final String SQL_DELETE = "DROP TABLE IF EXISTS ";
    private static final String SQL_QUERY = "SELECT * FROM ";
    private SQLiteDatabase db;
    private Context context;

    public DatabaseHelper(Context c) {
        super(c, DATABASE_HOME, null, 1);
        db = getWritableDatabase();
        context = c;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_HOME);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE + TABLE_HOME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void createItem(Item item, int page, Config.ItemPosition itemPosition) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_TIME, item.getId());
        itemValues.put(COLUMN_TYPE, item.getType().toString());
        itemValues.put(COLUMN_LABEL, item.getLabel());
        itemValues.put(COLUMN_X_POS, item.getX());
        itemValues.put(COLUMN_Y_POS, item.getY());

        Setup.logger().log(this, Log.INFO, null, "createItem: %s (ID: %d)", item.getLabel(), item.getId());

        StringBuilder concat = new StringBuilder();
        switch (item.getType()) {
            case APP:
                Setup.logger().log(this, Log.INFO, null, "Save App: %s", item.getIntent().toString());


                if (Setup.appSettings().enableImageCaching()) {
                    Tool.saveIcon(context, Tool.drawableToBitmap(item.getIconProvider().getDrawableSynchronously(-1)), Integer.toString(item.getId()));
                    Setup.logger().log(this, Log.INFO, null, "Checking Save Method:%s", item.getIntent().toString());

                }
                itemValues.put(COLUMN_DATA, Tool.getIntentAsString(item.getIntent()));
                break;
            case GROUP:
                for (Item tmp : item.getItems()) {
                    concat.append(tmp.getId()).append(Config.INT_SEP);
                }
                itemValues.put(COLUMN_DATA, concat.toString());
                break;
            case ACTION:
                itemValues.put(COLUMN_DATA, item.getActionValue());
                break;
            case WIDGET:
                concat = new StringBuilder(Integer.toString(item.getWidgetValue()) + Config.INT_SEP
                        + Integer.toString(item.getSpanX()) + Config.INT_SEP
                        + Integer.toString(item.getSpanY()));
                itemValues.put(COLUMN_DATA, concat.toString());
                break;
        }
        itemValues.put(COLUMN_PAGE, page);
        itemValues.put(COLUMN_DESKTOP, itemPosition.ordinal());

        // item will always be visible when first added
        itemValues.put(COLUMN_STATE, 1);
        db.insert(TABLE_HOME, null, itemValues);
    }

    @Override
    public void saveItem(Item item) {
        updateItem(item);
    }

    @Override
    public void saveItem(Item item, Config.ItemState state) {
        updateItem(item, state);
    }

    @Override
    public void saveItem(Item item, int page, Config.ItemPosition itemPosition) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.getId();
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        if (cursor.getCount() == 0) {
            createItem(item, page, itemPosition);
        } else if (cursor.getCount() == 1) {
            updateItem(item, page, itemPosition);
        }
    }

    @Override
    public void deleteItem(Item item, boolean deleteSubItems) {
        // if the item is a group then remove all entries
        if (deleteSubItems && item.getType() == Item.Type.GROUP) {
            for (Item i : item.getGroupItems()) {
                deleteItem(i, deleteSubItems);
            }
        }
        // delete the item itself
        db.delete(TABLE_HOME, COLUMN_TIME + " = ?", new String[]{String.valueOf(item.getId())});
    }

    @Override
    public List<List<Item>> getDesktop() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<List<Item>> desktop = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int page = Integer.parseInt(cursor.getString(6));
                int desktopVar = Integer.parseInt(cursor.getString(7));
                int stateVar = Integer.parseInt(cursor.getString(8));
                while (page >= desktop.size()) {
                    desktop.add(new ArrayList<>());
                }
                if (desktopVar == 1 && stateVar == 1) {
                    desktop.get(page).add(getSelection(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return desktop;
    }

    @Override
    public List<Item> getDock() {
        String SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME;
        Cursor cursor = db.rawQuery(SQL_QUERY_DESKTOP, null);
        List<Item> dock = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int desktopVar = Integer.parseInt(cursor.getString(7));
                int stateVar = Integer.parseInt(cursor.getString(8));
                if (desktopVar == 0 && stateVar == 1) {
                    dock.add(getSelection(cursor));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Tool.print("database : dock size is ", dock.size());
        return dock;
    }

    @Override
    public Item getItem(int id) {
        String SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + id;
        Cursor cursor = db.rawQuery(SQL_QUERY_SPECIFIC, null);
        Item item = null;
        if (cursor.moveToFirst()) {
            item = getSelection(cursor);
        }
        cursor.close();
        return item;
    }

    // update data attribute for an item
    private void updateItem(Item item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_LABEL, item.getLabel());
        itemValues.put(COLUMN_X_POS, item.getX());
        itemValues.put(COLUMN_Y_POS, item.getY());

        Setup.logger().log(this, Log.INFO, null, "updateItem: %s (ID: %d)", item != null ? item.getLabel() : "NULL", item != null ? item.getId() : -1);

        StringBuilder concat = new StringBuilder();
        switch (item.getType()) {
            case APP:
                if (Setup.appSettings().enableImageCaching()) {
                    Tool.saveIcon(context, Tool.drawableToBitmap(item.getIconProvider().getDrawableSynchronously(Config.NO_SCALE)), Integer.toString(item.getId()));
                }
                itemValues.put(COLUMN_DATA, Tool.getIntentAsString(item.getIntent()));
                break;
            case GROUP:
                for (Item tmp : item.getItems()) {
                    concat.append(tmp.getId()).append(Config.INT_SEP);
                }
                itemValues.put(COLUMN_DATA, concat.toString());
                break;
            case ACTION:
                itemValues.put(COLUMN_DATA, item.getActionValue());
                break;
            case WIDGET:
                concat = new StringBuilder(Integer.toString(item.getWidgetValue()) + Config.INT_SEP
                        + Integer.toString(item.getSpanX()) + Config.INT_SEP
                        + Integer.toString(item.getSpanY()));
                itemValues.put(COLUMN_DATA, concat.toString());
                break;
        }
        db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item.getId(), null);
    }

    // update the state of an item
    private void updateItem(Item item, Config.ItemState state) {
        ContentValues itemValues = new ContentValues();
        Setup.logger().log(this, Log.INFO, null, "updateItem (state): %s (ID: %d)", item != null ? item.getLabel() : "NULL", item != null ? item.getId() : -1);
        itemValues.put(COLUMN_STATE, state.ordinal());
        db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item.getId(), null);
    }

    // update the fields only used by the database
    private void updateItem(Item item, int page, Config.ItemPosition itemPosition) {
        Setup.logger().log(this, Log.INFO, null, "updateItem (delete + create): %s (ID: %d)", item != null ? item.getLabel() : "NULL", item != null ? item.getId() : -1);
        deleteItem(item, false);
        createItem(item, page, itemPosition);
    }

    private Item getSelection(Cursor cursor) {
        Item item = new Item();
        int id = Integer.parseInt(cursor.getString(0));
        Item.Type type = Item.Type.valueOf(cursor.getString(1));
        String label = cursor.getString(2);
        int x = Integer.parseInt(cursor.getString(3));
        int y = Integer.parseInt(cursor.getString(4));
        String data = cursor.getString(5);

        item.setItemId(id);
        item.setLabel(label);
        item.setX(x);
        item.setY(y);
        item.setType(type);

        String[] dataSplit;
        switch (type) {
            case APP:
            case SHORTCUT:
                item.setIntent(Tool.getIntentFromString(data));
                if (Setup.appSettings().enableImageCaching()) {
                    item.setIconProvider(Setup.get().getImageLoader().createIconProvider(Tool.getIcon(HomeActivity.Companion.getLauncher(), Integer.toString(id))));
                } else {
                    switch (type) {
                        case APP:
                        case SHORTCUT:
                            App app = Setup.get().getAppLoader().findItemApp(item);
                            item.setIconProvider(app != null ? Setup.imageLoader().createIconProvider(app.getIcon()) : null);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case GROUP:
                item.setItems(new ArrayList<>());
                dataSplit = data.split(Config.INT_SEP);
                for (String s : dataSplit) {
                    item.getItems().add(getItem(Integer.parseInt(s)));
                }
                break;
            case ACTION:
                item.setActionValue(Integer.parseInt(data));
                break;
            case WIDGET:
                dataSplit = data.split(Config.INT_SEP);
                item.setWidgetValue(Integer.parseInt(dataSplit[0]));
                item.setSpanX(Integer.parseInt(dataSplit[1]));
                item.setSpanY(Integer.parseInt(dataSplit[2]));
                break;
        }
        return item;
    }
}
