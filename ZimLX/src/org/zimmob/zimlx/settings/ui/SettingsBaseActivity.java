package org.zimmob.zimlx.settings.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import org.zimmob.zimlx.ZimPreferences;
import org.zimmob.zimlx.util.ThemeActivity;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsBaseActivity extends ThemeActivity {
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    private DecorLayout decorLayout;
    public SettingsDragLayer dragLayer;
    private Window window;

    // We do not need any synchronization for this variable as its only written on UI thread.
    private static SettingsBaseActivity INSTANCE;

    protected void onCreate(Bundle savedInstanceState) {
        Utilities.setupPirateLocale(this);
        super.onCreate(savedInstanceState);
        ZimPreferences prefs = Utilities.getZimPrefs(this);

        window = getWindow();
        decorLayout = new DecorLayout(this, window);
        dragLayer = new SettingsDragLayer(this, null);
        dragLayer.addView(decorLayout,
                new InsettableFrameLayout
                        .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        super.setContentView(decorLayout);
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

    public void setActionBarElevation(int value) {
        decorLayout.setActionBarElevation(value);
    }

    public Activity getActivity() {

        return this;
    }

    public static SettingsBaseActivity getInstance(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SettingsBaseActivity();

        }
        return INSTANCE;
    }
}
