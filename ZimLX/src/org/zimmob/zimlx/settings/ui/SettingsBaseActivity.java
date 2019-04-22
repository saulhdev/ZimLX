package org.zimmob.zimlx.settings.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import org.zimmob.zimlx.ZimPreferences;
import org.zimmob.zimlx.theme.ThemeManager;
import org.zimmob.zimlx.theme.ThemeOverride;
import org.zimmob.zimlx.theme.ThemeOverride.ThemeSet;
import org.zimmob.zimlx.util.ThemeActivity;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsBaseActivity extends ThemeActivity implements ThemeManager.ThemeableActivity {
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    private DecorLayout decorLayout;
    public SettingsDragLayer dragLayer;
    private Window window;
    private ThemeSet themeSet = new ThemeOverride.Settings();
    private ThemeOverride themeOverride;
    private int currentTheme = 0;
    private boolean paused = false;
    // We do not need any synchronization for this variable as its only written on UI thread.
    private static SettingsBaseActivity INSTANCE;

    protected void onCreate(Bundle savedInstanceState) {

        Utilities.setupPirateLocale(this);
        super.onCreate(savedInstanceState);
        ZimPreferences prefs = Utilities.getZimPrefs(this);
        themeOverride = new ThemeOverride(themeSet, this);
        themeOverride.applyTheme(this);
        currentTheme = themeOverride.getTheme(this);
        window = getWindow();
        decorLayout = new DecorLayout(this, window);
        dragLayer = new SettingsDragLayer(this, null);
        dragLayer.addView(decorLayout,
                new InsettableFrameLayout
                        .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        super.setContentView(dragLayer);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(prefs.getPrimaryColor());
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setContentView(int resId) {
        ViewGroup contentParent = decorLayout.findViewById(android.R.id.content);
        contentParent.removeAllViews();
        LayoutInflater.from(this).inflate(resId, contentParent);
    }

    protected ThemeSet getThemeSet() {
        return themeSet;
    }

    public void setActionBarElevation(int value) {
        decorLayout.setActionBarElevation(value);
    }

    public Activity getActivity() {

        return this;
    }

    public Bundle getRelaunchInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) return savedInstanceState;
        else
            return getIntent().getBundleExtra("state");
    }

    public static SettingsBaseActivity getInstance(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SettingsBaseActivity();

        }
        return INSTANCE;
    }

    protected Intent createRelaunchIntent() {
        Bundle state = new Bundle();
        onSaveInstanceState(state);
        return getIntent().putExtra("state", state);
    }

    @Override
    public void onThemeChanged() {
        if (currentTheme == themeOverride.getTheme(this)) return;
        if (paused) {
            recreate();
        } else {
            finish();
            startActivity(createRelaunchIntent(), ActivityOptions.makeCustomAnimation(
                    this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle());
        }
    }
}
