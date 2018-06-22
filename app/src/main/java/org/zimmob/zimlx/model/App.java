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
    private String label, packageName, className;
    private Drawable icon;
    public SimpleIconProvider _iconProvider;
    private int intIcon;
    @Nullable public String _universalLabel;

    public App(ResolveInfo info, PackageManager pm) {
        this.label = info.loadLabel(pm).toString();
        this.icon = info.loadIcon(pm);
        this.packageName = info.activityInfo.packageName;

        _iconProvider = Setup.imageLoader().createIconProvider(info.loadIcon(pm));
        this.className = info.activityInfo.name;
        this.intIcon=info.icon;

        try {
            updateUniversalLabel(pm, info);
            Log.d("AppModel", "Universal label " + getUniversalLabel());
        } catch (Exception e) {
            Log.e("AppModel", "Cannot resolve universal label for " + label, e);
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

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public SimpleIconProvider getIconProvider() {
        return _iconProvider;
    }

    public void setIconProvider(@NonNull SimpleIconProvider baseIconProvider) {

    }
    public String getComponentName() {
        return "ComponentInfo{" + packageName + "/" + className + "}";
    }

    /**
     +     * App label for root locale.
     +     * @see Locale#ROOT
     +     */
    @Nullable
    public String getUniversalLabel() {
                return _universalLabel;
           }

           public void setUniversalLabel(@Nullable String universalLabel) {
                _universalLabel = universalLabel;
           }
}
