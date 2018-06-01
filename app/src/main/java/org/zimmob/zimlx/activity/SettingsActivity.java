package org.zimmob.zimlx.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;
import net.gsantner.opoc.util.ContextUtils;
import net.gsantner.opoc.util.PermissionChecker;

import org.jetbrains.annotations.Contract;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.icon.IconsHandler;
import org.zimmob.zimlx.launcher.Launcher;
import org.zimmob.zimlx.preference.ColorPreferenceCompat;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DatabaseHelper;
import org.zimmob.zimlx.launcher.LauncherAction;
import org.zimmob.zimlx.util.DialogHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.zimmob.zimlx.config.Config.DRAWER_HORIZONTAL;
import static org.zimmob.zimlx.config.Config.DRAWER_VERTICAL;


/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class SettingsActivity extends ThemeActivity {
    static class RESULT {
        static final int NOCHANGE = -1;
        static final int CHANGED = 1;
        static final int RESTART_REQ = 2;
    }

    private static int activityRetVal = RESULT.NOCHANGE;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    public void onCreate(Bundle b) {
        // Must be applied before setContentView
        super.onCreate(b);
        ContextUtils contextUtils = new ContextUtils(this);
        contextUtils.setAppLanguage(_appSettings.getLanguage());

        // Load UI
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        // Custom code
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());
        showFragment(SettingsFragmentMaster.TAG, false);
    }

    private void showFragment(String tag, boolean addToBackStack) {
        String toolbarTitle = getString(R.string.settings);
        GsPreferenceFragmentCompat prefFrag = (GsPreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(tag);
        if (prefFrag == null) {
            switch (tag) {
                case SettingsFragmentMaster.TAG:
                default: {
                    prefFrag = new SettingsFragmentMaster();
                    toolbar.setTitle(prefFrag.getTitleOrDefault(toolbarTitle));
                    break;
                }
            }
        }
        toolbar.setTitle(toolbarTitle);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            t.addToBackStack(tag);
        }
        t.replace(R.id.settings__activity__fragment_placeholder, prefFrag, tag).commit();
    }

    @Override
    protected void onStop() {
        setResult(activityRetVal);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        GsPreferenceFragmentCompat prefFrag = (GsPreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(SettingsFragmentMaster.TAG);
        if (prefFrag != null && prefFrag.canGoBack()) {
            prefFrag.goBack();
            return;
        }
        super.onBackPressed();
    }

    public static abstract class OlSettingsFragment extends GsPreferenceFragmentCompat<AppSettings> {
        AppSettings _as;

        @Override
        protected AppSettings getAppSettings(Context context) {
            if (_as == null) {
                _as = AppSettings.get();
            }
            return _as;
        }

        @Override
        protected void onPreferenceChanged(SharedPreferences prefs, String key) {
            activityRetVal = RESULT.CHANGED;
        }

        @Override
        protected void onPreferenceScreenChanged(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
            super.onPreferenceScreenChanged(preferenceFragmentCompat, preferenceScreen);
            if (!TextUtils.isEmpty(preferenceScreen.getTitle())) {
                SettingsActivity a = (SettingsActivity) getActivity();
                if (a != null) {
                    a.toolbar.setTitle(preferenceScreen.getTitle());
                }
            }
        }
    }

    public static class SettingsFragmentMaster extends OlSettingsFragment {
        static final String TAG = "SettingsFragmentMaster";

        private static final int[] requireRestartPreferenceIds = new int[]{
                R.string.pref_key__desktop_columns,
                R.string.pref_key__desktop_rows,
                R.string.pref_key__desktop_style,
                R.string.pref_key__desktop_fullscreen,
                R.string.pref_key__desktop_show_label,
                R.string.pref_key__desktop_background_color,
                R.string.pref_key__search_bar_enable,
                R.string.pref_key__search_bar_show_hidden_apps,
                R.string.pref_key__minibar_background_color,
                R.string.pref_key__dock_enable,
                R.string.pref_key__dock_size,
                R.string.pref_key__dock_show_label,
                R.string.pref_key__dock_background_color,
                R.string.pref_key__drawer_columns,
                R.string.pref_key__drawer_rows,
                R.string.pref_key__drawer_style,
                R.string.pref_key__drawer_show_card_view,
                R.string.pref_key__drawer_show_position_indicator,
                R.string.pref_key__drawer_show_label,
                R.string.pref_key__drawer_background_color,
                R.string.pref_key__drawer_card_color,
                R.string.pref_key__drawer_label_color,
                R.string.pref_key__drawer_fast_scroll_color,
                R.string.pref_key__folder_shape,
                R.string.pref_key__date_bar_date_format_custom_1,
                R.string.pref_key__date_bar_date_format_custom_2,
                R.string.pref_key__date_bar_date_format_type,
                R.string.pref_key__date_bar_date_text_color,
                R.string.pref_key__icon_size,
                R.string.pref_key__icon_pack,
                R.string.pref_title__clear_database,
                R.string.pref_summary__backup,
                R.string.pref_summary__theme
        };

        @Override
        public int getPreferenceResourceForInflation() {
            return R.xml.preferences_master;
        }

        @Override
        public String getFragmentTag() {
            return TAG;
        }

        @Contract(pure = true)
        private boolean requiresRestart(int key) {
            for (int k : requireRestartPreferenceIds) {
                if (k == key) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void doUpdatePreferences() {
            Preference pref;
            String tmp;

            //
            // Preference in Master screen
            //
            if ((pref = findPreference(R.string.pref_key__cat_desktop)) != null) {
                tmp = String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDesktopColumnCount(), _as.getDesktopRowCount());
                pref.setSummary(tmp);
            }
            if ((pref = findPreference(R.string.pref_key__cat_dock)) != null) {
                tmp = String.format(Locale.ENGLISH, "%s: %d", getString(R.string.pref_title__size), _as.getDockSize());
                pref.setSummary(tmp);
            }
            if ((pref = findPreference(R.string.pref_key__cat_app_drawer)) != null) {
                tmp = String.format("%s: ", getString(R.string.pref_title__style));
                switch (_as.getDrawerStyle()) {
                    case DRAWER_HORIZONTAL: {
                        tmp += getString(R.string.horizontal_paged_drawer);
                        break;
                    }
                    case DRAWER_VERTICAL: {
                        tmp += getString(R.string.vertical_scroll_drawer);
                        break;
                    }
                }
                pref.setSummary(tmp);
            }
            if ((pref = findPreference(R.string.pref_key__cat_icons)) != null) {
                tmp = String.format(Locale.ENGLISH, "%s: %ddp", getString(R.string.pref_title__size), _as.getIconSize());
                pref.setSummary(tmp);
            }

            if ((pref = findPreference(R.string.pref_key__cat_appearance)) != null) {
                tmp = String.format(Locale.ENGLISH, "%s: ", getString(R.string.pref_title__theme));
                switch (_as.getTheme()) {
                    case "0": {
                        tmp += getString(R.string.theme_light);
                        break;
                    }
                    case "1": {
                        tmp += getString(R.string.theme_dark);
                        break;
                    }
                }
                pref.setSummary(tmp);
            }

            if ((pref = findPreference(R.string.pref_key__cat_folders)) != null) {
                tmp = String.format(Locale.ENGLISH, "%s: ", getString(R.string.pref_title__folder_shape));
                switch (_as.getFolderShape()) {
                    case 0: {
                        tmp += getString(R.string.title_folder_circle);
                        break;
                    }
                    case 1: {
                        tmp += getString(R.string.title_folder_circle_bg);
                        break;
                    }
                    case 2: {
                        tmp += getString(R.string.title_folder_square);
                        break;
                    }
                    case 3: {
                        tmp += getString(R.string.title_folder_square_bg);
                        break;
                    }
                }
                pref.setSummary(tmp);
            }
        }

        @Override
        protected void onPreferenceChanged(SharedPreferences prefs, String key) {
            super.onPreferenceChanged(prefs, key);
            int keyRes = _cu.getResId(ContextUtils.ResType.STRING, key);
            Launcher launcher = Launcher.Companion.getLauncher();
            switch (keyRes) {
                case R.string.pref_key__desktop_indicator_style: {
                    launcher.getDesktopIndicator().setMode(_as.getDesktopIndicatorMode());
                    break;
                }
                case R.string.pref_title__desktop_show_position_indicator: {
                    launcher.updateDesktopIndicatorVisibility();
                    break;
                }
                case R.string.pref_key__dock_enable: {
                    launcher.updateDock(true);
                    break;
                }
                case R.string.pref_key__gesture_double_tap:
                case R.string.pref_key__gesture_swipe_up:
                case R.string.pref_key__gesture_swipe_down:
                case R.string.pref_key__gesture_pinch:
                case R.string.pref_key__gesture_unpinch: {
                    if (prefs.getString(key, "0").equals("9")) {
                        DialogHelper.selectAppDialog(getContext(), app ->
                                prefs.edit().putString(key + "__", app.getPackageName()).apply());
                    }
                    break;
                }
            }
            if (requiresRestart(keyRes)) {
                activityRetVal = RESULT.RESTART_REQ;
                _as.setAppRestartRequired(true);
            }
        }

        @Override
        @SuppressWarnings({"ConstantConditions", "ConstantIfStatement", "StatementWithEmptyBody"})
        public Boolean onPreferenceClicked(Preference preference) {
            int keyRes = _cu.getResId(ContextUtils.ResType.STRING, preference.getKey());
            Launcher launcher = Launcher.Companion.getLauncher();

            switch (keyRes) {
                case R.string.pref_key__minibar: {
                    LauncherAction.RunAction(LauncherAction.Action.EditMinibar, getActivity());
                    return true;
                }
                case R.string.pref_key__hidden_apps: {
                    Intent intent = new Intent(getActivity(), HideAppsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    return true;
                }
                case R.string.pref_key__icon_pack: {
                    IconsHandler iconsHandler = new IconsHandler(getContext());
                    iconsHandler.showDialog(getActivity());
                    //AppManager.getInstance(getActivity()).startPickIconPackIntent(getActivity());
                    return true;
                }
                case R.string.pref_key__clear_database: {
                    DialogHelper.alertDialog(getContext(), getString(R.string.clear_user_data), getString(R.string.clear_user_data_are_you_sure), (dialog, which) -> {
                        if (launcher != null) {
                            launcher.recreate();
                        }
                        DatabaseHelper db = (DatabaseHelper) Launcher._db;
                        db.onUpgrade(db.getWritableDatabase(), 1, 1);
                        getActivity().finish();
                    });
                    return true;
                }
                case R.string.pref_key__backup: {
                    if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                        DialogHelper.backupDialog(getActivity());
                    }
                    return true;
                }
                case R.string.pref_key__restart: {
                    if (launcher != null) {
                        launcher.recreate();
                    }
                    getActivity().finish();
                    return true;
                }
                case R.string.pref_key__cat_about: {
                    startActivity(new Intent(getActivity(), MoreInfoActivity.class));
                    return true;
                }
            }

            if (preference instanceof ColorPreferenceCompat) {
                ColorPickerDialog dialog = ((ColorPreferenceCompat) preference).getDialog();
                dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    public void onColorSelected(int dialogId, int color) {
                        ((ColorPreferenceCompat) preference).saveValue(color);
                    }

                    public void onDialogDismissed(int dialogId) {
                    }
                });
                dialog.show(getActivity().getFragmentManager(), "color-picker-dialog");
            }
            return false;
        }
    }
}