/*
 * 2020 Zim Launcher
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
import android.util.TypedValue;

import com.android.launcher3.R;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {
    private static final String TAG = "Config";

    public static final String THEME_ICON_SHAPE = "pref_iconShape";

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

    public float getDefaultBlurStrength() {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.config_default_blur_strength, typedValue, true);
        return typedValue.getFloat();
    }

    public boolean defaultEnableBlur() {
        return context.getResources().getBoolean(R.bool.config_default_enable_blur);
    }

    public String getDefaultSearchProvider() {
        return context.getResources().getString(R.string.config_default_search_provider);
    }

    public String[] getDefaultIconPacks() {
        String[] iconPacks = context.getResources().getStringArray(R.array.config_default_icon_packs);

        return iconPacks;
    }

    public boolean enableColorizedLegacyTreatment() {
        return context.getResources().getBoolean(R.bool.config_enable_colorized_legacy_treatment);
    }

    public boolean enableWhiteOnlyTreatment() {
        return context.getResources().getBoolean(R.bool.config_enable_white_only_treatment);
    }

    public boolean enableLegacyTreatment() {
        return context.getResources().getBoolean(R.bool.config_enable_legacy_treatment);
    }
}
