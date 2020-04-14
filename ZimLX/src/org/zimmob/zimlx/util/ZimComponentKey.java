package org.zimmob.zimlx.util;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.Utilities;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.Preconditions;

import java.util.Arrays;

public class ZimComponentKey extends ComponentKey {
    public ZimComponentKey(ComponentName componentName, UserHandle user) {
        super(componentName, user);
    }

    /**
     * Creates a new component key from an encoded component key string in the form of
     * [flattenedComponentString#userId].  If the userId is not present, then it defaults
     * to the current user.
     */
    public ZimComponentKey(Context context, String componentKeyStr) {
        super(new ComponentName(context, componentKeyStr), Utilities.myUserHandle());
        int userDelimiterIndex = componentKeyStr.indexOf("#");
        if (userDelimiterIndex != -1) {
            String componentStr = componentKeyStr.substring(0, userDelimiterIndex);
            Long componentUser = Long.valueOf(componentKeyStr.substring(userDelimiterIndex + 1));
            componentName = ComponentName.unflattenFromString(componentStr);
            user = Utilities.notNullOrDefault(UserManagerCompat.getInstance(context)
                    .getUserForSerialNumber(componentUser.longValue()), Utilities.myUserHandle());
        } else {
            // No user provided, default to the current user
            componentName = ComponentName.unflattenFromString(componentKeyStr);
            user = Utilities.myUserHandle();
        }
        Preconditions.assertNotNull(componentName);
        Preconditions.assertNotNull(user);
        mHashCode = Arrays.hashCode(new Object[]{componentName, user});

    }
}
