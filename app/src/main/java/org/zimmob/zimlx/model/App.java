package org.zimmob.zimlx.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import org.zimmob.zimlx.icon.SimpleIconProvider;
import org.zimmob.zimlx.manager.Setup;

/**
 * Created by saul on 05-01-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class App {
    private String label, packageName, className;
    private Drawable icon;
    public SimpleIconProvider _iconProvider;
    private int intIcon;

    public App(Context _context, ResolveInfo _info, PackageManager _pm) {
        this.label = _info.loadLabel(_pm).toString();
        this.icon = _info.loadIcon(_pm);
        this.packageName = _info.activityInfo.packageName;

        _iconProvider = Setup.imageLoader().createIconProvider(_info.loadIcon(_pm));
        this.className = _info.activityInfo.name;
        this.intIcon=_info.icon;
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
}
