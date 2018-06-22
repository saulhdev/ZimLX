package org.zimmob.zimlx.activity.homeparts;

import android.graphics.Point;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.IDialogListener;
import org.zimmob.zimlx.util.Tool;

public class HpDesktopPickAction implements IDialogListener.OnAddAppDrawerItemListener {
    private HomeActivity _homeActivity;

    public HpDesktopPickAction(HomeActivity launcher) {
        _homeActivity = launcher;
    }

    public void onPickDesktopAction() {
        Setup.eventHandler().showPickAction(_homeActivity, this);
    }

    @Override
    public void onAdd(int type) {
        Point pos = _homeActivity.getDesktop().getCurrentPage().findFreeSpace();
        if (pos != null) {
            _homeActivity.getDesktop().addItemToCell(Item.newActionItem(type), pos.x, pos.y);
        } else {
            Tool.toast(_homeActivity, R.string.toast_not_enough_space);
        }
    }
}
