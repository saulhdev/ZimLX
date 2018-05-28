package org.zimmob.zimlx.icon;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.IconLabelItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IconsHandler {
    private static final String TAG = "IconsHandler";
    private static String[] LAUNCHER_INTENTS = new String[] {
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.gau.go.launcherex.theme",
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

    public IconsHandler(Context context) {
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
    public void showDialog(final Activity activity) {
        loadAvailableIconPacks();
        final IconAdapter iconAdapter = new IconAdapter(mContext, mIconPacks);

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
