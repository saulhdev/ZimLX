/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zimmob.zimlx.shortcuts;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.zimmob.zimlx.AbstractFloatingView;
import org.zimmob.zimlx.BubbleTextView;
import org.zimmob.zimlx.ItemInfo;
import org.zimmob.zimlx.Launcher;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.config.FeatureFlags;
import org.zimmob.zimlx.dragndrop.DragOptions;
import org.zimmob.zimlx.dragndrop.DragView;
import org.zimmob.zimlx.popup.PopupContainerWithArrow;
import org.zimmob.zimlx.popup.PopupItemView;
import org.zimmob.zimlx.popup.PopupPopulator;
import org.zimmob.zimlx.popup.SystemShortcut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link PopupItemView} that contains all of the {@link DeepShortcutView}s for an app,
 * as well as the system shortcuts such as Widgets and App Info.
 */
public class ShortcutsItemView extends PopupItemView implements View.OnLongClickListener,
        View.OnTouchListener {

    private final Point mIconShift = new Point();
    private final Point mIconLastTouchPos = new Point();
    private final List<DeepShortcutView> mDeepShortcutViews = new ArrayList<>();
    private final List<View> mSystemShortcutViews = new ArrayList<>();
    private Launcher mLauncher;
    private LinearLayout mShortcutsLayout;
    private LinearLayout mSystemShortcutIcons;
    private int mHiddenShortcutsHeight;
    public ShortcutsItemView(Context context) {
        this(context, null, 0);
    }

    public ShortcutsItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortcutsItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mLauncher = Launcher.getLauncher(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mShortcutsLayout = findViewById(R.id.deep_shortcuts);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        // Touched a shortcut, update where it was touched so we can drag from there on long click.
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mIconLastTouchPos.set((int) ev.getX(), (int) ev.getY());
                break;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        // Return early if this is not initiated from a touch or not the correct view
        if (!v.isInTouchMode() || !(v.getParent() instanceof DeepShortcutView)) return false;
        // Return early if global dragging is not enabled
        if (!mLauncher.isDraggingEnabled() || mLauncher.isEditingDisabled()) return false;
        // Return early if an item is already being dragged (e.g. when long-pressing two shortcuts)
        if (mLauncher.getDragController().isDragging()) return false;

        // Long clicked on a shortcut.
        DeepShortcutView sv = (DeepShortcutView) v.getParent();
        sv.setWillDrawIcon(false);

        // Move the icon to align with the center-top of the touch point
        mIconShift.x = mIconLastTouchPos.x - sv.getIconCenter().x;
        mIconShift.y = mIconLastTouchPos.y - mLauncher.getDeviceProfile().iconSizePx;

        DragView dv = mLauncher.getWorkspace().beginDragShared(sv.getIconView(),
                getContainer(), sv.getFinalInfo(),
                new ShortcutDragPreviewProvider(sv.getIconView(), mIconShift), new DragOptions());
        dv.animateShift(-mIconShift.x, -mIconShift.y);
        mLauncher.closeFolder(true);
        // TODO: support dragging from within folder without having to close it
        AbstractFloatingView.closeOpenContainer(mLauncher, AbstractFloatingView.TYPE_ALL);
        return false;
    }

    public void addShortcutView(View shortcutView, PopupPopulator.Item shortcutType) {
        addShortcutView(shortcutView, shortcutType, -1);
    }

    private void addShortcutView(View shortcutView, PopupPopulator.Item shortcutType, int index) {
        if (shortcutType == PopupPopulator.Item.SHORTCUT) {
            mDeepShortcutViews.add((DeepShortcutView) shortcutView);
        } else {
            mSystemShortcutViews.add(shortcutView);
        }
        if (shortcutType == PopupPopulator.Item.SYSTEM_SHORTCUT_ICON) {
            // System shortcut icons are added to a header that is separate from the full shortcuts.
            if (mSystemShortcutIcons == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(FeatureFlags.applyDarkTheme(mLauncher, FeatureFlags.DARK_SHORTCUTS));
                mSystemShortcutIcons = (LinearLayout) layoutInflater.inflate(
                        R.layout.system_shortcut_icons, mShortcutsLayout, false);
                mShortcutsLayout.addView(mSystemShortcutIcons, 0);
            }
            mSystemShortcutIcons.addView(shortcutView, index);
        } else {
            if (mShortcutsLayout.getChildCount() > 0) {
                View prevChild = mShortcutsLayout.getChildAt(mShortcutsLayout.getChildCount() - 1);
                if (prevChild instanceof DeepShortcutView) {
                    prevChild.findViewById(R.id.divider).setVisibility(VISIBLE);
                }
            }
            mShortcutsLayout.addView(shortcutView, index);
        }
    }

    public List<DeepShortcutView> getDeepShortcutViews(boolean reverseOrder) {
        if (reverseOrder) {
            Collections.reverse(mDeepShortcutViews);
        }
        return mDeepShortcutViews;
    }

    public List<View> getSystemShortcutViews(boolean reverseOrder) {
        // Always reverse system shortcut icons (in the header)
        // so they are in priority order from right to left.
        if (reverseOrder || mSystemShortcutIcons != null) {
            Collections.reverse(mSystemShortcutViews);
        }
        return mSystemShortcutViews;
    }

    /**
     * Hides shortcuts until only {@param maxShortcuts} are showing. Also sets
     * {@link #mHiddenShortcutsHeight} to be the amount of extra space that shortcuts will
     * require when {@link #showAllShortcuts(boolean)} is called.
     */
    public void hideShortcuts(boolean hideFromTop, int maxShortcuts) {
        // When shortcuts are shown, they get more space allocated to them.
        final int oldHeight = mShortcutsLayout.getChildAt(0).getLayoutParams().height;
        final int newHeight = getResources().getDimensionPixelSize(R.dimen.bg_popup_item_height);
        mHiddenShortcutsHeight = (newHeight - oldHeight) * mShortcutsLayout.getChildCount();

        int numToHide = mShortcutsLayout.getChildCount() - maxShortcuts;
        if (numToHide <= 0) {
            return;
        }
        final int numShortcuts = mShortcutsLayout.getChildCount();
        final int dir = hideFromTop ? 1 : -1;
        for (int i = hideFromTop ? 0 : numShortcuts - 1; 0 <= i && i < numShortcuts; i += dir) {
            View child = mShortcutsLayout.getChildAt(i);
            if (child instanceof DeepShortcutView) {
                mHiddenShortcutsHeight += child.getLayoutParams().height;
                child.setVisibility(GONE);
                int prev = i + dir;
                if (!hideFromTop && 0 <= prev && prev < numShortcuts) {
                    // When hiding views from the bottom, make sure to hide the last divider.
                    mShortcutsLayout.getChildAt(prev).findViewById(R.id.divider).setVisibility(GONE);
                }
                numToHide--;
                if (numToHide == 0) {
                    break;
                }
            }
        }
    }

    /**
     * Adds a {@link SystemShortcut.Widgets} item if there are widgets for the given ItemInfo.
     */
    public void enableWidgetsIfExist(final BubbleTextView originalIcon) {
        ItemInfo itemInfo = (ItemInfo) originalIcon.getTag();
        SystemShortcut widgetInfo = new SystemShortcut.Widgets();
        View.OnClickListener onClickListener = widgetInfo.getOnClickListener(mLauncher, itemInfo);
        View widgetsView = null;
        for (View systemShortcutView : mSystemShortcutViews) {
            if (systemShortcutView.getTag() instanceof SystemShortcut.Widgets) {
                widgetsView = systemShortcutView;
                break;
            }
        }
        final PopupPopulator.Item widgetsItem = mSystemShortcutIcons == null
                ? PopupPopulator.Item.SYSTEM_SHORTCUT
                : PopupPopulator.Item.SYSTEM_SHORTCUT_ICON;
        if (onClickListener != null && widgetsView == null) {
            // We didn't have any widgets cached but now there are some, so enable the shortcut.
            widgetsView = mLauncher.getLayoutInflater().inflate(widgetsItem.layoutId, this, false);
            PopupPopulator.initializeSystemShortcut(getContext(), widgetsView, widgetInfo);
            widgetsView.setOnClickListener(onClickListener);
            if (widgetsItem == PopupPopulator.Item.SYSTEM_SHORTCUT_ICON) {
                addShortcutView(widgetsView, widgetsItem, 0);
            } else {
                // If using the expanded system shortcut (as opposed to just the icon), we need to
                // reopen the container to ensure measurements etc. all work out. While this could
                // be quite janky, in practice the user would typically see a small flicker as the
                // animation restarts partway through, and this is a very rare edge case anyway.
                getContainer().close(false);
                PopupContainerWithArrow.showForIcon(originalIcon);
            }
        } else if (onClickListener == null && widgetsView != null) {
            // No widgets exist, but we previously added the shortcut so remove it.
            if (widgetsItem == PopupPopulator.Item.SYSTEM_SHORTCUT_ICON) {
                mSystemShortcutViews.remove(widgetsView);
                mSystemShortcutIcons.removeView(widgetsView);
            } else {
                ((PopupContainerWithArrow) getParent()).close(false);
                PopupContainerWithArrow.showForIcon(originalIcon);
            }
        }
    }

    @Override
    public int getArrowColor(boolean isArrowAttachedToBottom) {
        return Utilities.resolveAttributeData(getContext(),
                isArrowAttachedToBottom || mSystemShortcutIcons == null
                        ? R.attr.popupColorPrimary
                        : R.attr.appPopupHeaderBgColor);
    }
}
