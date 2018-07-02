package org.zimmob.zimlx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceAdminReceiver extends BroadcastReceiver {
    public static final String TAG = "DeviceAdminReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "DeviceAdmin received");
    }
}
