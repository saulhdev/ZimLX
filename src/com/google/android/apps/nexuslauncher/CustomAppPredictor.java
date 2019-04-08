package com.google.android.apps.nexuslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import com.android.launcher3.AppFilter;
import com.android.launcher3.AppInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ComponentKeyMapper;

import org.zimmob.zimlx.settings.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomAppPredictor extends UserEventDispatcher implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static int MAX_PREDICTIONS = 10;
    private static final int BOOST_ON_OPEN = 9;
    private static final String PREDICTION_SET = "pref_prediction_set";
    private static final String PREDICTION_PREFIX = "pref_prediction_count_";
    private static final Set<String> EMPTY_SET = new HashSet<>();
    private final static String[] PLACE_HOLDERS = new String[]{
            "com.google.android.apps.photos",
            "com.google.android.apps.maps",
            "com.google.android.gm",
            "com.google.android.deskclock",
            "com.android.settings",
            "com.whatsapp",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.google.android.youtube",
            "com.yodo1.crossyroad",
            "com.spotify.music",
            "com.android.chrome",
            "com.instagram.android",
            "com.skype.raider",
            "com.snapchat.android",
            "com.viber.voip",
            "com.twitter.android",
            "com.android.phone",
            "com.google.android.music",
            "com.google.android.calendar",
            "com.google.android.apps.genie.geniewidget",
            "com.netflix.mediaclient",
            "bbc.iplayer.android",
            "com.google.android.videos",
            "com.amazon.mShop.android.shopping",
            "com.microsoft.office.word",
            "com.google.android.apps.docs",
            "com.google.android.keep",
            "com.google.android.apps.plus",
            "com.google.android.talk"
    };
    private final Context mContext;
    private final AppFilter mAppFilter;
    private final SharedPreferences mPrefs;
    private final PackageManager mPackageManager;

    public CustomAppPredictor(Context context) {
        mContext = context;
        mAppFilter = AppFilter.newInstance(mContext);
        mPrefs = Utilities.getPrefs(context);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        mPackageManager = context.getPackageManager();
        MAX_PREDICTIONS = Integer.valueOf(Utilities.getZimPrefs(context).getNumPredictedApps());
    }

    List<ComponentKeyMapper<AppInfo>> getPredictions() {
        List<ComponentKeyMapper<AppInfo>> list = new ArrayList<>();
        if (isPredictorEnabled()) {
            clearNonExistentPackages();

            List<String> predictionList = new ArrayList<>(getStringSetCopy());

            Collections.sort(predictionList, (o1, o2) -> Integer.compare(getLaunchCount(o2), getLaunchCount(o1)));

            for (String prediction : predictionList) {
                list.add(getComponentFromString(prediction));
            }

            if (list.size() < MAX_PREDICTIONS) {
                for (String placeHolder : PLACE_HOLDERS) {
                    Intent intent = mPackageManager.getLaunchIntentForPackage(placeHolder);
                    if (intent != null) {
                        ComponentName componentInfo = intent.getComponent();
                        if (componentInfo != null) {
                            ComponentKey key = new ComponentKey(componentInfo, Process.myUserHandle());
                            if (!predictionList.contains(key.toString())) {
                                list.add(new ComponentKeyMapper<>(key));
                            }
                        }
                    }
                }
            }

            if (list.size() > MAX_PREDICTIONS) {
                list = list.subList(0, MAX_PREDICTIONS);
            }
        }
        return list;
    }

    public void logAppLaunch(View v, Intent intent, UserHandle user) {
        super.logAppLaunch(v, intent);
        if (isPredictorEnabled() && recursiveIsDrawer(v)) {
            ComponentName componentInfo = intent.getComponent();
            if (componentInfo != null && mAppFilter.shouldShowApp(componentInfo)) {
                clearNonExistentPackages();

                Set<String> predictionSet = getStringSetCopy();
                SharedPreferences.Editor edit = mPrefs.edit();

                String prediction = new ComponentKey(componentInfo, user).toString();
                if (predictionSet.contains(prediction)) {
                    edit.putInt(PREDICTION_PREFIX + prediction, getLaunchCount(prediction) + BOOST_ON_OPEN);
                } else if (predictionSet.size() < MAX_PREDICTIONS || decayHasSpotFree(predictionSet, edit)) {
                    predictionSet.add(prediction);
                }

                edit.putStringSet(PREDICTION_SET, predictionSet);
                edit.apply();
            }
        }
    }

    private boolean decayHasSpotFree(Set<String> toDecay, SharedPreferences.Editor edit) {
        boolean spotFree = false;
        Set<String> toRemove = new HashSet<>();
        for (String prediction : toDecay) {
            int launchCount = getLaunchCount(prediction);
            if (launchCount > 0) {
                edit.putInt(PREDICTION_PREFIX + prediction, --launchCount);
            } else if (!spotFree) {
                edit.remove(PREDICTION_PREFIX + prediction);
                toRemove.add(prediction);
                spotFree = true;
            }
        }
        for (String prediction : toRemove) {
            toDecay.remove(prediction);
        }
        return spotFree;
    }

    /**
     * Zero-based launch count of a shortcut
     *
     * @param component serialized component
     * @return the number of launches, at least zero
     */
    private int getLaunchCount(String component) {
        return mPrefs.getInt(PREDICTION_PREFIX + component, 0);
    }

    private boolean recursiveIsDrawer(View v) {
        if (v != null) {
            ViewParent parent = v.getParent();
            while (parent != null) {
                if (parent instanceof AllAppsContainerView) {
                    return true;
                }
                parent = parent.getParent();
            }
        }
        return false;
    }

    private boolean isPredictorEnabled() {
        return Utilities.getPrefs(mContext).getBoolean(SettingsActivity.SHOW_PREDICTIONS_PREF, true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.SHOW_PREDICTIONS_PREF) && !isPredictorEnabled()) {
            Set<String> predictionSet = getStringSetCopy();

            SharedPreferences.Editor edit = mPrefs.edit();
            for (String prediction : predictionSet) {
                Log.i("Predictor", "Clearing " + prediction + " at " + getLaunchCount(prediction));
                edit.remove(PREDICTION_PREFIX + prediction);
            }
            edit.putStringSet(PREDICTION_SET, EMPTY_SET);
            edit.apply();
        }
    }

    private ComponentKeyMapper<AppInfo> getComponentFromString(String str) {
        return new ComponentKeyMapper<>(new ComponentKey(mContext, str));
    }

    private void clearNonExistentPackages() {
        Set<String> originalSet = mPrefs.getStringSet(PREDICTION_SET, EMPTY_SET);
        Set<String> predictionSet = new HashSet<>(originalSet);

        SharedPreferences.Editor edit = mPrefs.edit();
        for (String prediction : originalSet) {
            try {
                mPackageManager.getPackageInfo(new ComponentKey(mContext, prediction).componentName.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                predictionSet.remove(prediction);
                edit.remove(PREDICTION_PREFIX + prediction);
            }
        }

        edit.putStringSet(PREDICTION_SET, predictionSet);
        edit.apply();
    }

    private Set<String> getStringSetCopy() {
        Set<String> set = new HashSet<>();
        set.addAll(mPrefs.getStringSet(PREDICTION_SET, EMPTY_SET));
        return set;
    }
}
