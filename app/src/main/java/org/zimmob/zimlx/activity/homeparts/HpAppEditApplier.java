package org.zimmob.zimlx.activity.homeparts;

import android.graphics.Point;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.interfaces.DialogListener;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.widget.Desktop;
import org.zimmob.zimlx.widget.Dock;

public class HpAppEditApplier implements DialogListener.OnEditDialogListener {
    private HomeActivity _homeActivity;
    private Item _item;

    public HpAppEditApplier(HomeActivity home) {
        _homeActivity = home;
    }

    public void onEditItem(final Item item) {
        _item = item;
        Setup.eventHandler().showEditDialog(_homeActivity, item, this);
    }

    @Override
    public void onRename(String name) {
        _item.setLabel(name);
        Setup.dataManager().saveItem(_item);
        Point point = new Point(_item.x, _item.y);

        switch (_item._locationInLauncher) {
            case Item.LOCATION_DESKTOP: {
                Desktop desktop = _homeActivity.getDesktop();
                desktop.removeItem(desktop.getCurrentPage().coordinateToChildView(point), false);
                desktop.addItemToCell(_item, _item.x, _item.y);
                break;
            }
            case Item.LOCATION_DOCK: {
                Dock dock = _homeActivity.getDock();
                _homeActivity.getDock().removeItem(dock.coordinateToChildView(point), false);
                dock.addItemToCell(_item, _item.x, _item.y);
                break;
            }
        }
    }
}
