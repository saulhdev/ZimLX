package org.zimmob.zimlx.settings.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import org.zimmob.zimlx.ZimPreferences;
import org.zimmob.zimlx.util.ThemeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsBaseActivity extends ThemeActivity {
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    private DecorLayout decorLayout;
    private Window window;
    private ZimPreferences sharedPrefs;

    protected void onCreate(Bundle savedInstanceState) {
        Utilities.setupPirateLocale(this);
        super.onCreate(savedInstanceState);
        window = getWindow();
        decorLayout = new DecorLayout(this, window);
        super.setContentView(decorLayout);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        sharedPrefs = Utilities.getZimPrefs(this);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(sharedPrefs.getPrimaryColor());
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*if (!Utilities.ATLEAST_OREO_MR1 && Utilities.ATLEAST_OREO) {
            Utilities.setLightUi(window);
            window.setStatusBarColor(0);
            window.setNavigationBarColor(0);
        }*/
    }

    public void setContentView(View v) {
        ViewGroup contentParent = decorLayout.findViewById(android.R.id.content);
        contentParent.removeAllViews();
        contentParent.addView(v);
    }

    public void setContentView(int resId) {
        ViewGroup contentParent = decorLayout.findViewById(android.R.id.content);
        contentParent.removeAllViews();
        LayoutInflater.from(this).inflate(resId, contentParent);
    }

    public void setContentView(View v, ViewGroup.LayoutParams lp) {
        ViewGroup contentParent = decorLayout.findViewById(android.R.id.content);
        contentParent.removeAllViews();
        contentParent.addView(v, lp);
    }

    public Float getActionBarElevation() {
        return decorLayout.getActionBarElevation();
    }

    public void setActionBarElevation(int value) {
        decorLayout.setActionBarElevation(value);
    }
}
