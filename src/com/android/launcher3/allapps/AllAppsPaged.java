package com.android.launcher3.allapps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.PagedView;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.pageindicators.PageIndicatorDots;
import com.android.launcher3.userevent.nano.LauncherLogProto;

public class AllAppsPaged extends PagedView<PageIndicatorDots> implements UserEventDispatcher.LogContainerProvider {
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxCountX;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxCountY;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxItemsPerPage;

    private AlphabeticalAppsList mApps;

    public AllAppsPaged(Context context) {
        this(context, null, 0);
    }

    public AllAppsPaged(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsPaged(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        InvariantDeviceProfile profile = LauncherAppState.getIDP(context);
        mMaxCountX = profile.numRows;
        mMaxCountY = profile.numColumns;

        mMaxItemsPerPage = mMaxCountX * mMaxCountY;
    }


    /**
     * Sets the list of apps in this view, used to determine the fastscroll position.
     */
    public void setApps(AlphabeticalAppsList apps, boolean usingTabs) {
        mApps = apps;
    }

    public AlphabeticalAppsList getApps() {
        return mApps;
    }

    @Override
    public void fillInLogContainerData(View v, ItemInfo info, LauncherLogProto.Target target, LauncherLogProto.Target targetParent) {
        if (mApps.hasFilter()) {
            targetParent.containerType = LauncherLogProto.ContainerType.SEARCHRESULT;
        } else {
            targetParent.containerType = LauncherLogProto.ContainerType.ALLAPPS;
        }
    }
}
