package org.zimmob.zimlx.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.SimpleIconProvider;

/**
 * Created by saul on 05-01-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class App {
    private String label, packageName, className;
    private SimpleIconProvider iconProvider;
    public ResolveInfo info;

    public App(Context _context, ResolveInfo _info, PackageManager _pm) {
        this.info = _info;
        this.label = _info.loadLabel(_pm).toString();
        this.iconProvider = Setup.imageLoader().createIconProvider(_info.loadIcon(_pm));
        this.packageName = _info.activityInfo.packageName;
        this.className = _info.activityInfo.name;
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

    public SimpleIconProvider getIconProvider() {
        return iconProvider;
    }

    public void setIconProvider(SimpleIconProvider iconProvider) {
        this.iconProvider = iconProvider;
    }
}
