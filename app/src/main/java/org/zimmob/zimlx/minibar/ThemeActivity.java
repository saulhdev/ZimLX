package org.zimmob.zimlx.minibar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.settings.AppSettings;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class ThemeActivity extends AppCompatActivity {

    AppSettings appSettings;
    private String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appSettings = new AppSettings(this);
        currentTheme = appSettings.getTheme();
        switch (currentTheme) {
            case "0":
                setTheme(R.style.LauncherTheme);
                break;
            case "1":
                setTheme(R.style.LauncherTheme_Dark);
                break;
            case "2":
                setTheme(R.style.LauncherTheme_Black);
                break;
            default:
                setTheme(R.style.LauncherTheme);
                break;
        }

        getWindow().setStatusBarColor(dark(Utilities.getPrefs(this).getPrimaryColor()));

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!appSettings.getTheme().equals(currentTheme)) {
            restart();
        }
    }

    private void restart() {
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private int dark(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, Math.max((int) (r * 0.8), 0), Math.max((int) (g * 0.8), 0), Math.max((int) (b * 0.8), 0));
    }
}