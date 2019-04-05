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
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherFiles;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.R;
import com.android.launcher3.SessionCommitReceiver;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.IconShapeOverride;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.LooperExecutor;
import com.android.launcher3.util.SettingsObserver;
import com.android.launcher3.views.ButtonPreference;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import org.zimmob.zimlx.minibar.Minibar;
import org.zimmob.zimlx.preferences.ColorPreferenceCompat;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceRecyclerViewAccessibilityDelegate;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;

import static com.android.launcher3.Utilities.restartLauncher;
import static org.zimmob.zimlx.util.ZimFlags.APPDRAWER_SHOW_PREDICTIONS;
import static org.zimmob.zimlx.util.ZimFlags.APPDRAWER_SORT_MODE;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends SettingsBaseActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private int mAppBarHeight;

    public final static String NOTIFICATION_BADGING = "notification_badging";
    public final static String ICON_PACK_PREF = "pref_icon_pack";
    public final static String ENABLE_MINUS_ONE_PREF = "pref_enable_minus_one";
    public final static String SMARTSPACE_PREF = "pref_smartspace";
    public final static String ICON_BADGING_PREFERENCE_KEY = "pref_icon_badging";
    public final static String NOTIFICATION_ENABLED_LISTENERS = "enabled_notification_listeners";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.setLightUi(getWindow());
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mAppBarHeight = getResources().getDimensionPixelSize(R.dimen.app_bar_elevation);

        if (savedInstanceState == null) {
            // Display the fragment as the main content.
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, new LauncherSettingsFragment())
                    .commit();
        }
        updateUpButton();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {
        Fragment fragment = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
        if (fragment instanceof DialogFragment) {
            ((DialogFragment) fragment).show(getSupportFragmentManager(), preference.getKey());
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            setTitle(preference.getTitle());
            transaction.setCustomAnimations(R.animator.fly_in, R.animator.fade_out, R.animator.fade_in, R.animator.fly_out);
            transaction.replace(R.id.content, fragment);
            transaction.addToBackStack("PreferenceFragment");
            transaction.commit();
            updateUpButton(true);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUpButton();
    }

    private void updateUpButton() {
        updateUpButton(getSupportFragmentManager().getBackStackEntryCount() != 0);
    }

    private void updateUpButton(boolean enabled) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
        setActionBarElevation(enabled ? mAppBarHeight : 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private abstract static class BaseFragment extends PreferenceFragmentCompat implements AdapterView.OnItemLongClickListener {
        @Override
        public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
            super.setPreferenceScreen(preferenceScreen);
            if (preferenceScreen != null) {
                int count = preferenceScreen.getPreferenceCount();
                for (int i = 0; i < count; i++)
                    preferenceScreen.getPreference(i).setIconSpaceReserved(false);
            }
        }

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

        private Preference mDeveloperOptions;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.zim_preferences);
            if (!Utilities.getZimPrefs(getActivity()).getDeveloperOptionsEnabled()) {
                mDeveloperOptions = getPreferenceScreen().findPreference("pref_key__developer_options");
                getPreferenceScreen().removePreference(mDeveloperOptions);
            }
        }

        @Override
        public void setDivider(Drawable divider) {
            super.setDivider(null);
        }

        @Override
        public void setDividerHeight(int height) {
            super.setDividerHeight(0);
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            RecyclerView recyclerView = (RecyclerView) inflater
                    .inflate(R.layout.settings_recycler_view, parent, false);

            recyclerView.setLayoutManager(onCreateLayoutManager());
            recyclerView.setAccessibilityDelegateCompat(
                    new PreferenceRecyclerViewAccessibilityDelegate(recyclerView));

            return recyclerView;
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().setTitle(R.string.settings_button_text);
            if (mDeveloperOptions != null &&
                    Utilities.getZimPrefs(getActivity()).getDeveloperOptionsEnabled()) {
                getPreferenceScreen().addPreference(mDeveloperOptions);
                mDeveloperOptions = null;
            }
        }
    }

    public static class SubSettingsFragment extends BaseFragment implements
            Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        static final String TITLE = "title";
        static final String CONTENT_RES_ID = "content_res_id";

        private SystemDisplayRotationLockObserver mRotationLockObserver;
        private IconBadgingObserver mIconBadgingObserver;

        //private CustomIconPreference mIconPackPref;
        private Context mContext;

        public static SubSettingsFragment newInstance(SubPreference preference) {
            SubSettingsFragment fragment = new SubSettingsFragment();
            Bundle b = new Bundle(2);
            b.putString(TITLE, (String) preference.getTitle());
            b.putInt(CONTENT_RES_ID, preference.getContent());
            fragment.setArguments(b);
            return fragment;
        }

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
                    break;

                case R.xml.zim_preferences_theme:
                    Preference iconShapeOverride = findPreference(IconShapeOverride.KEY_PREFERENCE);
                    if (iconShapeOverride != null) {
                        if (IconShapeOverride.isSupported(getActivity())) {
                            IconShapeOverride.handlePreferenceUi((ListPreference) iconShapeOverride);
                        } else {
                            getPreferenceScreen().removePreference(iconShapeOverride);
                        }
                    }

                    //mIconPackPref = findPreference(ICON_PACK_PREF);
                    //mIconPackPref.setOnPreferenceChangeListener(this);
                    break;

                case R.xml.zim_preferences_app_drawer:
                    SwitchPreference showPrediction = findPreference(APPDRAWER_SHOW_PREDICTIONS);
                    showPrediction.setOnPreferenceChangeListener(this);
                    if (showPrediction.isChecked()) {
                        findPreference(APPDRAWER_SORT_MODE).setEnabled(false);
                    } else {
                        findPreference(APPDRAWER_SORT_MODE).setEnabled(true);
                    }

                    break;
                case R.xml.zim_preferences_dev_options:
                    findPreference("kill").setOnPreferenceClickListener(this);
                    break;

                case R.xml.zim_preferences_behavior:
                    //findPreference(ENABLE_MINUS_ONE_PREF).setTitle(getDisplayGoogleTitle());
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
                case R.xml.zim_preferences_notification:
                    ButtonPreference iconBadgingPref =
                            findPreference(ICON_BADGING_PREFERENCE_KEY);
                    if (!getResources().getBoolean(R.bool.notification_badging_enabled)) {
                        getPreferenceScreen().removePreference(iconBadgingPref);
                    } else {
                        // Listen to system notification badge settings while this UI is active.
                        mIconBadgingObserver = new IconBadgingObserver(
                                iconBadgingPref, resolver, getFragmentManager());
                        mIconBadgingObserver.register(NOTIFICATION_BADGING, NOTIFICATION_ENABLED_LISTENERS);
                    }
                    break;
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(getContent());
        }

        private String getDisplayGoogleTitle() {
            CharSequence charSequence = null;
            /*try {
                //Resources resourcesForApplication = mContext.getPackageManager().getResourcesForApplication("com.google.android.googlequicksearchbox");
                /*int identifier = resourcesForApplication.getIdentifier("title_google_home_screen", "string", "com.google.android.googlequicksearchbox");
                if (identifier != 0) {
                    charSequence = resourcesForApplication.getString(identifier);
                }
            } catch (PackageManager.NameNotFoundException ex) {
            }
            */
            if (TextUtils.isEmpty(charSequence)) {
                charSequence = mContext.getString(R.string.title_google_app);
            }
            return mContext.getString(R.string.title_show_google_app, charSequence);
        }

        private int getContent() {
            return getArguments().getInt(CONTENT_RES_ID);
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().setTitle(getArguments().getString(TITLE));

            //if (mIconPackPref != null)
            //    mIconPackPref.reloadIconPacks();
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
            } else {
                super.onDisplayPreferenceDialog(preference);
                return;
            }
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case ICON_PACK_PREF:
                    ProgressDialog.show(mContext,
                            null,
                            mContext.getString(R.string.state_loading),
                            true,
                            false);

                    new LooperExecutor(LauncherModel.getWorkerLooper()).execute(() -> {
                        // Clear the icon cache.
                        LauncherAppState.getInstance(mContext).getIconCache().clear();

                        // Wait for it
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.e("SettingsActivity", "Error waiting", e);
                        }

                        restartLauncher(mContext);
                    });
                    return true;
                case APPDRAWER_SHOW_PREDICTIONS:
                    if ((boolean) newValue) {
                        findPreference(APPDRAWER_SORT_MODE).setEnabled(false);
                        return true;
                    } else findPreference(APPDRAWER_SORT_MODE).setEnabled(true);
                    SuggestionConfirmationFragment confirmationFragment = new SuggestionConfirmationFragment();
                    confirmationFragment.setTargetFragment(this, 0);
                    confirmationFragment.show(getFragmentManager(), preference.getKey());
                    break;
            }
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (SMARTSPACE_PREF.equals(preference.getKey())) {
                //SmartspaceController.get(mContext).cZ();
                return true;
            } else if ("kill".equals(preference.getKey())) {
                Utilities.killLauncher();
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey() != null) {
                switch (preference.getKey()) {

                    case "pref_key__minibar":
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

                        return super.onPreferenceTreeClick(preference);
                }
            }
            return false;
        }
    }

    public static class SuggestionConfirmationFragment extends DialogFragment implements DialogInterface.OnClickListener {
        public void onClick(final DialogInterface dialogInterface, final int n) {
            if (getTargetFragment() instanceof PreferenceFragmentCompat) {
                Preference preference = ((PreferenceFragmentCompat) getTargetFragment()).findPreference(APPDRAWER_SHOW_PREDICTIONS);
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
     * Content observer which listens for system badging setting changes,
     * and updates the launcher badging setting subtext accordingly.
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
            mBadgingPref.setOnPreferenceClickListener(serviceEnabled && Utilities.ATLEAST_OREO ? null : this);
            mBadgingPref.setSummary(summary);

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (!Utilities.ATLEAST_OREO && serviceEnabled) {
                ComponentName cn = new ComponentName(preference.getContext(), NotificationListener.class);
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(":settings:fragment_args_key", cn.flattenToString());
                preference.getContext().startActivity(intent);
            } else {
                new SettingsActivity.NotificationAccessConfirmation().show(mFragmentManager, "notification_access");
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
        public void onClick(DialogInterface dialogInterface, int i) {
            ComponentName cn = new ComponentName(getActivity(), NotificationListener.class);
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(":settings:fragment_args_key", cn.flattenToString());
            getActivity().startActivity(intent);
        }
    }
}