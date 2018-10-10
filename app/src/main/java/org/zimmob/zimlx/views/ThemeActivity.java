package org.zimmob.zimlx.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.config.FeatureFlags;

public class ThemeActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeatureFlags.applyDarkTheme(this);
        getWindow().setStatusBarColor(dark(Utilities.getPrefs(this).getPrimaryColor()));
    }

    private int dark(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, Math.max((int) (r * 0.8), 0), Math.max((int) (g * 0.8), 0), Math.max((int) (b * 0.8), 0));
    }
}
