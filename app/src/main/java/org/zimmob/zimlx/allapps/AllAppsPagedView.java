package org.zimmob.zimlx.allapps;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;

import org.zimmob.zimlx.BubbleTextView;
import org.zimmob.zimlx.CellLayout;
import org.zimmob.zimlx.DeviceProfile;
import org.zimmob.zimlx.InvariantDeviceProfile;
import org.zimmob.zimlx.ItemInfo;
import org.zimmob.zimlx.Launcher;
import org.zimmob.zimlx.LauncherAppState;
import org.zimmob.zimlx.PagedViewX;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.ShortcutInfo;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.keyboard.ViewGroupFocusHelper;
import org.zimmob.zimlx.pageindicators.PageIndicator;

import java.util.ArrayList;
import java.util.Iterator;

public class AllAppsPagedView extends PagedViewX<PersonalWorkSlidingTabStrip> {
    private final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
    private final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
    private final static float TOUCH_SLOP_DAMPING_FACTOR = 4;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxCountX;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxCountY;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final int mMaxItemsPerPage;
    private final LayoutInflater mInflater;
    private final ViewGroupFocusHelper mFocusIndicatorHelper;
    private PageIndicator mPageIndicator;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mGridCountX;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mGridCountY;
    private int mAllocatedContentSize;

    public AllAppsPagedView(Context context) {
        this(context, null);
    }

    public AllAppsPagedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsPagedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LauncherAppState app = LauncherAppState.getInstance();

        InvariantDeviceProfile profile = app.getInvariantDeviceProfile();
        mMaxCountX = profile.numColumnsDrawer;
        mMaxCountY = profile.numRowsDrawer;

        mMaxItemsPerPage = mMaxCountX * mMaxCountY;

        mInflater = LayoutInflater.from(context);

        mIsRtl = Utilities.isRtl(getResources());
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

