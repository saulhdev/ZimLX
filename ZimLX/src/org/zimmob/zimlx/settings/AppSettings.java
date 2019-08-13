package org.zimmob.zimlx.settings;

import android.content.Context;

import com.android.launcher3.AppObject;

import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

public class AppSettings extends SharedPreferencesPropertyBackend {
    private Context mContext;

    public AppSettings(Context context) {
        super(context);
        mContext = context;
    }

    public static AppSettings get() {
        return new AppSettings(AppObject.get());
    }


    @Override
    public Context getContext() {
        return mContext;
    }

}

