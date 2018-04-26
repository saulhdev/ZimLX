package org.zimmob.zimlx.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;

import org.zimmob.zimlx.manager.Setup;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class App {
    public String _label, _packageName, _className;
    public BaseIconProvider _iconProvider;
    public ResolveInfo _info;

    public App(Context context, ResolveInfo info, PackageManager pm) {
        _info = info;

        _iconProvider = Setup.imageLoader().createIconProvider(info.loadIcon(pm));
        _label = info.loadLabel(pm).toString();
        _packageName = info.activityInfo.packageName;
        _className = info.activityInfo.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof App) {
            App temp = (App) o;
            return _packageName.equals(temp._packageName);
        } else {
            return false;
        }
    }

    public String getLabel() {
        return _label;
    }

    public String getPackageName() {
        return _packageName;
    }


    public String getClassName() {
        return _className;
    }


    public BaseIconProvider getIconProvider() {
        return _iconProvider;
    }

    public void setIconProvider(@NonNull BaseIconProvider baseIconProvider) {

    }
}