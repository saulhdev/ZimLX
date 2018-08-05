package org.zimmob.zimlx.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.icon.SimpleIconProvider;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.Tool;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Item implements Parcelable {

    public static final int LOCATION_DESKTOP = 0;
    public static final int LOCATION_DOCK = 1;
    public Type type;
    public Drawable icon;
    public int x = 0;
    public int y = 0;
    //Needed for folder to optimize the folder open position
    public int _locationInLauncher;
    // intent for shortcuts and apps
    public Intent intent;
    // list of items for groups
    private List<Item> items;
    // int value for launcher action
    private int _actionValue;
    // widget specific values
    private int _widgetValue;
    public int spanX = 1;
    public int spanY = 1;
    // all items need these values
    private int _idValue;
    private String name = "";
    private String packageName = "";

    public SimpleIconProvider iconProvider = null;


    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel parcel) {
            return new Item(parcel);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public Item() {
        Random random = new Random();
        _idValue = random.nextInt();
    }

    /**
     * @param parcel
     */
    public Item(Parcel parcel) {
        _idValue = parcel.readInt();
        type = Type.valueOf(parcel.readString());
        name = parcel.readString();
        packageName = parcel.readString();
        x = parcel.readInt();
        y = parcel.readInt();
        switch (type) {
            case APP:
            case SHORTCUT:
                intent = Tool.getIntentFromString(parcel.readString());
                break;
            case GROUP:
                List<String> labels = new ArrayList<>();
                parcel.readStringList(labels);
                items = new ArrayList<>();
                for (String s : labels) {
                    items.add(HomeActivity._db.getItem(Integer.parseInt(s)));
                }
                break;
            case ACTION:
                _actionValue = parcel.readInt();
                break;
            case WIDGET:
                _widgetValue = parcel.readInt();
                spanX = parcel.readInt();
                spanY = parcel.readInt();
                break;
        }
        _locationInLauncher = parcel.readInt();

        if (Setup.appSettings().enableImageCaching()) {
            icon = Tool.getIcon(HomeActivity.companion.getLauncher(), Integer.toString(_idValue));
            iconProvider = Setup.imageLoader().createIconProvider(Tool.getIcon(HomeActivity.companion.getLauncher(), Integer.toString(_idValue)));

        } else {
            switch (type) {
                case APP:
                case SHORTCUT:
                    App app = Setup.appLoader().findItemApp(this);
                    icon = app != null ? app.getIcon() : null;
                    break;
                default:
                    break;
            }
        }
    }

    public static Item newAppItem(App app) {
        Item item = new Item();
        item.type = Type.APP;
        item.name = app.getLabel();
        item.icon = app.getIcon();
        item.intent = toIntent(app);
        item.packageName = app.getPackageName();
        item.iconProvider = Setup.imageLoader().createIconProvider(app.getIcon());
        return item;
    }

    public static Item newShortcutItem(Intent intent, Drawable icon, String name) {
        Item item = new Item();
        item.type = Type.SHORTCUT;
        item.name = name;
        item.icon = icon;
        item.spanX = 1;
        item.spanY = 1;
        item.intent = intent;
        item.iconProvider = Setup.imageLoader().createIconProvider(icon);

        return item;
    }

    public static Item newGroupItem() {
        Item item = new Item();
        item.type = Type.GROUP;
        item.name = "";
        item.spanX = 1;
        item.spanY = 1;
        item.items = new ArrayList<>();

        return item;
    }

    public static Item newActionItem(int action) {
        Item item = new Item();
        item.type = Type.ACTION;
        item.spanX = 1;
        item.spanY = 1;
        item._actionValue = action;
        return item;
    }

    public static Item newWidgetItem(int widgetValue) {
        Item item = new Item();
        item.type = Type.WIDGET;
        item._widgetValue = widgetValue;
        item.spanX = 1;
        item.spanY = 1;

        return item;
    }

    private static Intent toIntent(App app) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(app.getPackageName(), app.getClassName());
        return intent;
    }

    @Override
    public boolean equals(Object object) {
        Item itemObject = (Item) object;
        return object != null && _idValue == itemObject._idValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(_idValue);
        out.writeString(type.toString());
        out.writeString(name);
        out.writeInt(x);
        out.writeInt(y);
        switch (type) {
            case APP:
            case SHORTCUT:
                out.writeString(Tool.getIntentAsString(intent));
                break;
            case GROUP:
                List<String> labels = new ArrayList<>();
                for (Item i : items) {
                    labels.add(Integer.toString(i._idValue));
                }
                out.writeStringList(labels);
                break;
            case ACTION:
                out.writeInt(_actionValue);
                break;
            case WIDGET:
                out.writeInt(_widgetValue);
                out.writeInt(spanX);
                out.writeInt(spanY);
                break;
        }
        out.writeInt(_locationInLauncher);
    }

    public void reset() {
        Random random = new Random();
        _idValue = random.nextInt();
    }

    public Integer getId() {
        return _idValue;
    }

    public void setItemId(int id) {
        _idValue = id;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getLabel() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setLabel(String label) {
        name = label;
    }

    public Type getType() {
        return type;
    }

    public List<Item> getGroupItems() {
        Collections.sort(items, (p1, p2) -> Collator.getInstance().compare(
                p1.name, p2.name
        ));
        return items;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSpanX() {
        return spanX;
    }

    public void setSpanX(int x) {
        spanX = x;
    }

    public int getSpanY() {
        return spanY;
    }

    public void setSpanY(int y) {
        spanY = y;
    }

    public Drawable getIcon() {
        return icon;
    }

    public SimpleIconProvider getIconProvider() {
        return iconProvider;
    }

    public enum Type {
        APP,
        SHORTCUT,
        GROUP,
        ACTION,
        WIDGET
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getLocationInLauncher() {
        return _locationInLauncher;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public int getActionValue() {
        return _actionValue;
    }

    public void setActionValue(int actionValue) {
        _actionValue = actionValue;
    }

    public int getWidgetValue() {
        return _widgetValue;
    }

    public void setWidgetValue(int widgetValue) {
        _widgetValue = widgetValue;
    }
}
