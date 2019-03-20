package com.android.launcher3.allapps;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.DeleteDropTarget;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Insettable;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.PagedView;
import com.android.launcher3.R;
import com.android.launcher3.dragndrop.DragController;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.userevent.nano.LauncherLogProto;

public class AllAppsPagedView extends PagedView implements DragSource,
        View.OnLongClickListener, Insettable {
    private final AlphabeticalAppsList mApps;
    private Launcher mLauncher;
    private int mNumAppsRows;
    private int mNumAppsColums;
    private int mNumAppsPerPage;
    private int mNumAppsPages;
    private SearchUiManager mSearchUiManager;
    private View mSearchContainer;
    private AllAppsRecyclerView mAppsRecyclerView;

    public AllAppsPagedView(Context context) {
        this(context, null, 0);
    }

    public AllAppsPagedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsPagedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = Launcher.getLauncher(context);
        mApps = new AlphabeticalAppsList(context);
        DeviceProfile grid = mLauncher.getDeviceProfile();
        mNumAppsColums = grid.inv.numColumns;
        mNumAppsRows = grid.inv.numRows;
        mNumAppsPerPage = mNumAppsColums * mNumAppsRows;

    }


    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete,
                                boolean success) {
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
            mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        }
        mLauncher.unlockScreenOrientation(false);

        if (!success) {
            d.deferDragViewCleanupPostAnimation = false;
        }
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        return (float) grid.allAppsIconSizePx / grid.iconSizePx;
    }

    @Override
    public void fillInLogContainerData(View v, ItemInfo info, LauncherLogProto.Target target, LauncherLogProto.Target targetParent) {
        // This is filled in {@link AllAppsRecyclerView}
    }

    @Override
    public void setInsets(Rect insets) {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        mAppsRecyclerView.setPadding(
                mAppsRecyclerView.getPaddingLeft(), mAppsRecyclerView.getPaddingTop(),
                mAppsRecyclerView.getPaddingRight(), insets.bottom);

        if (grid.isVerticalBarLayout()) {
            ViewGroup.MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.topMargin = insets.top;
            mlp.rightMargin = insets.right;
            setLayoutParams(mlp);
        } else {
            View navBarBg = findViewById(R.id.nav_bar_bg);
            ViewGroup.LayoutParams navBarBgLp = navBarBg.getLayoutParams();
            navBarBgLp.height = insets.bottom;
            navBarBg.setLayoutParams(navBarBgLp);
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        // When we have exited all apps or are in transition, disregard long clicks
        if (!mLauncher.isAppsViewVisible() ||
                mLauncher.getWorkspace().isSwitchingState()) return false;
        // Return if global dragging is not enabled or we are already dragging
        if (!mLauncher.isDraggingEnabled()) return false;
        if (mLauncher.getDragController().isDragging()) return false;

        // Start the drag
        final DragController dragController = mLauncher.getDragController();
        dragController.addDragListener(new DragController.DragListener() {
            @Override
            public void onDragStart(DropTarget.DragObject dragObject, DragOptions options) {
                v.setVisibility(INVISIBLE);
            }

            @Override
            public void onDragEnd() {
                v.setVisibility(VISIBLE);
                dragController.removeDragListener(this);
            }
        });
        mLauncher.getWorkspace().beginDragShared(v, this, new DragOptions());
        return false;
    }
}
