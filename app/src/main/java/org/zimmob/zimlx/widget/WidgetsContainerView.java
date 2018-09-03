package org.zimmob.zimlx.widget;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import org.zimmob.zimlx.BaseContainerView;
import org.zimmob.zimlx.DeleteDropTarget;
import org.zimmob.zimlx.DragSource;
import org.zimmob.zimlx.DropTarget;
import org.zimmob.zimlx.Launcher;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.config.FeatureFlags;
import org.zimmob.zimlx.dragndrop.DragOptions;
import org.zimmob.zimlx.folder.Folder;
import org.zimmob.zimlx.util.MultiHashMap;
import org.zimmob.zimlx.util.PackageUserKey;

import java.util.List;

public class WidgetsContainerView extends BaseContainerView implements OnLongClickListener, OnClickListener, DragSource {
    Launcher mLauncher;
    private WidgetsListAdapter mAdapter;
    private WidgetsRecyclerView mRecyclerView;
    private Toast mWidgetInstructionToast;

    public WidgetsContainerView(Context context) {
        this(context, null);
    }

    public WidgetsContainerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidgetsContainerView(Context context, AttributeSet attributeSet, int i) {
        super(new ContextThemeWrapper(context, getTheme()), attributeSet, i);
        this.mLauncher = Launcher.getLauncher(context);
        this.mAdapter = new WidgetsListAdapter(this, this, context);
    }

    private static int getTheme() {
        return FeatureFlags.getUseDarkTheme() ? R.style.WidgetContainerTheme_Dark : R.style.WidgetContainerTheme;
    }

    public View getTouchDelegateTargetView() {
        return this.mRecyclerView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mRecyclerView = getContentView().findViewById(R.id.widgets_list_view);
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void scrollToTop() {
        this.mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onClick(View view) {
        if (this.mLauncher.isWidgetsViewVisible() && !this.mLauncher.getWorkspace().isSwitchingState() && view instanceof WidgetCell) {
            handleClick();
        }
    }

    public void handleClick() {
        if (this.mWidgetInstructionToast != null) {
            this.mWidgetInstructionToast.cancel();
        }
        this.mWidgetInstructionToast = Toast.makeText(getContext(), Utilities.wrapForTts(getContext().getText(R.string.long_press_widget_to_add), getContext().getString(R.string.long_accessible_way_to_add)), Toast.LENGTH_SHORT);
        this.mWidgetInstructionToast.show();
    }

    @Override
    public boolean onLongClick(View view) {
        return mLauncher.isWidgetsViewVisible() && handleLongClick(view);
    }

    public boolean handleLongClick(View view) {
        return view.isInTouchMode() && !mLauncher.getWorkspace().isSwitchingState() && mLauncher.isDraggingEnabled() && beginDragging(view);
    }

    private boolean beginDragging(View view) {
        if (!(view instanceof WidgetCell)) {
            Log.e("WidgetsContainerView", "Unexpected dragging view: " + view);
        } else if (!beginDraggingWidget((WidgetCell) view)) {
            return false;
        }
        if (this.mLauncher.getDragController().isDragging()) {
            this.mLauncher.enterSpringLoadedDragMode();
        }
        return true;
    }

    private boolean beginDraggingWidget(WidgetCell widgetCell) {
        WidgetImageView widgetImageView = widgetCell.findViewById(R.id.widget_preview);
        if (widgetImageView.getBitmap() == null) {
            return false;
        }
        int[] iArr = new int[2];
        this.mLauncher.getDragLayer().getLocationInDragLayer(widgetImageView, iArr);
        new PendingItemDragHelper(widgetCell).startDrag(widgetImageView.getBitmapBounds(), widgetImageView.getBitmap().getWidth(), widgetImageView.getWidth(), new Point(iArr[0], iArr[1]), this, new DragOptions());
        return true;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
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
        return 0.0f;
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // Do nothing
    }

    @Override
    public void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        if (!(!z && z2 && (view == this.mLauncher.getWorkspace() || view instanceof DeleteDropTarget || view instanceof Folder))) {
            this.mLauncher.exitSpringLoadedDragModeDelayed(true, 500, null);
        }
        if (!z2) {
            dragObject.deferDragViewCleanupPostAnimation = false;
        }
    }

    public void setWidgets(MultiHashMap multiHashMap) {
        this.mAdapter.setWidgets(multiHashMap);
        this.mAdapter.notifyDataSetChanged();
        View findViewById = getContentView().findViewById(R.id.loader);
        if (findViewById != null) {
            ((ViewGroup) getContentView()).removeView(findViewById);
        }
    }

    public boolean isEmpty() {
        return this.mAdapter.getItemCount() == 0;
    }

    public List getWidgetsForPackageUser(PackageUserKey packageUserKey) {
        return this.mAdapter.copyWidgetsForPackageUser(packageUserKey);
    }
}