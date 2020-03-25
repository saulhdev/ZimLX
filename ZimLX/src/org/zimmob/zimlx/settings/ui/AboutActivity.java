package org.zimmob.zimlx.settings.ui;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import org.zimmob.zimlx.theme.ThemeOverride;
import org.zimmob.zimlx.util.ThemeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class AboutActivity extends ThemeActivity {

    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    private ThemeOverride themeOverride;
    private int currentTheme = 0;
    private ThemeOverride.ThemeSet themeSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utilities.setupPirateLocale(this);
        themeSet = new ThemeOverride.Settings();
        themeOverride = new ThemeOverride(themeSet, this);
        themeOverride.applyTheme(this);
        currentTheme = themeOverride.getTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_more);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Utilities.getZimPrefs(this).getPrimaryColor());
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.about);
    }
}