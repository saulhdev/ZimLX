/*
 * Copyright (C) 2019 Paranoid Android
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
package com.aosp.launcher;

import android.animation.AnimatorSet;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.Utilities;
import com.aosp.launcher.qsb.QsbAnimationController;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

import org.zimmob.zimlx.settings.SettingsActivity;
import org.zimmob.zimlx.smartspace.FeedBridge;

public class AospLauncher extends Launcher {
    public LauncherClient mClient;
    public QsbAnimationController mQsbAnimationController;

    public AospLauncher() {
        setLauncherCallbacks(new AospLauncherCallbacks(this));
    }

    public LauncherCallbacks getLauncherCallbacks() {
        return mLauncherCallbacks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = Utilities.getPrefs(this);
        if (!FeedBridge.Companion.getInstance(this).isInstalled()) {
            prefs.edit().putBoolean(SettingsActivity.ENABLE_MINUS_ONE_PREF, false).apply();
        }
    }

    @Nullable
    public LauncherClient getGoogleNow() {
        return mClient;
    }

    public void playQsbAnimation() {
        mQsbAnimationController.playQsbAnimation();
    }

    public AnimatorSet openQsb() {
        return mQsbAnimationController.openQsb();
    }

}