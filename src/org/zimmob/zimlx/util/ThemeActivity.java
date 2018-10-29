package org.zimmob.zimlx.util;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.launcher3.Utilities;

import org.zimmob.zimlx.ZimPreferences;

public class ThemeActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZimPreferences prefs = Utilities.getZimPrefs(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dark(prefs.getPrimaryColor()));
        }
    }

    private int dark(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, Math.max((int) (r * 0.8), 0), Math.max((int) (g * 0.8), 0), Math.max((int) (b * 0.8), 0));
    }
}
