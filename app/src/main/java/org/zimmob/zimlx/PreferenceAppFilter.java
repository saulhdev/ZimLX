package org.zimmob.zimlx;

import android.content.ComponentName;
import android.content.Context;

import org.zimmob.zimlx.preferences.IPreferenceProvider;

public class PreferenceAppFilter implements AppFilter {

    @Override
    public boolean shouldShowApp(ComponentName app, Context context) {
        if (app.getPackageName().equals(context.getPackageName()))
            return false;
        IPreferenceProvider prefs = Utilities.getPrefs(context);
        return prefs.getShowHidden() || !Utilities.isAppHidden(context, app.flattenToString());
    }
}
