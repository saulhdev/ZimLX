package org.zimmob.zimlx.folder;

public interface IFolderListener {
    //public void onAdd(ShortcutInfo item, int rank);
    //public void onRemove(ShortcutInfo item);
    //public void onTitleChanged(CharSequence title);
    void onItemsChanged(boolean animate);
    //public void prepareAutoUpdate();
}
