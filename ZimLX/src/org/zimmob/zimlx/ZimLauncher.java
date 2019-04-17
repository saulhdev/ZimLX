package org.zimmob.zimlx;

import android.content.Context;
import android.os.Bundle;

import com.google.android.apps.nexuslauncher.NexusLauncherActivity;

public class ZimLauncher extends NexusLauncherActivity {

    public static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }
}
