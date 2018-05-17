package org.zimmob.zimlx.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.model.PopupIconLabelItem;
import org.zimmob.zimlx.util.DragAction;
import org.zimmob.zimlx.util.DragAction.Action;
import org.zimmob.zimlx.util.DragHandler;
import org.zimmob.zimlx.util.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class DragNDropLayout extends FrameLayout {

    private final float DRAG_THRESHOLD;
    private DragAction.Action _dragAction;
    private boolean _dragExceedThreshold;
    private Item _dragItem;
    private PointF _dragLocation;
    private PointF _dragLocationConverted;
    private PointF _dragLocationStart;
    private View _dragView;
    private boolean _dragging;
    private float _folderPreviewScale;
    private float _overlayIconScale;
    private final RecyclerView _overlayPopup;
    private final FastItemAdapter<PopupIconLabelItem> _overlayPopupAdapter;
    private boolean _overlayPopupShowing;
    private final OverlayView _overlayView;
    private final Paint _paint;
    private PointF _previewLocation;
    private final HashMap<DropTargetListener, DragFlag> _registeredDropTargetEntries;
    private boolean _showFolderPreview;
    private final SlideInLeftAnimator _slideInLeftAnimator;
    private final SlideInRightAnimator _slideInRightAnimator;
    private final int[] _tempArrayOfInt2;

    public DragNDropLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.DRAG_THRESHOLD = 20.0f;
        _paint = new Paint(1);
        _registeredDropTargetEntries = new HashMap();
        _tempArrayOfInt2 = new int[2];
        _dragLocation = new PointF();
        _dragLocationStart = new PointF();
        _dragLocationConverted = new PointF();
        _overlayIconScale = 1.0f;
        _overlayPopupAdapter = new FastItemAdapter();
        _previewLocation = new PointF();
        _slideInLeftAnimator = new SlideInLeftAnimator(new AccelerateDecelerateInterpolator());
        _slideInRightAnimator = new SlideInRightAnimator(new AccelerateDecelerateInterpolator());
        _paint.setFilterBitmap(true);
        _paint.setColor(-1);
        _overlayView = new OverlayView();
        _overlayPopup = new RecyclerView(context);
        _overlayPopup.setVisibility(View.INVISIBLE);
        _overlayPopup.setAlpha(0.0f);
        _overlayPopup.setOverScrollMode(2);
        _overlayPopup.setLayoutManager(new LinearLayoutManager(context, 1, false));
        _overlayPopup.setItemAnimator(_slideInLeftAnimator);
        _overlayPopup.setAdapter(_overlayPopupAdapter);
        addView(_overlayView, new LayoutParams(-1, -1));
        addView(_overlayPopup, new LayoutParams(-2, -2));
        setWillNotDraw(false);
    }

    public final boolean getDragging() {
        return _dragging;
    }

    @NonNull
    public final PointF getDragLocation() {
        return _dragLocation;
    }

    @Nullable
    public final Action getDragAction() {
        return _dragAction;
    }

    public final boolean getDragExceedThreshold() {
        return _dragExceedThreshold;
    }

    @Nullable
    public final Item getDragItem() {
        return _dragItem;
    }

    public final void showFolderPreviewAt(@NonNull View fromView, float x, float y) {
        if (!_showFolderPreview) {
            _showFolderPreview = true;
            convertPoint(fromView, this, x, y);
            _folderPreviewScale = 0.0f;
            invalidate();
        }
    }

    public final void cancelFolderPreview() {
        _showFolderPreview = false;
        _previewLocation.set(-1.0f, -1.0f);
        invalidate();
    }

    @Override
    protected void onDraw(@Nullable Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null || DragHandler._cachedDragBitmap == null || _dragLocation.equals(-1f, -1f))
            return;
        float x = _dragLocation.x - HomeActivity._itemTouchX;
        float y = _dragLocation.y - HomeActivity._itemTouchY;
        if (_dragging) {
            canvas.save();
            _overlayIconScale = Tool.clampFloat(_overlayIconScale + 0.05f, 1f, 1.1f);
            canvas.scale(_overlayIconScale, _overlayIconScale, x + DragHandler._cachedDragBitmap
                    .getWidth() / 2, y + DragHandler._cachedDragBitmap.getHeight() / 2);
            canvas.drawBitmap(DragHandler._cachedDragBitmap, x, y, _paint);
            canvas.restore();
        }
        if (_dragging)
            invalidate();
    }

    @SuppressLint({"ResourceType"})
    public void onViewAdded(@Nullable View child) {
        super.onViewAdded(child);
        _overlayView.bringToFront();
        _overlayPopup.bringToFront();
    }

    public final void showPopupMenuForItem(float x, float y, @NonNull List<PopupIconLabelItem> popupItem, com.mikepenz.fastadapter.listeners.OnClickListener<PopupIconLabelItem> listener) {
        if (!_overlayPopupShowing) {
            _overlayPopupShowing = true;
            _overlayPopup.setVisibility(View.VISIBLE);
            _overlayPopup.setTranslationX(x);
            _overlayPopup.setTranslationY(y);
            _overlayPopup.setAlpha(1.0f);
            _overlayPopupAdapter.add((List) popupItem);
            _overlayPopupAdapter.withOnClickListener(listener);
        }
    }

    public final void setPopupMenuShowDirection(boolean left) {
        if (left) {
            _overlayPopup.setItemAnimator(_slideInLeftAnimator);
        } else {
            _overlayPopup.setItemAnimator(_slideInRightAnimator);
        }
    }

    public final void hidePopupMenu() {
        if (_overlayPopupShowing) {
            _overlayPopupShowing = false;
            _overlayPopup.animate().alpha(0.0f).withEndAction(() -> {
                _overlayPopup.setVisibility(View.INVISIBLE);
                _overlayPopupAdapter.clear();
            });
            if (!_dragging) {
                _dragView = null;
                _dragItem = null;
                _dragAction = null;
            }
        }
    }

    public final void startDragNDropOverlay(View view, Item item, DragAction.Action action) {
        _dragging = true;
        _dragExceedThreshold = false;
        _overlayIconScale = 0.0f;
        _dragView = view;
        _dragItem = item;
        _dragAction = action;
        _dragLocationStart.set(_dragLocation);
        for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
            convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
            DragFlag dragFlag = (DragFlag) dropTarget.getValue();
            DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
            dragFlag.setShouldIgnore(!dropTargetListener.onStart(_dragAction, _dragLocation,
                    isViewContains(((DropTargetListener) dropTarget.getKey()).getView(), (int) _dragLocation.x, (int) _dragLocation.y)));

        }
        _overlayView.invalidate();
    }

    protected void onDetachedFromWindow() {
        cancelAllDragNDrop();
        super.onDetachedFromWindow();
    }

    public final void cancelAllDragNDrop() {
        _dragging = false;
        if (!_overlayPopupShowing) {
            _dragView = null;
            _dragItem = null;
            _dragAction = null;
        }
        for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget.getKey()).onEnd();
        }
    }

    public final void registerDropTarget(@NonNull DropTargetListener targetListener) {
        _registeredDropTargetEntries.put(targetListener, new DragFlag());
    }

    public void unregisterDropTarget(DropTargetListener targetListener) {
        _registeredDropTargetEntries.remove(targetListener);
    }

    public boolean onInterceptTouchEvent(@Nullable MotionEvent event) {
        if (event != null && event.getActionMasked() == 1 && _dragging) {
            handleDragFinished();
        }
        if (event != null) {
            _dragLocation.set(event.getX(), event.getY());
        }
        if (_dragging) {
            return true;
        }

        return super.onInterceptTouchEvent(event);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        if (event != null) {
            if (_dragging) {
                _dragLocation.set(event.getX(), event.getY());
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        handleMovement();
                        break;
                    case MotionEvent.ACTION_UP:
                        handleDragFinished();
                        break;
                    default:
                        break;
                }
                return _dragging || super.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    private final void handleMovement() {
        if (!_dragExceedThreshold && (Math.abs(_dragLocationStart.x - _dragLocation.x) > this.DRAG_THRESHOLD || Math.abs(_dragLocationStart.y - _dragLocation.y) > this.DRAG_THRESHOLD)) {
            _dragExceedThreshold = true;

            for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
                if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();

                    dropTargetListener.onStartDrag(_dragAction, _dragLocationConverted);
                }
            }
        }
        if (_dragExceedThreshold) {
            hidePopupMenu();
        }
        for (Entry<DropTargetListener, DragFlag> dropTarget2 : _registeredDropTargetEntries.entrySet()) {
            DropTargetListener dropTargetListener = dropTarget2.getKey();
            if (!dropTarget2.getValue().getShouldIgnore()) {
                convertPoint(dropTarget2.getKey().getView());
                if (isViewContains(dropTarget2.getKey().getView(),
                        (int) _dragLocation.x,
                        (int) _dragLocation.y)) {

                    dropTargetListener.onMove(_dragAction, _dragLocationConverted);
                    if (dropTarget2.getValue().getPreviousOutside()) {
                        dropTarget2.getValue().setPreviousOutside(false);
                        dropTargetListener = dropTarget2.getKey();
                        dropTargetListener.onEnter(_dragAction, _dragLocationConverted);
                    }
                } else if (!dropTarget2.getValue().getPreviousOutside()) {
                    dropTarget2.getValue().setPreviousOutside(true);
                    dropTargetListener = dropTarget2.getKey();
                    dropTargetListener.onExit(_dragAction, _dragLocationConverted);
                }
            }
        }
    }

    private void handleDragFinished() {
        _dragging = false;
        for (Entry dropTarget : _registeredDropTargetEntries.entrySet()) {
            if (!((DragFlag) dropTarget.getValue()).getShouldIgnore()) {
                if (isViewContains(
                        ((DropTargetListener) dropTarget.getKey()).getView(),
                        (int) _dragLocation.x,
                        (int) _dragLocation.y)) {
                    convertPoint(((DropTargetListener) dropTarget.getKey()).getView());
                    DropTargetListener dropTargetListener = (DropTargetListener) dropTarget.getKey();
                    dropTargetListener.onDrop(_dragAction, _dragLocationConverted, _dragItem);
                }
            }
        }
        for (Entry dropTarget2 : _registeredDropTargetEntries.entrySet()) {
            ((DropTargetListener) dropTarget2.getKey()).onEnd();
        }
        cancelFolderPreview();
    }

    public final void convertPoint(@NonNull View toView) {

        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        _dragLocationConverted.set(
                (fromCoordinate[0] - toCoordinate[0]) + _dragLocation.x,
                (fromCoordinate[1] - toCoordinate[1]) + _dragLocation.y);
    }

    public final void convertPoint(@NonNull View fromView, @NonNull View toView, float x, float y) {
        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        fromView.getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);
        _previewLocation.set(
                ((float) (fromCoordinate[0] - toCoordinate[0])) + x,
                ((float) (fromCoordinate[1] - toCoordinate[1])) + y);
    }

    private final boolean isViewContains(View view, int rx, int ry) {
        view.getLocationOnScreen(_tempArrayOfInt2);
        int x = _tempArrayOfInt2[0];
        int y = _tempArrayOfInt2[1];
        int w = view.getWidth();
        int h = view.getHeight();
        return rx >= x && rx <= x + w && ry >= y && ry <= y + h;
    }

    public static final class DragFlag {
        private boolean _previousOutside = true;
        private boolean _shouldIgnore = false;

        public final boolean getPreviousOutside() {
            return _previousOutside;
        }

        public final void setPreviousOutside(boolean v) {
            _previousOutside = v;
        }

        public final boolean getShouldIgnore() {
            return _shouldIgnore;
        }

        public final void setShouldIgnore(boolean v) {
            _shouldIgnore = v;
        }
    }

    public static class DropTargetListener {

        private final View view;

        public DropTargetListener(View view) {
            this.view = view;
        }

        public final View getView() {
            return this.view;
        }

        public boolean onStart(Action action, PointF location, boolean isInside) {
            return false;
        }

        public void onStartDrag(Action action, PointF location) {
        }

        public void onDrop(Action action, PointF location, Item item) {
        }

        public void onMove(Action action, PointF location) {
        }

        public void onEnter(Action action, PointF location) {
        }

        public void onExit(Action action, PointF location) {
        }

        public void onEnd() {
        }
    }

    public final class OverlayView extends View {

        public OverlayView() {
            super(DragNDropLayout.this.getContext());
            setWillNotDraw(false);
        }

        public boolean onTouchEvent(@Nullable MotionEvent event) {
            if (event == null || event.getActionMasked() != 0 || DragNDropLayout.this.getDragging() || !DragNDropLayout.this._overlayPopupShowing) {
                return super.onTouchEvent(event);
            }
            DragNDropLayout.this.hidePopupMenu();
            return true;
        }

        protected void onDraw(@Nullable Canvas canvas) {
            super.onDraw(canvas);
            if (canvas == null || DragHandler._cachedDragBitmap == null || _dragLocation.equals(-1f, -1f))
                return;

            float x = _dragLocation.x - HomeActivity._itemTouchX;
            float y = _dragLocation.y - HomeActivity._itemTouchY;
            if (_dragging) {
                canvas.save();
                _overlayIconScale = Tool.clampFloat(_overlayIconScale + 0.05f, 1f, 1.1f);
                canvas.scale(_overlayIconScale,
                        _overlayIconScale, x + DragHandler._cachedDragBitmap.getWidth() / 2,
                        y + DragHandler._cachedDragBitmap.getHeight() / 2);

                canvas.drawBitmap(DragHandler._cachedDragBitmap, x, y, _paint);
                canvas.restore();
            }

            if (_dragging)
                invalidate();
        }
    }

}