package org.zimmob.zimlx.activity.homeparts;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.zimmob.zimlx.interfaces.DialogListener;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.Definitions;
import org.zimmob.zimlx.util.LauncherAction;
import org.zimmob.zimlx.viewutil.DialogHelper;

public class HpEventHandler implements Setup.EventHandler {
    @Override
    public void showLauncherSettings(Context context) {
        LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context);
    }

    @Override
    public void showPickAction(Context context, final DialogListener.OnAddAppDrawerItemListener listener) {
        DialogHelper.addActionItemDialog(context, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                if (position == 0) {
                    listener.onAdd(Definitions.ACTION_LAUNCHER);
                }
            }
        });

    }

    @Override
    public void showEditDialog(Context context, Item item, final DialogListener.OnEditDialogListener listener) {
        DialogHelper.editItemDialog("Edit Item", item.getLabel(), context, new DialogHelper.OnItemEditListener() {
            @Override
            public void itemLabel(String label) {
                listener.onRename(label);
            }
        });
    }

    @Override
    public void showDeletePackageDialog(Context context, Item item) {
        DialogHelper.deletePackageDialog(context, item);
    }
}
