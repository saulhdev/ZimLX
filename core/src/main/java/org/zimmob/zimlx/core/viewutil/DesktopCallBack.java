package org.zimmob.zimlx.core.viewutil;

import android.view.View;

import org.zimmob.zimlx.core.model.Item;
import org.zimmob.zimlx.core.util.RevertibleAction;

public interface DesktopCallBack<V extends View> extends RevertibleAction {
    boolean addItemToPoint(Item item, int x, int y);

    boolean addItemToPage(Item item, int page);

    boolean addItemToCell(Item item, int x, int y);

    void removeItem(V view, boolean animate);
}
