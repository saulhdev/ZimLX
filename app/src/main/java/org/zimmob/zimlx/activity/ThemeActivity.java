package org.zimmob.zimlx.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.util.AppSettings;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class ThemeActivity extends AppCompatActivity {

    protected AppSettings _appSettings;
    private String _currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _appSettings = AppSettings.get();
        _currentTheme = _appSettings.getTheme();
        if (_appSettings.getTheme().equals("0")) {
            setTheme(R.style.NormalActivity_Light);
        } else {
            setTheme(R.style.NormalActivity_Dark);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dark(_appSettings.getPrimaryColor(), 0.8));
            //getWindow().setNavigationBarColor(_appSettings.getPrimaryColor());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!_appSettings.getTheme().equals(_currentTheme)) {
            restart();
        }
    }

    protected void restart() {
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public int dark(int color, double factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }
}