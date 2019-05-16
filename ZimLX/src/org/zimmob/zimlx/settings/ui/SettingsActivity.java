/*
 * Copyright (C) 2015 The Android Open Source Project
 * Copyright (C) 2017 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zimmob.zimlx.settings.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.LauncherFiles;
import com.android.launcher3.R;
import com.android.launcher3.SessionCommitReceiver;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.SettingsObserver;
import com.android.launcher3.views.ButtonPreference;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.FakeLauncherKt;
import org.zimmob.zimlx.ZimLauncher;
import org.zimmob.zimlx.ZimUtilsKt;
import org.zimmob.zimlx.colors.ThemedEditTextPreferenceDialogFragmentCompat;
import org.zimmob.zimlx.colors.ThemedListPreferenceDialogFragment;
import org.zimmob.zimlx.colors.ThemedMultiSelectListPreferenceDialogFragmentCompat;
import org.zimmob.zimlx.gestures.ui.GesturePreference;
import org.zimmob.zimlx.gestures.ui.SelectGestureHandlerFragment;
import org.zimmob.zimlx.globalsearch.ui.SearchProviderPreference;
import org.zimmob.zimlx.globalsearch.ui.SelectSearchProviderFragment;
import org.zimmob.zimlx.iconpack.IconPackManager;
import org.zimmob.zimlx.minibar.Minibar;
import org.zimmob.zimlx.preferences.ColorPreferenceCompat;
import org.zimmob.zimlx.preferences.GridSizeDialogFragmentCompat;
import org.zimmob.zimlx.preferences.GridSizePreference;
import org.zimmob.zimlx.preferences.SingleDimensionGridSizeDialogFragmentCompat;
import org.zimmob.zimlx.preferences.SingleDimensionGridSizePreference;
import org.zimmob.zimlx.smartspace.FeedBridge;
import org.zimmob.zimlx.theme.ThemeOverride;
import org.zimmob.zimlx.theme.ThemeOverride.ThemeSet;
import org.zimmob.zimlx.util.ZimFlags;

import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;
import androidx.preference.internal.AbstractMultiSelectListPreference;

/**
 * Settings activity for Launcher.
 */
public class SettingsActivity extends SettingsBaseActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        FragmentManager.OnBackStackChangedListener, View.OnClickListener {

    public static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";

    public static final String ICON_PACK_PREF = "pref_icon_pack";
    private static final String ICON_BADGING_PREFERENCE_KEY = "pref_icon_badging";
    /**
     * Hidden field Settings.Secure.NOTIFICATION_BADGING
     */
    public static final String NOTIFICATION_BADGING = "notification_badging";
    /**
     * Hidden field Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
     */
    private final static String NOTIFICATION_ENABLED_LISTENERS = "enabled_notification_listeners";

    public final static String SHOW_PREDICTIONS_PREF = "pref_show_predictions";
    public final static String ENABLE_MINUS_ONE_PREF = "pref_enable_minus_one";
    public final static String SMARTSPACE_PREF = "pref_smartspace";

    public final static String EXTRA_TITLE = "title";

    public final static String EXTRA_FRAGMENT = "fragment";
    public final static String EXTRA_FRAGMENT_ARGS = "fragmentArgs";

    private boolean isSubSettings;
    protected boolean forceSubSettings = false;
    public final static String FEED_THEME_PREF = "pref_feedTheme";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        savedInstanceState = getRelaunchInstanceState(savedInstanceState);

        String fragmentName = getIntent().getStringExtra(EXTRA_FRAGMENT);
        int content = getIntent().getIntExtra(SubSettingsFragment.CONTENT_RES_ID, 0);
        isSubSettings = content != 0 || fragmentName != null || forceSubSettings;


        boolean showSearch = shouldShowSearch();

