package org.zimmob.zimlx.interfaces;

import android.view.View;

import org.zimmob.zimlx.model.Item;

public interface DesktopCallBack<V extends View> extends RevertibleAction {
    boolean addItemToPoint(Item item, int x, int y);

    boolean addItemToPage(Item item, int page);

    boolean addItemToCell(Item item, int x, int y);

    void removeItem(V view, boolean animate);
}
