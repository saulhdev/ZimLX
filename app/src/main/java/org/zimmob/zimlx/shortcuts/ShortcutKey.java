package org.zimmob.zimlx.shortcuts;

import android.content.ComponentName;
import android.content.Intent;
import android.os.UserHandle;

import org.zimmob.zimlx.ItemInfo;
import org.zimmob.zimlx.ShortcutInfo;
import org.zimmob.zimlx.util.ComponentKey;

/**
 * A key that uniquely identifies a shortcut using its package, id, and user handle.
 */
public class ShortcutKey extends ComponentKey {

    public ShortcutKey(String packageName, UserHandle user, String id) {
        // Use the id as the class name.
        super(new ComponentName(packageName, id), user);
    }

    public static ShortcutKey fromInfo(ShortcutInfoCompat shortcutInfo) {
        return new ShortcutKey(shortcutInfo.getPackage(), shortcutInfo.getUserHandle(),
                shortcutInfo.getId());
    }

    public static ShortcutKey fromIntent(Intent intent, UserHandle user) {
        String shortcutId = intent.getStringExtra(
                ShortcutInfoCompat.EXTRA_SHORTCUT_ID);
        return new ShortcutKey(intent.getPackage(), user, shortcutId);
    }

    public static ShortcutKey fromShortcutInfo(ShortcutInfo info) {
        return fromIntent(info.getPromisedIntent(), info.user);
    }

    public static ShortcutKey fromItemInfo(ItemInfo itemInfo) {
        return fromIntent(itemInfo.getIntent(), itemInfo.user);
    }

    public String getId() {
        return componentName.getClassName();
    }
}
