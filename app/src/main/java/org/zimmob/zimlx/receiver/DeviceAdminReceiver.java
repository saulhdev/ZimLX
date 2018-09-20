package org.zimmob.zimlx.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.zimmob.zimlx.R;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
    public static final String TAG = "DeviceAdminReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "DeviceAdmin received");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent){
        return context.getString(R.string.dt2s_admin_warning);
    }
}
