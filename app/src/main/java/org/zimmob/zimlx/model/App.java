package org.zimmob.zimlx.model;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.zimmob.zimlx.icon.SimpleIconProvider;
import org.zimmob.zimlx.manager.Setup;

import java.util.Comparator;
import java.util.Locale;

/**
 * Created by saul on 05-01-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class App implements Comparator<App> {
    private static final String TAG = App.class.getSimpleName();
    private String label;
    private String packageName;
    private String className;
    @Nullable
    private String universalLabel;
    private Drawable icon;
    private SimpleIconProvider iconProvider;
    private int iconRes;

    public App(ResolveInfo info, PackageManager pm) {
        this.label = info.loadLabel(pm).toString();
        this.icon = info.loadIcon(pm);
        this.packageName = info.activityInfo.packageName;
        this.iconProvider = Setup.imageLoader().createIconProvider(info.loadIcon(pm));
        this.className = info.activityInfo.name;
        this.iconRes = info.getIconResource();
        try {
            updateUniversalLabel(pm, info);
            Log.d(TAG, "Universal label " + getUniversalLabel());
        } catch (Exception e) {
            Log.e(TAG, "Cannot resolve universal label for " + label, e);
        }
    }

    private void updateUniversalLabel(PackageManager pm, ResolveInfo info) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = info.activityInfo.applicationInfo;
        Configuration config = new Configuration();
        config.locale = Locale.ROOT;

        Resources resources = pm.getResourcesForApplication(appInfo);
        resources.updateConfiguration(config, null);

        setUniversalLabel(resources.getString(appInfo.labelRes));
    }

    @Override
    public int compare(App o1, App o2) {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof App) {
            App temp = (App) obj;
            return this.packageName.equals(temp.packageName);
        } else {
            return false;
        }
    }

    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getIconRes() {
        return this.iconRes;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public SimpleIconProvider getIconProvider() {
        return iconProvider;
    }

    public void setIconProvider(@NonNull SimpleIconProvider baseIconProvider) {
        this.iconProvider = baseIconProvider;
    }

    public String getComponentName() {
        return "ComponentInfo{" + packageName + "/" + className + "}";
    }

    /**
     * +     * App label for root locale.
     * +     * @see Locale#ROOT
     * +
     */
    @Nullable
    public String getUniversalLabel() {
        return universalLabel;
    }

    public void setUniversalLabel(@Nullable String universalLabel) {
        this.universalLabel = universalLabel;
    }
}
