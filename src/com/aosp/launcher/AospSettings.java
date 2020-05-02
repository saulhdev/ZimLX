package com.aosp.launcher;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.launcher3.settings.SettingsActivity;

import com.aosp.launcher.customization.IconDatabase;
import com.aosp.launcher.settings.IconPackPrefSetter;
import com.aosp.launcher.settings.ReloadingListPreference;
import com.aosp.launcher.util.AppReloader;

public class AospSettings extends SettingsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public interface OnResumePreferenceCallback {
        void onResume();
    }

    public static class AospSettingsFragment extends LauncherSettingsFragment {
        private static final String KEY_ICON_PACK = "pref_icon_pack";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            final Context context = getActivity();
            ReloadingListPreference icons = (ReloadingListPreference) findPreference(KEY_ICON_PACK);
            icons.setOnReloadListener(new IconPackPrefSetter(context));
            icons.setOnPreferenceChangeListener((pref, val) -> {
                IconDatabase.clearAll(context);
                IconDatabase.setGlobal(context, (String) val);
                AppReloader.get(context).reload();
                return true;
            });
        }
    }
}
