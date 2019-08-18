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
    //APP DRAWER SORT MODE
    public static final int SORT_AZ = 0;
    public static final int SORT_ZA = 1;
    public static final int SORT_LAST_INSTALLED = 2;
    public static final int SORT_MOST_USED = 3;

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

    public String getDefaultColorResolver() {
        return context.getString(R.string.config_default_color_resolver);
    }

    public String[] getDefaultIconPacks() {
        String[] iconPacks = context.getResources().getStringArray(R.array.config_default_icon_packs);

        return iconPacks;
    }

    public String getDefaultSearchProvider() {
        return context.getResources().getString(R.string.config_default_search_provider);
    }


    public boolean enableLegacyTreatment() {
        return context.getResources().getBoolean(R.bool.config_enable_legacy_treatment);
    }

    public boolean enableColorizedLegacyTreatment() {
        return context.getResources().getBoolean(R.bool.config_enable_colorized_legacy_treatment);
    }

    public boolean enableWhiteOnlyTreatment() {
        return context.getResources().getBoolean(R.bool.config_enable_white_only_treatment);
    }

    public boolean defaultEnableBlur() {
        return context.getResources().getBoolean(R.bool.config_default_enable_blur);
    }

    public float getDefaultBlurStrength() {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.config_default_blur_strength, typedValue, true);
        return typedValue.getFloat();
    }

    public boolean enableSmartspace() {
        return context.getResources().getBoolean(R.bool.config_enable_smartspace);
    }
}
