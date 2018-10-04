package org.zimmob.zimlx.graphics;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Process;
import android.os.UserHandle;

import org.zimmob.zimlx.AppInfo;
import org.zimmob.zimlx.IconCache;
import org.zimmob.zimlx.InvariantDeviceProfile;
import org.zimmob.zimlx.LauncherAppState;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.compat.LauncherActivityInfoCompat;
import org.zimmob.zimlx.model.PackageItemInfo;
import org.zimmob.zimlx.shortcuts.DeepShortcutManager;
import org.zimmob.zimlx.shortcuts.ShortcutInfoCompat;
import org.zimmob.zimlx.util.IconNormalizer;

import static android.graphics.Paint.DITHER_FLAG;
import static android.graphics.Paint.FILTER_BITMAP_FLAG;

public class LauncherIcons implements AutoCloseable {

    public static final Object sPoolSync = new Object();
    private static final Canvas sCanvas = new Canvas();
    private static final Rect sOldBounds = new Rect();
    private static final int DEFAULT_WRAPPER_BACKGROUND = Color.WHITE;
    private static LauncherIcons sPool;
    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
    }

    private final Rect mOldBounds = new Rect();
    private final Context mContext;
    private final Canvas mCanvas;
    private final PackageManager mPm;
    private final int mFillResIconDpi;
    private final int mIconBitmapSize;
    private IconNormalizer mNormalizer;
    private ShadowGenerator mShadowGenerator;
    private Drawable mWrapperIcon;
    private int mWrapperBackgroundColor = DEFAULT_WRAPPER_BACKGROUND;
    // sometimes we store linked lists of these things
    private LauncherIcons next;

    private LauncherIcons(Context context) {
        mContext = context.getApplicationContext();
        mPm = mContext.getPackageManager();

        InvariantDeviceProfile idp = LauncherAppState.getIDP(mContext);
        mFillResIconDpi = idp.fillResIconDpi;
        mIconBitmapSize = idp.iconBitmapSize;

        mCanvas = new Canvas();
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(DITHER_FLAG, FILTER_BITMAP_FLAG));
    }

    /**
     * Return a new Message instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     */
    public static LauncherIcons obtain(Context context) {
        synchronized (sPoolSync) {
            if (sPool != null) {
                LauncherIcons m = sPool;
                sPool = m.next;
                m.next = null;
                return m;
            }
        }
        return new LauncherIcons(context);
    }

    /**
     * Recycles a LauncherIcons that may be in-use.
     */
    public void recycle() {
        synchronized (sPoolSync) {
            // Clear any temporary state variables
            mWrapperBackgroundColor = DEFAULT_WRAPPER_BACKGROUND;

            next = sPool;
            sPool = this;
        }
    }

    @Override
    public void close() {
        recycle();
    }


    public static Bitmap createIconBitmap(ShortcutIconResource shortcutIconResource, Context context) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(shortcutIconResource.packageName);
            if (resourcesForApplication != null) {
                return createIconBitmap(resourcesForApplication.getDrawableForDensity(resourcesForApplication.getIdentifier(shortcutIconResource.resourceName, null, null), LauncherAppState.getInstance().getInvariantDeviceProfile().fillResIconDpi), context);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Bitmap createIconBitmap(Bitmap bitmap, Context context) {
        int i = LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize;
        if (i == bitmap.getWidth() && i == bitmap.getHeight()) {
            return bitmap;
        }
        return createIconBitmap(new BitmapDrawable(context.getResources(), bitmap), context);
    }

    public static Bitmap createBadgedIconBitmap(Drawable drawable, UserHandle userHandle, Context context, int i) {
        IconNormalizer instance = IconNormalizer.getInstance();
        float f = instance.getScale(drawable, null);
        Bitmap createIconBitmap = createIconBitmap(drawable, context, f);
        if (Utilities.isAdaptive(drawable)) {
            createIconBitmap = ShadowGenerator.getInstance().recreateIcon(createIconBitmap);
        }
        return badgeIconForUser(createIconBitmap, userHandle, context);
    }

    public static Bitmap badgeIconForUser(Bitmap bitmap, UserHandle userHandle, Context context) {
        if (userHandle == null || Process.myUserHandle().equals(userHandle)) {
            return bitmap;
        }
        Drawable userBadgedIcon = context.getPackageManager().getUserBadgedIcon(new FixedSizeBitmapDrawable(bitmap), userHandle);
        if (userBadgedIcon instanceof BitmapDrawable) {
            return ((BitmapDrawable) userBadgedIcon).getBitmap();
        }
        return createIconBitmap(userBadgedIcon, context);
    }

    public static Bitmap createScaledBitmapWithoutShadow(Drawable drawable, Context context, int i) {
        RectF rectF = new RectF();
        IconNormalizer instance = IconNormalizer.getInstance();
        float f = instance.getScale(drawable, rectF);
        return createIconBitmap(drawable, context, Math.min(f, ShadowGenerator.getScaleForBounds(rectF)));
    }

    public static Bitmap addShadowToIcon(Bitmap bitmap, Context context) {
        return ShadowGenerator.getInstance().recreateIcon(bitmap);
    }

    public static Bitmap badgeWithBitmap(Bitmap bitmap, Bitmap bitmap2, Context context) {
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.profile_badge_size);
        synchronized (sCanvas) {
            sCanvas.setBitmap(bitmap);
            sCanvas.drawBitmap(bitmap2, new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight()), new Rect(bitmap.getWidth() - dimensionPixelSize, bitmap.getHeight() - dimensionPixelSize, bitmap.getWidth(), bitmap.getHeight()), new Paint(2));
            sCanvas.setBitmap(null);
        }
        return bitmap;
    }

    public static Bitmap createIconBitmap(Drawable drawable, Context context) {
        float f = 1.0f;
        return createIconBitmap(drawable, context, f);
    }

    public static Bitmap createIconBitmap(Drawable drawable, Context context, float f) {
        Bitmap createBitmap;
        synchronized (sCanvas) {
            int i = LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize;
            if (drawable instanceof PaintDrawable) {
                PaintDrawable paintDrawable = (PaintDrawable) drawable;
                paintDrawable.setIntrinsicWidth(i);
                paintDrawable.setIntrinsicHeight(i);
            } else if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null && bitmap.getDensity() == 0) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                intrinsicWidth = i;
                intrinsicHeight = i;
            } else {
                float f2 = ((float) intrinsicWidth) / ((float) intrinsicHeight);
                if (intrinsicWidth > intrinsicHeight) {
                    intrinsicWidth = (int) (((float) i) / f2);
                    intrinsicHeight = i;
                } else if (intrinsicHeight > intrinsicWidth) {
                    intrinsicHeight = (int) (((float) i) * f2);
                    intrinsicWidth = i;
                } else {
                    intrinsicWidth = i;
                    intrinsicHeight = i;
                }
            }
            createBitmap = Bitmap.createBitmap(i, i, Config.ARGB_8888);
            Canvas canvas = sCanvas;
            canvas.setBitmap(createBitmap);
            int i2 = (i - intrinsicHeight) / 2;
            int i3 = (i - intrinsicWidth) / 2;
            sOldBounds.set(drawable.getBounds());
            if (Utilities.isAdaptive(drawable)) {
                i2 = Math.min(i2, i3);
                intrinsicWidth = Math.max(intrinsicHeight, intrinsicWidth);
                drawable.setBounds(i2, i2, i2 + intrinsicWidth, intrinsicWidth + i2);
            } else {
                drawable.setBounds(i2, i3, intrinsicHeight + i2, intrinsicWidth + i3);
            }
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.scale(f, f, (float) (i / 2), (float) (i / 2));
            drawable.draw(canvas);
            canvas.restore();
            drawable.setBounds(sOldBounds);
            canvas.setBitmap(null);
        }
        return createBitmap;
    }

    public Bitmap createShortcutIcon(ShortcutInfoCompat shortcutInfo) {
        return createShortcutIcon(shortcutInfo, true /* badged */);
    }

    public Bitmap createShortcutIcon(ShortcutInfoCompat shortcutInfo, boolean badged) {
        return createShortcutIcon(shortcutInfo, mContext, true);
    }

    public static Bitmap createShortcutIcon(ShortcutInfoCompat shortcutInfoCompat, Context context) {
        return createShortcutIcon(shortcutInfoCompat, context, true);
    }

    public static Bitmap createShortcutIcon(ShortcutInfoCompat shortcutInfoCompat, Context context, boolean z) {
        Bitmap defaultIcon;
        LauncherAppState instance = LauncherAppState.getInstance();
        Drawable shortcutIconDrawable = DeepShortcutManager.getInstance(context).getShortcutIconDrawable(shortcutInfoCompat, instance.getInvariantDeviceProfile().fillResIconDpi);
        IconCache iconCache = instance.getIconCache();
        if (shortcutIconDrawable == null) {
            defaultIcon = iconCache.getDefaultIcon(Process.myUserHandle());
        } else {
            defaultIcon = createScaledBitmapWithoutShadow(shortcutIconDrawable, context, 26);
        }
        if (!z) {
            return defaultIcon;
        }
        Bitmap addShadowToIcon = addShadowToIcon(defaultIcon, context);
        ComponentName activity = shortcutInfoCompat.getActivity();
        if (activity != null) {
            AppInfo appInfo = new AppInfo();
            appInfo.user = shortcutInfoCompat.getUserHandle();
            appInfo.componentName = activity;
            appInfo.intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER").setComponent(activity);
            LauncherActivityInfoCompat lac = LauncherActivityInfoCompat.create(context, appInfo.user, appInfo.intent);
            iconCache.getTitleAndIcon(appInfo, lac, false);
            defaultIcon = appInfo.iconBitmap;
        } else {
            PackageItemInfo packageItemInfo = new PackageItemInfo(shortcutInfoCompat.getPackage());
            iconCache.getTitleAndIconForApp(packageItemInfo, false);
            defaultIcon = packageItemInfo.iconBitmap;
        }
        return badgeWithBitmap(addShadowToIcon, defaultIcon, context);
    }

    static class FixedSizeBitmapDrawable extends BitmapDrawable {
        public FixedSizeBitmapDrawable(Bitmap bitmap) {
            super(null, bitmap);
        }

        @Override
        public int getIntrinsicHeight() {
            return getBitmap().getWidth();
        }

        @Override
        public int getIntrinsicWidth() {
            return getBitmap().getWidth();
        }
    }
}