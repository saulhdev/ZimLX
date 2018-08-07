package org.zimmob.zimlx.icon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.apps.AppManager;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.util.AppSettings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IconPackHandler {
    private static final String TAG = IconPackHandler.class.getSimpleName();
    private static String[] LAUNCHER_INTENTS = new String[]{
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.gau.go.launcherex.theme",
            "com.dlto.atom.launcher.THEME",
            "com.novalauncher.THEME",
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON"
    };
    private Map<String, IconPackInfo> mIconPacks = new HashMap<>();
    private PackageManager mPackageManager;
    private Map<String, String> mAppFilterDrawables = new HashMap<>();
    private List<Bitmap> mBackImages = new ArrayList<>();
    private List<String> mDrawables = new ArrayList<>();
    private Resources mCurrentIconPackRes;
    private Resources mOriginalIconPackRes;
    private String mIconPackPackageName;
    private Bitmap mFrontImage;
    private Bitmap mMaskImage;
    private Bitmap mTmpBitmap;
    private AlertDialog mAlertDialog;
    private Context mContext;
    private String mDefaultIconPack;

    private float mFactor = 1.0f;
    private boolean mDialogShowing;

    public IconPackHandler(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mDefaultIconPack = context.getString(R.string.icon_pack_default);
        String iconPack = AppSettings.get().getIconPack();
        loadAvailableIconPacks();
        loadIconPack(iconPack, false);
    }

    private void loadAvailableIconPacks() {
        List<ResolveInfo> launcherActivities = new ArrayList<>();
        mIconPacks.clear();

        for (String i : LAUNCHER_INTENTS) {
            launcherActivities.addAll(mPackageManager.queryIntentActivities(
                    new Intent(i), PackageManager.GET_META_DATA));
        }
        for (ResolveInfo ri : launcherActivities) {
            String packageName = ri.activityInfo.packageName;
            IconPackInfo info = new IconPackInfo(ri, mPackageManager);
            mIconPacks.put(packageName, info);
        }
    }

    public Map<String, IconPackInfo> getIconPacks() {
        return mIconPacks;
    }

    private File getIconsCacheDir() {
        return new File(mContext.getCacheDir().getPath() + "/icons/");
    }

    private void clearCache() {
        File cacheDir = getIconsCacheDir();
        if (!cacheDir.isDirectory()) {
            return;
        }

        for (File item : cacheDir.listFiles()) {
            if (!item.delete()) {
                Log.w(TAG, "Failed to delete file: " + item.getAbsolutePath());
            }
        }
    }

    public boolean isDefaultIconPack() {
        return mDefaultIconPack.equalsIgnoreCase(mIconPackPackageName) ||
                mIconPackPackageName.equals(mContext.getString(R.string.icon_pack_system));
    }

    private void loadIconPack(String packageName, boolean fallback) {
        mIconPackPackageName = packageName;
        if (!fallback) {
            mAppFilterDrawables.clear();
            mBackImages.clear();
            clearCache();
        } else {
            mDrawables.clear();
        }

        if (isDefaultIconPack()) {
            return;
        }

        XmlPullParser xpp = null;

        try {
            mOriginalIconPackRes = mPackageManager.getResourcesForApplication(mIconPackPackageName);
            mCurrentIconPackRes = mOriginalIconPackRes;
            int appfilterid = mOriginalIconPackRes.getIdentifier("appfilter", "xml",
                    mIconPackPackageName);
            if (appfilterid > 0) {
                xpp = mOriginalIconPackRes.getXml(appfilterid);
            }

            if (xpp != null) {
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (!fallback & xpp.getName().equals("iconback")) {
                            for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                if (xpp.getAttributeName(i).startsWith("img")) {
                                    String drawableName = xpp.getAttributeValue(i);
                                    Bitmap iconback = loadBitmap(drawableName);
                                    if (iconback != null) {
                                        mBackImages.add(iconback);
                                    }
                                }
                            }
                        } else if (!fallback && xpp.getName().equals("iconmask")) {
                            if (xpp.getAttributeCount() > 0 &&
                                    xpp.getAttributeName(0).equals("img1")) {
                                String drawableName = xpp.getAttributeValue(0);
                                mMaskImage = loadBitmap(drawableName);
                            }
                        } else if (!fallback && xpp.getName().equals("iconupon")) {
                            if (xpp.getAttributeCount() > 0 &&
                                    xpp.getAttributeName(0).equals("img1")) {
                                String drawableName = xpp.getAttributeValue(0);
                                mFrontImage = loadBitmap(drawableName);
                            }
                        } else if (!fallback && xpp.getName().equals("scale")) {
                            if (xpp.getAttributeCount() > 0 &&
                                    xpp.getAttributeName(0).equals("factor")) {
                                mFactor = Float.valueOf(xpp.getAttributeValue(0));
                            }
                        }
                        if (xpp.getName().equals("item")) {
                            String componentName = null;
                            String drawableName = null;

                            for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                if (xpp.getAttributeName(i).equals("component")) {
                                    componentName = xpp.getAttributeValue(i);
                                } else if (xpp.getAttributeName(i).equals("drawable")) {
                                    drawableName = xpp.getAttributeValue(i);
                                }
                            }
                            if (fallback && getIdentifier(packageName, drawableName,
                                    true) > 0 && !mDrawables.contains(drawableName)) {
                                mDrawables.add(drawableName);
                            }
                            if (!fallback && componentName != null && drawableName != null &&
                                    !mAppFilterDrawables.containsKey(componentName)) {
                                mAppFilterDrawables.put(componentName, drawableName);
                            }
                        }
                    }
                    eventType = xpp.next();
                }
            }
        } catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing appfilter.xml " + e);
        }
    }

    public void switchIconPacks(String packageName) {
        if (packageName.equals(mIconPackPackageName)) {
            packageName = mDefaultIconPack;
        }

        String localizedDefault = mContext.getString(R.string.icon_pack_system);
        if (packageName.equals(mDefaultIconPack) || packageName.equals(localizedDefault) ||
                mIconPacks.containsKey(packageName)) {
            new IconPackLoader(packageName).execute();
        }
    }

    public void hideDialog() {
        if (mDialogShowing && mAlertDialog != null) {
            mAlertDialog.dismiss();
            mDialogShowing = false;
        }
    }

    public void applyIconPack(AppManager appManager, final int iconSize, String iconPackName, List<App> apps) {
        Resources iconPackResources = null;
        int intResourceIcon;
        int intResourceBack = 0;
        int intResourceMask = 0;
        int intResourceUpon = 0;
        float scale = 1;

        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
        p.setAntiAlias(true);

        Paint origP = new Paint(Paint.FILTER_BITMAP_FLAG);
        origP.setAntiAlias(true);

        Paint maskP = new Paint(Paint.FILTER_BITMAP_FLAG);
        maskP.setAntiAlias(true);
        maskP.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        if (!iconPackName.equals("")) {
            try {
                iconPackResources = appManager.getPackageManager().getResourcesForApplication(iconPackName);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            if (iconPackResources != null) {
                if (getResource(iconPackResources, iconPackName, "iconback", null) != null)
                    intResourceBack = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconback", null), "drawable", iconPackName);
                if (getResource(iconPackResources, iconPackName, "iconmask", null) != null)
                    intResourceMask = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconmask", null), "drawable", iconPackName);
                if (getResource(iconPackResources, iconPackName, "iconupon", null) != null)
                    intResourceUpon = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconupon", null), "drawable", iconPackName);
                if (getResource(iconPackResources, iconPackName, "scale", null) != null)
                    scale = Float.parseFloat(getResource(iconPackResources, iconPackName, "scale", null));
            }
        }

        BitmapFactory.Options uniformOptions = new BitmapFactory.Options();
        uniformOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        uniformOptions.inScaled = false;
        uniformOptions.inDither = false;

        Bitmap back = null;
        Bitmap mask = null;
        Bitmap upon = null;
        Canvas canvasOrig;
        Canvas canvas;
        Bitmap scaledBitmap;
        Bitmap scaledOrig;
        Bitmap orig;

        if (iconPackName.compareTo("") != 0 && iconPackResources != null) {
            try {
                if (intResourceBack != 0)
                    back = BitmapFactory.decodeResource(iconPackResources, intResourceBack, uniformOptions);
                if (intResourceMask != 0)
                    mask = BitmapFactory.decodeResource(iconPackResources, intResourceMask, uniformOptions);
                if (intResourceUpon != 0)
                    upon = BitmapFactory.decodeResource(iconPackResources, intResourceUpon, uniformOptions);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        for (int I = 0; I < apps.size(); I++) {
            if (iconPackResources != null) {
                String iconResource = getResource(iconPackResources, iconPackName, null, apps.get(I).getComponentName());
                if (iconResource != null) {
                    intResourceIcon = iconPackResources.getIdentifier(iconResource, "drawable", iconPackName);
                } else {
                    intResourceIcon = 0;
                }

                if (intResourceIcon != 0) {
                    // has single drawable for app
                    apps.get(I).setIcon(new BitmapDrawable(BitmapFactory.decodeResource(iconPackResources, intResourceIcon, uniformOptions)));
                } else {
                    try {
                        orig = Bitmap.createBitmap(apps.get(I).getIcon().getIntrinsicWidth(), apps.get(I).getIcon().getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    } catch (Exception e) {
                        continue;
                    }
                    apps.get(I).getIcon().setBounds(0, 0, apps.get(I).getIcon().getIntrinsicWidth(), apps.get(I).getIcon().getIntrinsicHeight());
                    apps.get(I).getIcon().draw(new Canvas(orig));

                    scaledOrig = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    scaledBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(scaledBitmap);

                    if (back != null)
                        canvas.drawBitmap(back, getResizedMatrix(back, iconSize, iconSize), p);

                    canvasOrig = new Canvas(scaledOrig);
                    orig = getResizedBitmap(orig, (int) (iconSize * scale), (int) (iconSize * scale));
                    canvasOrig.drawBitmap(orig, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, origP);

                    if (mask != null)
                        canvasOrig.drawBitmap(mask, getResizedMatrix(mask, iconSize, iconSize), maskP);

                    canvas.drawBitmap(getResizedBitmap(scaledOrig, iconSize, iconSize), 0, 0, p);

                    if (upon != null)
                        canvas.drawBitmap(upon, getResizedMatrix(upon, iconSize, iconSize), p);

                    apps.get(I).setIcon(new BitmapDrawable(appManager.getContext().getResources(), scaledBitmap));
                }
            }
        }
    }

    public String getResource(Resources resources, String packageName, String resourceName, String componentName) {
        XmlResourceParser xrp;
        String resource = null;
        try {
            int resourceValue = resources.getIdentifier("appfilter", "xml", packageName);
            if (resourceValue != 0) {
                xrp = resources.getXml(resourceValue);
                while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                    if (xrp.getEventType() == 2) {
                        try {
                            String string = xrp.getName();
                            if (componentName != null) {
                                if (xrp.getAttributeValue(0).compareTo(componentName) == 0) {
                                    resource = xrp.getAttributeValue(1);
                                }
                            } else if (string.equals(resourceName)) {
                                resource = xrp.getAttributeValue(0);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    xrp.next();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return resource;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    public Matrix getResizedMatrix(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return matrix;
    }

    public void showDialog(final Activity activity) {
        loadAvailableIconPacks();
        final IconPackAdapter iconAdapter = new IconPackAdapter(mContext, mIconPacks);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.icon_pack_title)
                .setAdapter(iconAdapter, (dialog, position) -> {
                    String selected = iconAdapter.getItem(position);
                    String current = AppSettings.get().getIconPack();
                    if (!selected.equals(current)) {
                        switchIconPacks(selected);
                    }
                });
        mAlertDialog = builder.create();
        mAlertDialog.show();
        mDialogShowing = true;
    }

    private Bitmap loadBitmap(String drawableName) {
        Drawable bitmap = loadDrawable(null, drawableName, true);
        if (bitmap != null && bitmap instanceof BitmapDrawable) {
            return ((BitmapDrawable) bitmap).getBitmap();
        }
        return null;
    }

    public Drawable loadDrawable(String packageName, String drawableName, boolean currentIconPack) {
        if (packageName == null) {
            packageName = mIconPackPackageName;
        }
        int id = getIdentifier(packageName, drawableName, currentIconPack);

        return id > 0 ?
                (!currentIconPack ? mOriginalIconPackRes : mCurrentIconPackRes).getDrawable(id) :
                null;
    }

    public int getIdentifier(String packageName, String drawableName, boolean currentIconPack) {
        if (drawableName == null) {
            return 0;
        }
        if (packageName == null) {
            packageName = mIconPackPackageName;
        }
        return (!currentIconPack ? mOriginalIconPackRes : mCurrentIconPackRes).getIdentifier(
                drawableName, "drawable", packageName);
    }

    private class IconPackLoader extends AsyncTask<Void, Void, Void> {
        private String mIconPackPackageName;

        private IconPackLoader(String packageName) {
            mIconPackPackageName = packageName;
            //mIconCache = LauncherAppState.getInstance(mContext).getIconCache();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            loadIconPack(mIconPackPackageName, false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AppSettings.get().setIconPack(mIconPackPackageName);
        }
    }

}
