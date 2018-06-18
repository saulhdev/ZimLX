package org.zimmob.zimlx.util;

import android.os.Build;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;

import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.model.ItemInfo;

import java.util.Arrays;

public class PackageUserKey {
    public String mPackageName;
    public UserHandle mUser;
    private int mHashCode;

    public static PackageUserKey fromItemInfo(Item info) {
        //return new PackageUserKey(info.getTargetComponent().getPackageName(), info.user);
        return new PackageUserKey(info.getIntent().getPackage(), null);
    }

    public static PackageUserKey fromNotification(StatusBarNotification notification) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PackageUserKey(notification.getPackageName(), notification.getUser());
        }

        return new PackageUserKey(notification.getPackageName(),null);
    }

    public PackageUserKey(String packageName, UserHandle user) {
        update(packageName, user);
    }

    private void update(String packageName, UserHandle user) {
        mPackageName = packageName;
        mUser = user;
        mHashCode = Arrays.hashCode(new Object[] {packageName, user});
    }

    /**
     * This should only be called to avoid new object creations in a loop.
     * @return Whether this PackageUserKey was successfully updated - it shouldn't be used if not.
     */
    public boolean updateFromItemInfo(ItemInfo info) {
        /*if (DeepShortcutManager.supportsShortcuts(info)) {
            update(info.getTargetComponent().getPackageName(), info.user);
            return true;
        }*/
        return false;
    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PackageUserKey)) return false;
        PackageUserKey otherKey = (PackageUserKey) obj;
        return mPackageName.equals(otherKey.mPackageName) && mUser.equals(otherKey.mUser);
    }
}
