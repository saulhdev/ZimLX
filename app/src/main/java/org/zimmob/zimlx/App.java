package org.zimmob.zimlx;

import android.app.Application;

import org.zimmob.zimlx.preferences.PreferenceImpl;
import org.zimmob.zimlx.preferences.PreferenceProvider;

public class App extends Application {
    private static App _instance;

    public static App get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceProvider.INSTANCE.init(new PreferenceImpl(this));
    }
}
