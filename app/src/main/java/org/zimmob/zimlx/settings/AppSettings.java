package org.zimmob.zimlx.settings;

import android.content.Context;

import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

import org.zimmob.zimlx.App;

public class AppSettings extends SharedPreferencesPropertyBackend {
    //TODO: Hacer la configuracion inicial
    public AppSettings(Context context) {
        super(context);
    }

    public static AppSettings get() {
        return new AppSettings(App.get());
    }
}

