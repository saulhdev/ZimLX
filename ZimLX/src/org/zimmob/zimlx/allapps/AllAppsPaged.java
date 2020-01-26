package org.zimmob.zimlx.allapps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.PagedView;
import com.android.launcher3.Utilities;
import com.android.launcher3.pageindicators.PageIndicatorDots;

public class AllAppsPaged extends PagedView<PageIndicatorDots> {

    private static final String TAG = "FolderPagedView";
    private static final int[] sTmpArray = new int[2];
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxCountX;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxCountY;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxItemsPerPage;

    private int mAllocatedContentSize;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mGridCountX;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mGridCountY;

    public AllAppsPaged(Context context, AttributeSet attrs) {
        super(context, attrs);
        InvariantDeviceProfile profile = LauncherAppState.getIDP(context);
        mMaxCountX = profile.numColsDrawer;
        mMaxCountY = profile.numRows;

        mMaxItemsPerPage = mMaxCountX * mMaxCountY;

        mIsRtl = Utilities.isRtl(getResources());
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

    }


}
