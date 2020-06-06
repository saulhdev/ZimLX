package com.google.android.apps.nexuslauncher.a;

import android.content.Context;
import android.os.Handler;

import com.google.android.apps.nexuslauncher.UIUpdateHandler;

public class a {
    public static a I;
    public final Handler b;
    public Context mContext;
    public UIUpdateHandler e;

    public a(Context context) {
        mContext = context;
        b = new Handler();
    }

    public static a a(Context context) {
        a(context);
        return I;
    }


}
