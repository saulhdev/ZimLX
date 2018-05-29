package org.zimmob.zimlx.activity.homeparts;

import android.content.Context;

import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.DialogHelper;
import org.zimmob.zimlx.util.IDialogListener;
import org.zimmob.zimlx.launcher.LauncherAction;

public class HpEventHandler implements Setup.EventHandler {
    @Override
    public void showLauncherSettings(Context context) {
        LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
    }

    @Override
    public void showPickAction(Context context, final IDialogListener.OnAddAppDrawerItemListener listener) {
        DialogHelper.addActionItemDialog(context, (dialog, itemView, position, text) -> {
            if (position == 0) {
                listener.onAdd(Config.ACTION_LAUNCHER);
            }
        });

    }

    @Override
    public void showEditDialog(Context context, Item item, final IDialogListener.OnEditDialogListener listener) {
        DialogHelper.editItemDialog("Edit Item", item.getLabel(), context, label -> listener.onRename(label));
    }

    @Override
    public void showDeletePackageDialog(Context context, Item item) {
        DialogHelper.deletePackageDialog(context, item);
    }
}