        super.onCreate(savedInstanceState);
        //setContentView(showSearch ? R.layout.activity_settings_home : R.layout.activity_settings);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            Fragment fragment = createLaunchFragment(getIntent());
            // Display the fragment as the main content.
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        updateUpButton();
        /*if (showSearch) {
            Toolbar toolbar = findViewById(R.id.search_action_bar);
            toolbar.setOnClickListener(this);
        }*/
    }

    protected Fragment createLaunchFragment(Intent intent) {
        CharSequence title = intent.getCharSequenceExtra(EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }
        String fragment = intent.getStringExtra(EXTRA_FRAGMENT);
        if (fragment != null) {
            return Fragment.instantiate(this, fragment, intent.getBundleExtra(EXTRA_FRAGMENT_ARGS));
        }
        int content = intent.getIntExtra(SubSettingsFragment.CONTENT_RES_ID, 0);
        return content != 0
                ? SubSettingsFragment.newInstance(getIntent())
                : new LauncherSettingsFragment();
    }

    protected boolean shouldShowSearch() {
        return FeatureFlags.FEATURE_SETTINGS_SEARCH && !isSubSettings;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldShowSearch()) {
            Toolbar toolbar = findViewById(R.id.search_action_bar);
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_restart_zim);
            ActionMenuView menuView;
            int count = toolbar.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof ActionMenuView) {
                    menuView = (ActionMenuView) child;
                    break;
                }
            }

            if (!BuildConfig.APPLICATION_ID.equals(resolveDefaultHome())) {
                toolbar.inflateMenu(R.menu.menu_change_default_home);
            }
            toolbar.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.action_change_default_home:
                        FakeLauncherKt.changeDefaultHome(this);
                        break;
                    case R.id.action_restart_zim:
                        Utilities.killLauncher();
                        break;
                    default:
                        return false;
                }
                return true;
            });
        }
    }

    private String resolveDefaultHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info = getPackageManager()
                .resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (info != null && info.activityInfo != null) {
            return info.activityInfo.packageName;
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        /*if (v.getId() == R.id.search_action_bar) {
            //startActivity(new Intent(this, SettingsSearchActivity.class));
        }*/
    }

    @NotNull
    @Override
    protected ThemeSet getThemeSet() {
        if (getIntent().getBooleanExtra(SubSettingsFragment.HAS_PREVIEW, false)) {
            return new ThemeOverride.SettingsTransparent();
        } else {
            return super.getThemeSet();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {
        Fragment fragment;
        if (preference instanceof SubPreference) {
            Log.e("SETTING P", preference.getFragment());
            ((SubPreference) preference).start(this);
            return true;

        } else {
            fragment = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
        }
        if (fragment instanceof DialogFragment) {
            ((DialogFragment) fragment).show(getSupportFragmentManager(), preference.getKey());
        } else {
            startFragment(this, preference.getFragment(), preference.getExtras(), preference.getTitle());
        }
        return true;
    }

    private void updateUpButton() {
        updateUpButton(isSubSettings || getSupportFragmentManager().getBackStackEntryCount() != 0);
    }

    private void updateUpButton(boolean enabled) {
        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        updateUpButton();
    }

    public abstract static class BaseFragment extends PreferenceFragmentCompat implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            ListAdapter listAdapter = listView.getAdapter();
            Object item = listAdapter.getItem(position);

            if (item instanceof SubPreference) {
                SubPreference subPreference = (SubPreference) item;
                if (subPreference.onLongClick(null)) {
                    ((SettingsActivity) getActivity()).onPreferenceStartFragment(this, subPreference);
                    return true;
                } else {
                    return false;
                }
            }
            return item != null && item instanceof View.OnLongClickListener && ((View.OnLongClickListener) item).onLongClick(view);
        }
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends BaseFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.zim_preferences);
            //onPreferencesAdded(getPreferenceScreen());
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().setTitle(R.string.settings_button_text);
            getActivity().setTitleColor(R.color.white);

        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }
    }

    public static class SubSettingsFragment extends BaseFragment implements
            Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        public static final String TITLE = "title";
        public static final String CONTENT_RES_ID = "content_res_id";
        public static final String HAS_PREVIEW = "has_preview";

        private SystemDisplayRotationLockObserver mRotationLockObserver;
        private IconBadgingObserver mIconBadgingObserver;

        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mContext = getActivity();

            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            int preference = getContent();
            ContentResolver resolver = getActivity().getContentResolver();

            switch (preference) {
                case R.xml.zim_preferences_desktop:
                    if (!Utilities.ATLEAST_OREO) {
                        getPreferenceScreen().removePreference(
                                findPreference(SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY));
                    }
                    // Setup allow rotation preference
                    Preference rotationPref = findPreference(Utilities.ALLOW_ROTATION_PREFERENCE_KEY);
                    if (getResources().getBoolean(R.bool.allow_rotation)) {
                        // Launcher supports rotation by default. No need to show this setting.
                        getPreferenceScreen().removePreference(rotationPref);
                    } else {
                        mRotationLockObserver = new SystemDisplayRotationLockObserver(rotationPref, resolver);

                        // Register a content observer to listen for system setting changes while
                        // this UI is active.
                        mRotationLockObserver.register(Settings.System.ACCELEROMETER_ROTATION);

                        // Initialize the UI once
                        rotationPref.setDefaultValue(Utilities.getAllowRotationDefaultValue(getActivity()));

                    }

                    break;

                case R.xml.zim_preferences_app_drawer:
                    findPreference(SHOW_PREDICTIONS_PREF).setOnPreferenceChangeListener(this);
                    break;
                case R.xml.zim_preferences_theme:
                    IconPackManager ipm = IconPackManager.Companion.getInstance(mContext);
                    Preference packMaskingPreference = findPreference("pref_iconPackMasking");
                    PreferenceGroup parent = packMaskingPreference.getParent();
                    ipm.addListener(() -> {
                        if (!ipm.maskSupported()) {
                            parent.removePreference(packMaskingPreference);
                        } else if (parent.findPreference("pref_iconPackMasking") == null) {
                            parent.addPreference(packMaskingPreference);
                        }
                        return null;
                    });
                    break;

                case R.xml.zim_preferences_notification:
                    ButtonPreference iconBadgingPref =
                            (ButtonPreference) findPreference(ICON_BADGING_PREFERENCE_KEY);
                    if (!getResources().getBoolean(R.bool.notification_badging_enabled)) {
                        getPreferenceScreen().removePreference(iconBadgingPref);
                    } else {
                        // Listen to system notification badge settings while this UI is active.
                        mIconBadgingObserver = new IconBadgingObserver(
                                iconBadgingPref, resolver, getFragmentManager());
                        mIconBadgingObserver.register(NOTIFICATION_BADGING, NOTIFICATION_ENABLED_LISTENERS);
                    }

                    break;

                case R.xml.zim_preferences_dev_options:
                    findPreference("kill").setOnPreferenceClickListener(this);
                    break;

            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(getContent());
        }

        private int getContent() {
            return getArguments().getInt(CONTENT_RES_ID);
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().setTitle(getArguments().getString(TITLE));
            if (getContent() == R.xml.zim_preferences_smartspace) {
                SwitchPreference minusOne = (SwitchPreference) findPreference(
                        ENABLE_MINUS_ONE_PREF);
                if (minusOne != null && !FeedBridge.Companion.getInstance(getActivity())
                        .isInstalled()) {
                    minusOne.setChecked(false);
                }
            }
        }

        @Override
        public void onDestroy() {
            if (mRotationLockObserver != null) {
                mRotationLockObserver.unregister();
                mRotationLockObserver = null;
            }
            if (mIconBadgingObserver != null) {
                mIconBadgingObserver.unregister();
                mIconBadgingObserver = null;
            }
            super.onDestroy();
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            final DialogFragment f;
            if (preference instanceof GridSizePreference) {
                f = GridSizeDialogFragmentCompat.Companion.newInstance(preference.getKey());
            } else if (preference instanceof SingleDimensionGridSizePreference) {
                f = SingleDimensionGridSizeDialogFragmentCompat.Companion
                        .newInstance(preference.getKey());
            } else if (preference instanceof GesturePreference) {
                f = SelectGestureHandlerFragment.Companion
                        .newInstance((GesturePreference) preference);
            } else if (preference instanceof SearchProviderPreference) {
                f = SelectSearchProviderFragment.Companion
                        .newInstance((SearchProviderPreference) preference);
            } else if (preference instanceof ListPreference) {
                Log.d("success", "onDisplayPreferenceDialog: yay");
                f = ThemedListPreferenceDialogFragment.Companion.newInstance(preference.getKey());
            } else if (preference instanceof EditTextPreference) {
                f = ThemedEditTextPreferenceDialogFragmentCompat.Companion
                        .newInstance(preference.getKey());
            } else if (preference instanceof AbstractMultiSelectListPreference) {
                f = ThemedMultiSelectListPreferenceDialogFragmentCompat.Companion
                        .newInstance(preference.getKey());
            } else {
                super.onDisplayPreferenceDialog(preference);
                return;
            }
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
        }

        public static SubSettingsFragment newInstance(SubPreference preference) {
            SubSettingsFragment fragment = new SubSettingsFragment();
            Bundle b = new Bundle(2);
            b.putString(TITLE, (String) preference.getTitle());
            b.putInt(CONTENT_RES_ID, preference.getContent());
            fragment.setArguments(b);
            return fragment;
        }

        public static SubSettingsFragment newInstance(Intent intent) {
            SubSettingsFragment fragment = new SubSettingsFragment();
            Bundle b = new Bundle(2);
            b.putString(TITLE, intent.getStringExtra(TITLE));
            b.putInt(CONTENT_RES_ID, intent.getIntExtra(CONTENT_RES_ID, 0));
            fragment.setArguments(b);
            return fragment;
        }

        public static SubSettingsFragment newInstance(String title, @XmlRes int content) {
            SubSettingsFragment fragment = new SubSettingsFragment();
            Bundle b = new Bundle(2);
            b.putString(TITLE, title);
            b.putInt(CONTENT_RES_ID, content);
            fragment.setArguments(b);
            return fragment;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case SHOW_PREDICTIONS_PREF:
                    if ((boolean) newValue) {
                        return true;
                    }
                    SuggestionConfirmationFragment confirmationFragment = new SuggestionConfirmationFragment();
                    confirmationFragment.setTargetFragment(this, 0);
                    confirmationFragment.show(getFragmentManager(), preference.getKey());
                    break;
            }
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "kill":
                    Utilities.killLauncher();
                    break;

                case "crashLauncher":
                    throw new RuntimeException("Triggered from developer options");

                case "appInfo":
                    ComponentName componentName = new ComponentName(getActivity(),
                            ZimLauncher.class);
                    LauncherAppsCompat.getInstance(getContext())
                            .showAppDetailsForProfile(componentName,
                                    android.os.Process.myUserHandle());
                    break;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey() != null) {
                switch (preference.getKey()) {
                    case ZimFlags.MINIBAR:
                        Minibar.RunAction(Minibar.Action.EditMinibar, getActivity());
                        break;

                    default:
                        if (preference instanceof ColorPreferenceCompat) {
                            ColorPickerDialog dialog = ((ColorPreferenceCompat) preference).getDialog();
                            dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                                public void onColorSelected(int dialogId, int color) {
                                    ((ColorPreferenceCompat) preference).saveValue(color);
                                }

                                public void onDialogDismissed(int dialogId) {
                                }
                            });
                            dialog.show((getActivity()).getFragmentManager(), "color-picker-dialog");
                        }
                }
            }
            return false;
        }
    }

    public static class SuggestionConfirmationFragment extends DialogFragment implements
            DialogInterface.OnClickListener {

        public void onClick(final DialogInterface dialogInterface, final int n) {
            if (getTargetFragment() instanceof PreferenceFragmentCompat) {
                Preference preference = ((PreferenceFragmentCompat) getTargetFragment())
                        .findPreference(SHOW_PREDICTIONS_PREF);
                if (preference instanceof TwoStatePreference) {
                    ((TwoStatePreference) preference).setChecked(false);
                }
            }
        }

        public Dialog onCreateDialog(final Bundle bundle) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_disable_suggestions_prompt)
                    .setMessage(R.string.msg_disable_suggestions_prompt)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.label_turn_off_suggestions, this).create();
        }


        @Override
        public void onStart() {
            super.onStart();
            ZimUtilsKt.applyAccent(((AlertDialog) getDialog()));
        }
    }

    /**
     * Content observer which listens for system auto-rotate setting changes, and enables/disables
     * the launcher rotation setting accordingly.
     */
    private static class SystemDisplayRotationLockObserver extends SettingsObserver.System {

        private final Preference mRotationPref;

        public SystemDisplayRotationLockObserver(
                Preference rotationPref, ContentResolver resolver) {
            super(resolver);
            mRotationPref = rotationPref;
        }

        @Override
        public void onSettingChanged(boolean enabled) {
            mRotationPref.setEnabled(enabled);
            mRotationPref.setSummary(enabled
                    ? R.string.allow_rotation_desc : R.string.allow_rotation_blocked_desc);
        }
    }

    /**
     * Content observer which listens for system badging setting changes, and updates the launcher
     * badging setting subtext accordingly.
     */
    private static class IconBadgingObserver extends SettingsObserver.Secure
            implements Preference.OnPreferenceClickListener {

        private final ButtonPreference mBadgingPref;
        private final ContentResolver mResolver;
        private final FragmentManager mFragmentManager;
        private boolean serviceEnabled = true;

        public IconBadgingObserver(ButtonPreference badgingPref, ContentResolver resolver,
                                   FragmentManager fragmentManager) {
            super(resolver);
            mBadgingPref = badgingPref;
            mResolver = resolver;
            mFragmentManager = fragmentManager;
        }

        @Override
        public void onSettingChanged(boolean enabled) {
            int summary = enabled ? R.string.icon_badging_desc_on : R.string.icon_badging_desc_off;

            if (enabled) {
                // Check if the listener is enabled or not.
                String enabledListeners =
                        Settings.Secure.getString(mResolver, NOTIFICATION_ENABLED_LISTENERS);
                ComponentName myListener =
                        new ComponentName(mBadgingPref.getContext(), NotificationListener.class);
                serviceEnabled = enabledListeners != null &&
                        (enabledListeners.contains(myListener.flattenToString()) ||
                                enabledListeners.contains(myListener.flattenToShortString()));
                if (!serviceEnabled) {
                    summary = R.string.title_missing_notification_access;
                }
            }
            mBadgingPref.setWidgetFrameVisible(!serviceEnabled);
            mBadgingPref.setOnPreferenceClickListener(
                    serviceEnabled && Utilities.ATLEAST_OREO ? null : this);
            mBadgingPref.setSummary(summary);

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (!Utilities.ATLEAST_OREO && serviceEnabled) {
                ComponentName cn = new ComponentName(preference.getContext(),
                        NotificationListener.class);
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(":settings:fragment_args_key", cn.flattenToString());
                preference.getContext().startActivity(intent);
            } else {
                new SettingsActivity.NotificationAccessConfirmation()
                        .show(mFragmentManager, "notification_access");
            }
            return true;
        }
    }

    public static class NotificationAccessConfirmation
            extends DialogFragment implements DialogInterface.OnClickListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            String msg = context.getString(R.string.msg_missing_notification_access,
                    context.getString(R.string.derived_app_name));
            return new AlertDialog.Builder(context)
                    .setTitle(R.string.title_missing_notification_access)
                    .setMessage(msg)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.title_change_settings, this)
                    .create();
        }

        @Override
        public void onStart() {
            super.onStart();
            ZimUtilsKt.applyAccent(((AlertDialog) getDialog()));
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            ComponentName cn = new ComponentName(getActivity(), NotificationListener.class);
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(":settings:fragment_args_key", cn.flattenToString());
            getActivity().startActivity(intent);
        }
    }

    public static void startFragment(Context context, String fragment, @Nullable Bundle args,
                                     @Nullable CharSequence title) {
        context.startActivity(createFragmentIntent(context, fragment, args, title));
    }

    @NotNull
    private static Intent createFragmentIntent(Context context, String fragment,
                                               @Nullable Bundle args, @Nullable CharSequence title) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_FRAGMENT, fragment);
        intent.putExtra(EXTRA_FRAGMENT_ARGS, args);
        if (title != null) {
            intent.putExtra(EXTRA_TITLE, title);
        }
        return intent;
    }
}