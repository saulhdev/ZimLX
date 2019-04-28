package org.zimmob.zimlx.colors;

import android.content.Context;

import com.android.launcher3.Utilities;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.ZimPreferences;

import java.util.Map;

public class ColorEngine implements ZimPreferences.OnPreferenceChangeListener {
    private ZimPreferences prefs;
    private Context mContext;
    private static ColorEngine sInstance;
    private Map<String, OnColorChangeListener> colorListeners;

    public ColorEngine(Context context) {
        mContext = context;
        prefs = Utilities.getZimPrefs(mContext);
    }

    public void addColorChangeListeners(OnColorChangeListener listener, String[] keys) {
        if (keys.length == 0) {
            throw new RuntimeException("At least one key is required");
        }
        for (String key : keys) {
            if (colorListeners.get(key) == null) {
                prefs.addOnPreferenceChangeListener(this, key);
            }
            colorListeners.put(key, listener);
            //val resolver by getOrCreateResolver(key)
            //       listener.onColorChange(key, resolver.resolveColor())

        }
    }

    public void removeColorChangeListeners(OnColorChangeListener listener, String[] keys) {
        if (keys.length == 0) {
            throw new RuntimeException("At least one key is required");
        }
        for (String key : keys) {
            colorListeners.remove(key);
            if (colorListeners.get(key) == null) {
                prefs.removeOnPreferenceChangeListener(key, this);
            }

        }
    }


    public interface OnColorChangeListener {
        void onColorChange(String resolver, int color);
    }

    @Override
    public void onValueChanged(@NotNull String key, @NotNull ZimPreferences prefs, boolean force) {

    }

    public static ColorEngine getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ColorEngine(context.getApplicationContext());
        }
        return sInstance;

    }

    public static class Resolvers {
        public static String ACCENT = "pref_accentColorResolver";
        public static String HOTSEAT_QSB_BG = "pref_hotseatQsbColorResolver";
        public static String ALLAPPS_QSB_BG = "pref_allappsQsbColorResolver";
    }
}
