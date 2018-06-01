package org.zimmob.zimlx.badge;

import org.zimmob.zimlx.config.Config;

public class FolderBadgeInfo extends BadgeInfo {
    private static final int MIN_COUNT = 0;

    private int mNumNotifications;

    public FolderBadgeInfo() {
        super(null);
    }

    public void addBadgeInfo(BadgeInfo badgeToAdd) {
        if (badgeToAdd == null) {
            return;
        }
        mNumNotifications += badgeToAdd.getNotificationKeys().size();
        mNumNotifications = Config.boundToRange(
                mNumNotifications, MIN_COUNT, BadgeInfo.MAX_COUNT);
    }

    public void subtractBadgeInfo(BadgeInfo badgeToSubtract) {
        if (badgeToSubtract == null) {
            return;
        }
        mNumNotifications -= badgeToSubtract.getNotificationKeys().size();
        mNumNotifications = Config.boundToRange(
                mNumNotifications, MIN_COUNT, BadgeInfo.MAX_COUNT);
    }

    @Override
    public int getNotificationCount() {
        // This forces the folder badge to always show up as a dot.
        return 0;
    }

    public boolean hasBadge() {
        return mNumNotifications > 0;
    }
}
