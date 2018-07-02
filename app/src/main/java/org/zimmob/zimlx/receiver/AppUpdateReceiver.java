package org.zimmob.zimlx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.zimmob.zimlx.manager.Setup;

public class AppUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Setup.appLoader().onAppUpdated(context, intent);
    }
}

