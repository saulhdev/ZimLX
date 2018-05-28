package org.zimmob.zimlx.util;

public interface IDialogListener {

    interface OnAddAppDrawerItemListener {
        void onAdd(int type);
    }

    interface OnEditDialogListener {
        void onRename(String name);
    }
}
