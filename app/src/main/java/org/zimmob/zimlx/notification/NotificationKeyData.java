package org.zimmob.zimlx.notification;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NotificationKeyData {
    public final String notificationKey;
    public final String shortcutId;
    public int count;

    private NotificationKeyData(String notificationKey, String shortcutId, int count) {
        this.notificationKey = notificationKey;
        this.shortcutId = shortcutId;
        this.count = Math.max(1, count);
    }

    /*public static NotificationKeyData fromNotification(StatusBarNotification sbn) {
        Notification notif = sbn.getNotification();
        return new NotificationKeyData(sbn.getKey(), notif.getShortcutId(), notif.number);
    }

    public static List<String> extractKeysOnly(@NonNull List<NotificationKeyData> notificationKeys) {
        List<String> keysOnly = new ArrayList<>(notificationKeys.size());
        for (NotificationKeyData notificationKeyData : notificationKeys) {
            keysOnly.add(notificationKeyData.notificationKey);
        }
        return keysOnly;
    }
*/
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NotificationKeyData)) {
            return false;
        }
        // Only compare the keys.
        return ((NotificationKeyData) obj).notificationKey.equals(notificationKey);
    }
}