        mFocusIndicatorHelper = new ViewGroupFocusHelper(this);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mPageIndicator.setScroll(l, mMaxScrollX);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mFocusIndicatorHelper.draw(canvas);
        super.dispatchDraw(canvas);
    }
    protected void determineScrollingStart(MotionEvent ev) {
        //float absDeltaX = Math.abs(ev.getX() - getDownMotionX());
        float absDeltaX = Math.abs(ev.getX());
        //float absDeltaY = Math.abs(ev.getY() - getDownMotionY());
        float absDeltaY = Math.abs(ev.getY());

        if (Float.compare(absDeltaX, 0f) == 0) return;

        float slope = absDeltaY / absDeltaX;
        float theta = (float) Math.atan(slope);

        if (absDeltaX > mTouchSlop || absDeltaY > mTouchSlop) {
            cancelCurrentPageLongPress();
        }

        if (theta > MAX_SWIPE_ANGLE) {
            return;
        } else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE) {
            theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
            float extraRatio = (float)
                    Math.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
            super.determineScrollingStart(ev, 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio);
        } else {
            super.determineScrollingStart(ev);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    /**
     * Binds items to the layout.
     */
    public void bindItems(ArrayList<ShortcutInfo> items) {
        ArrayList<View> icons = new ArrayList<>();

        for (ShortcutInfo item : items) {
            icons.add(createNewView(item));
        }
        arrangeChildren(icons, icons.size(), false);
    }

    public void arrangeChildren(ArrayList<View> list, int itemCount) {
        arrangeChildren(list, itemCount, true);
    }

    private void arrangeChildren(ArrayList<View> list, int itemCount, boolean saveChanges) {
        ArrayList<CellLayout> pages = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout page = (CellLayout) getChildAt(i);
            page.removeAllViews();
            pages.add(page);
        }
        setupContentDimensions(itemCount);

        Iterator<CellLayout> pageItr = pages.iterator();
        CellLayout currentPage = null;

        int position = 0;
        int newX, newY, rank;

        rank = 0;
        for (int i = 0; i < itemCount; i++) {
            View v = list.size() > i ? list.get(i) : null;
            if (currentPage == null || position >= mMaxItemsPerPage) {
                // Next page
                if (pageItr.hasNext()) {
                    currentPage = pageItr.next();
                } else {
                    currentPage = createAndAddNewPage();
                }
                position = 0;
            }

            if (v != null) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
                newX = position % mGridCountX;
                newY = position / mGridCountX;
                ItemInfo info = (ItemInfo) v.getTag();
                if (info.cellX != newX || info.cellY != newY || info.rank != rank) {
                    info.cellX = newX;
                    info.cellY = newY;
                    info.rank = rank;
                    if (saveChanges) {
                        //LauncherModel.addOrMoveItemInDatabase(getContext(), info,
                        //        mFolder.mInfo.id, 0, info.cellX, info.cellY);
                    }
                }
                lp.cellX = info.cellX;
                lp.cellY = info.cellY;
                /*currentPage.addViewToCellLayout(
                        v, -1, mFolder.mLauncher.getViewIdForItem(info), lp, true);

                if (rank < FolderIcon.NUM_ITEMS_IN_PREVIEW && v instanceof BubbleTextView) {
                    ((BubbleTextView) v).verifyHighRes();
                }*/
            }

            rank++;
            position++;
        }

        // Remove extra views.
        boolean removed = false;
        while (pageItr.hasNext()) {
            removeView(pageItr.next());
            removed = true;
        }
        if (removed) {
            setCurrentPage(0);
        }

        setEnableOverscroll(getPageCount() > 1);

        // Update footer
        mPageIndicator.setVisibility(getPageCount() > 1 ? View.VISIBLE : View.GONE);
        // Set the gravity as LEFT or RIGHT instead of START, as START depends on the actual text.
        //mFolder.mFolderName.setGravity(getPageCount() > 1 ?
        //        (mIsRtl ? Gravity.RIGHT : Gravity.LEFT) : Gravity.CENTER_HORIZONTAL);
    }

    public View createNewView(ShortcutInfo item) {
        final BubbleTextView textView = (BubbleTextView) mInflater.inflate(
                R.layout.folder_application, null, false);
        textView.applyFromShortcutInfo(item);
        //textView.setOnClickListener(mFolder);
        //textView.setOnLongClickListener(mFolder);
        textView.setOnFocusChangeListener(mFocusIndicatorHelper);
        //textView.setOnKeyListener(mKeyListener);

        textView.setLayoutParams(new CellLayout.LayoutParams(
                item.cellX, item.cellY, item.spanX, item.spanY));
        return textView;
    }

    private void setupContentDimensions(int count) {
        mAllocatedContentSize = count;
        boolean done;
        if (count >= mMaxItemsPerPage) {
            mGridCountX = mMaxCountX;
            mGridCountY = mMaxCountY;
            done = true;
        } else {
            done = false;
        }

        while (!done) {
            int oldCountX = mGridCountX;
            int oldCountY = mGridCountY;
            if (mGridCountX * mGridCountY < count) {
                // Current grid is too small, expand it
                if ((mGridCountX <= mGridCountY || mGridCountY == mMaxCountY) && mGridCountX < mMaxCountX) {
                    mGridCountX++;
                } else if (mGridCountY < mMaxCountY) {
                    mGridCountY++;
                }
                if (mGridCountY == 0) mGridCountY++;
            } else if ((mGridCountY - 1) * mGridCountX >= count && mGridCountY >= mGridCountX) {
                mGridCountY = Math.max(0, mGridCountY - 1);
            } else if ((mGridCountX - 1) * mGridCountY >= count) {
                mGridCountX = Math.max(0, mGridCountX - 1);
            }
            done = mGridCountX == oldCountX && mGridCountY == oldCountY;
        }

        // Update grid size
        for (int i = getPageCount() - 1; i >= 0; i--) {
            //getPageAt(i).setGridSize(mGridCountX, mGridCountY);
        }
    }

    private CellLayout createAndAddNewPage() {
        DeviceProfile grid = Launcher.getLauncher(getContext()).getDeviceProfile();
        CellLayout page = new CellLayout(getContext());
        page.setCellDimensions(grid.folderCellWidthPx, grid.folderCellHeightPx);
        page.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
        page.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        page.setInvertIfRtl(true);
        page.setGridSize(mGridCountX, mGridCountY);

        addView(page, -1, generateDefaultLayoutParams());
        return page;
    }

}
