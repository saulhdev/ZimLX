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

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherFiles;

import org.zimmob.zimlx.util.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class ZimPreferencesHandler  {
    private SharedPreferences sharedPrefs;
    private Context mContext;
    public ZimPreferencesHandler(Context context){
        mContext = context;
        sharedPrefs = migratePrefs();

    }

    private SharedPreferences migratePrefs() {
        String dir = Launcher.mContext.getCacheDir().getParent();
        File oldFile = new File(dir, "shared_prefs/" + LauncherFiles.OLD_SHARED_PREFERENCES_KEY + ".xml");
        File newFile = new File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml");
        if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile);
            oldFile.delete();
        }
        return mContext.getApplicationContext().getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }


    public ArrayList<String> getMinibarItems() {
        Set<String> ret = sharedPrefs.getStringSet("pref_key__minibar_items", Config.getInstance(mContext).minibarItems);

        ArrayList<String> arrayList =new ArrayList<String>();
        for (String str : ret)
            arrayList.add(str);
        return arrayList;
    }

    public int getIntPref(String key, int defaultValue){
        return sharedPrefs.getInt(key, defaultValue);
    }
    public void setIntPref(String key, int value){
        sharedPrefs.edit().putInt(key, value).apply();
    }

    protected Boolean getBooleanPref(String key, Boolean value){
        return sharedPrefs.getBoolean(key, value);
    }
}
