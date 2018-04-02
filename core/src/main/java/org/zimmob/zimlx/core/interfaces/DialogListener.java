package org.zimmob.zimlx.core.interfaces;

public interface DialogListener {

    interface OnAddAppDrawerItemListener {
        void onAdd();
    }

    interface OnEditDialogListener {
        void onRename(String name);
    }
}
