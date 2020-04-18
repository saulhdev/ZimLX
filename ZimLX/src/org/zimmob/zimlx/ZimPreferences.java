/*
 * Copyright (C) 2020 Zim Launcher
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
package org.zimmob.zimlx;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.R;

import java.util.ArrayList;

public class ZimPreferences extends ZimPreferencesHandler implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static ZimPreferences sInstance;
    private static final Object sInstanceLock = new Object();
    public static ZimPreferences getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new ZimPreferences(context);
            }
            return sInstance;
        }
    }

    public ZimPreferences(Context context){
        super(context);
    }



    /*DESKTOP PREFERECENCE*/
    public int getDashColor() {
        return getIntPref("pref_key__minibar_color", R.color.colorPrimary);
    }
    public void setDashColor(String key, int color) {
        setIntPref(key, color);
    }

    public ArrayList<String> getDashItems() {
        ArrayList<String> arrayList = getMinibarItems();
        return arrayList;
    }

    /*THEMES*/
    public int getAccentColor() {
        return getIntPref("pref_key__accent_color", R.color.colorAccent);
    }

    /*DEV*/
    public Boolean getDeveloperOptionsEnabled(){
        return getBooleanPref("pref_developerOptionsEnabled", false);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }
}
