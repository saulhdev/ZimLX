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

package org.zimmob.zimlx.badge;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.SparseArray;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.graphics.IconPalette;
import org.zimmob.zimlx.graphics.ShadowGenerator;

/**
 * Contains parameters necessary to draw a badge for an icon (e.g. the size of the badge).
 *
 * @see BadgeInfo for the data to draw
 */
public class BadgeRenderer {

    private static final boolean DOTS_ONLY = false;

    // The badge sizes are defined as percentages of the app icon size.
    private static final float SIZE_PERCENTAGE = 0.38f;
    // Used to expand the width of the badge for each additional digit.
    private static final float CHAR_SIZE_PERCENTAGE = 0.12f;
    private static final float TEXT_SIZE_PERCENTAGE = 0.26f;
    private static final float OFFSET_PERCENTAGE = 0.02f;
    private static final float STACK_OFFSET_PERCENTAGE_X = 0.05f;
    private static final float STACK_OFFSET_PERCENTAGE_Y = 0.06f;
    private static final float DOT_SCALE = 0.6f;

    private final Context mContext;
    private final IconPalette mIconPalette;
    private final int mSize;
    private final int mCharSize;
    private final int mTextHeight;
    private final int mOffset;
    private final int mStackOffsetX;
    private final int mStackOffsetY;
    private final IconDrawer mLargeIconDrawer;
    private final IconDrawer mSmallIconDrawer;
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG
            | Paint.FILTER_BITMAP_FLAG);
    private final SparseArray<Bitmap> mBackgroundsWithShadow;
    private final Bitmap mBackgroundWithShadow;

    public BadgeRenderer(Context context, int iconSizePx) {
        mContext = context;
        Resources res = context.getResources();
        mSize = (int) (SIZE_PERCENTAGE * iconSizePx);
        mCharSize = (int) (CHAR_SIZE_PERCENTAGE * iconSizePx);
        mOffset = (int) (OFFSET_PERCENTAGE * iconSizePx);
        mStackOffsetX = (int) (STACK_OFFSET_PERCENTAGE_X * iconSizePx);
        mStackOffsetY = (int) (STACK_OFFSET_PERCENTAGE_Y * iconSizePx);
        mTextPaint.setTextSize(iconSizePx * TEXT_SIZE_PERCENTAGE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mLargeIconDrawer = new IconDrawer(res.getDimensionPixelSize(R.dimen.badge_small_padding));
        mSmallIconDrawer = new IconDrawer(res.getDimensionPixelSize(R.dimen.badge_large_padding));
        mIconPalette = IconPalette.fromDominantColor(Utilities.getDynamicBadgeColor(context));

        // Measure the text height.
        Rect tempTextHeight = new Rect();
        mTextPaint.getTextBounds("0", 0, 1, tempTextHeight);
        mTextHeight = tempTextHeight.height();
        mBackgroundsWithShadow = new SparseArray<>(3);
        mBackgroundWithShadow = ShadowGenerator.createPillWithShadow(-1, mSize, mSize);
    }

    public void draw(Canvas canvas, BadgeInfo badgeInfo, Rect rect, float f, Point point) {
        draw(canvas, badgeInfo, rect, f, point, mIconPalette);
    }

    public void draw(Canvas canvas, BadgeInfo badgeInfo, Rect iconBounds, float badgeScale, Point spaceForOffset, IconPalette iconPalette) {
        mTextPaint.setColor(iconPalette.textColor);
        IconDrawer iconDrawer = (badgeInfo == null || !badgeInfo.isIconLarge()) ? mSmallIconDrawer : mLargeIconDrawer;
        Shader icon = badgeInfo == null ? null : badgeInfo.getNotificationIconForBadge(
                mContext, iconPalette.backgroundColor, mSize, iconDrawer.mPadding);
        String notificationCount = badgeInfo == null ? "0"
                : String.valueOf(badgeInfo.getNotificationCount());
        int numChars = notificationCount.length();
        int width = DOTS_ONLY ? mSize : mSize + mCharSize * (numChars - 1);
        Bitmap backgroundWithShadow = mBackgroundsWithShadow.get(numChars);
        if (backgroundWithShadow == null) {
            backgroundWithShadow = new ShadowGenerator.Builder(Color.WHITE)
                    .setupBlurForSize(mSize).createPill(width, mSize);
            mBackgroundsWithShadow.put(numChars, backgroundWithShadow);
        }

        if (badgeInfo != null) {
            badgeInfo.getNotificationIconForBadge(mContext, iconPalette.backgroundColor, mSize, iconDrawer.mPadding);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        badgeScale *= 0.6f;

        // We draw the badge relative to its center.
        int badgeCenterX = iconBounds.right - width / 2;
        int badgeCenterY = iconBounds.top + mSize / 2;
        boolean isText = !DOTS_ONLY && badgeInfo != null && badgeInfo.getNotificationCount() != 0;
        boolean isIcon = !DOTS_ONLY && icon != null;
        boolean isDot = !(isText || isIcon);
        if (isDot) {
            badgeScale *= DOT_SCALE;
        }
        int offsetX = Math.min(mOffset, spaceForOffset.x);
        int offsetY = Math.min(mOffset, spaceForOffset.y);
        canvas.translate(badgeCenterX + offsetX, badgeCenterY - offsetY);
        //canvas.translate((float) ((rect.right - (mSize / 2)) + Math.min(mOffset, point.x)), (float) ((rect.top + (mSize / 2)) - Math.min(mOffset, point.y)));
        canvas.scale(badgeScale, badgeScale);
        mBackgroundPaint.setColorFilter(iconPalette.backgroundColorMatrixFilter);
        int length = mBackgroundWithShadow.getHeight();
        mBackgroundPaint.setColorFilter(iconPalette.saturatedBackgroundColorMatrixFilter);
        canvas.drawBitmap(mBackgroundWithShadow, (float) ((-length) / 2), (float) ((-length) / 2), mBackgroundPaint);
        int backgroundWithShadowSize = backgroundWithShadow.getHeight(); // Same as width.
        boolean shouldStack = !isDot && badgeInfo != null
                && badgeInfo.getNotificationKeys().size() > 1;
        if (shouldStack) {
            int offsetDiffX = mStackOffsetX - mOffset;
            int offsetDiffY = mStackOffsetY - mOffset;
            canvas.translate(offsetDiffX, offsetDiffY);
            canvas.drawBitmap(backgroundWithShadow, -backgroundWithShadowSize / 2,
                    -backgroundWithShadowSize / 2, mBackgroundPaint);
            canvas.translate(-offsetDiffX, -offsetDiffY);
        }
        if (isText) {
            canvas.drawBitmap(backgroundWithShadow, -backgroundWithShadowSize / 2,
                    -backgroundWithShadowSize / 2, mBackgroundPaint);
            canvas.drawText(notificationCount, 0, mTextHeight / 2, mTextPaint);
        } else if (isIcon) {
            canvas.drawBitmap(backgroundWithShadow, -backgroundWithShadowSize / 2,
                    -backgroundWithShadowSize / 2, mBackgroundPaint);
            iconDrawer.drawIcon(icon, canvas);
        } else if (isDot) {
            mBackgroundPaint.setColorFilter(iconPalette.saturatedBackgroundColorMatrixFilter);
            canvas.drawBitmap(backgroundWithShadow, -backgroundWithShadowSize / 2,
                    -backgroundWithShadowSize / 2, mBackgroundPaint);
        }
        canvas.restore();
    }

    /**
     * Draws the notification icon with padding of a given size.
     */
    private class IconDrawer {

        private final int mPadding;
        private final Bitmap mCircleClipBitmap;
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
                Paint.FILTER_BITMAP_FLAG);

        public IconDrawer(int padding) {
            mPadding = padding;
            mCircleClipBitmap = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ALPHA_8);
            Canvas canvas = new Canvas();
            canvas.setBitmap(mCircleClipBitmap);
            canvas.drawCircle(mSize / 2, mSize / 2, mSize / 2 - padding, mPaint);
        }

        public void drawIcon(Shader icon, Canvas canvas) {
            mPaint.setShader(icon);
            canvas.drawBitmap(mCircleClipBitmap, -mSize / 2, -mSize / 2, mPaint);
            mPaint.setShader(null);
        }
    }
}
