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
package org.zimmob.zimlx.util;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {
    private static final String TAG = "Config";
    //APP DRAWER SORT MODE
    public static final int SORT_AZ = 0;
    public static final int SORT_ZA = 1;
    public static final int SORT_LAST_INSTALLED = 2;
    public static final int SORT_MOST_USED = 3;
    public static final int SORT_BY_COLOR = 4;

    public Context context;
    private static final Object sInstanceLock = new Object();
    private static Config sInstance;
    @NotNull
    public final Set<String> minibarItems = new HashSet<>();
    String[] items = {"10", "11", "12", "13", "14", "15", "16", "17", "18"};

    public Config(Context context) {
        this.context = context;
        minibarItems.addAll(Arrays.asList(items));
    }

    @NotNull
    public static Config getInstance(@NotNull Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new Config(context.getApplicationContext());
            }
            return sInstance;
        }
    }
}
