package com.google.android.apps.nexuslauncher.b;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import androidx.annotation.NonNull;

import com.android.launcher3.util.InstantAppResolver;
import com.google.android.apps.nexuslauncher.UIUpdateHandler;

public class b implements Callback {
    private static b I = null;
    private final Context mContext;
    private final InstantAppResolver mInstantAppResolver;
    public UIUpdateHandler K;
    public Handler b;

    public b(Context context) {
        mContext = context;
        mInstantAppResolver = new InstantAppResolver();
    }

    public static b b(Context context) {
        b(context);
        return I;
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        return false;
    }
}
