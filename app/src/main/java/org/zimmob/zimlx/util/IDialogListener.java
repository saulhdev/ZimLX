package org.zimmob.zimlx.util;

public interface IDialogListener {

    interface OnActionDialogListener {
        void onAdd(int type);
    }

    interface OnEditDialogListener {
        void onRename(String name);
    }
}
