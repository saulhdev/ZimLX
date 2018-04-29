package org.zimmob.zimlx.activity.homeparts;

import android.graphics.Point;

import org.zimmob.zimlx.activity.Home;
import org.zimmob.zimlx.interfaces.DialogListener;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.widget.Desktop;
import org.zimmob.zimlx.widget.Dock;

public class HpAppEditApplier implements DialogListener.OnEditDialogListener {
    private Home _home;
    private Item _item;

    public HpAppEditApplier(Home home) {
        _home = home;
    }

    public void onEditItem(final Item item) {
        _item = item;
        Setup.eventHandler().showEditDialog(_home, item, this);
    }

    @Override
    public void onRename(String name) {
        _item.setLabel(name);
        Setup.dataManager().saveItem(_item);
        Point point = new Point(_item._x, _item._y);

        switch (_item._locationInLauncher) {
            case Item.LOCATION_DESKTOP: {
                Desktop desktop = _home.getDesktop();
                desktop.removeItem(desktop.getCurrentPage().coordinateToChildView(point), false);
                desktop.addItemToCell(_item, _item._x, _item._y);
                break;
            }
            case Item.LOCATION_DOCK: {
                Dock dock = _home.getDock();
                _home.getDock().removeItem(dock.coordinateToChildView(point), false);
                dock.addItemToCell(_item, _item._x, _item._y);
                break;
            }
        }
    }
}
