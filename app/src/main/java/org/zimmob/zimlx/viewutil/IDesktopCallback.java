package org.zimmob.zimlx.viewutil;

import android.view.View;

import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.IRevertibleAction;

public interface IDesktopCallback<V extends View> extends IRevertibleAction {
    boolean addItemToPoint(Item item, int x, int y);

    boolean addItemToPage(Item item, int page);

    boolean addItemToCell(Item item, int x, int y);

    void removeItem(V view, boolean animate);
}
