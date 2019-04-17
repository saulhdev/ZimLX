package org.zimmob.zimlx.color;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.zimmob.zimlx.ZimPreferences;

public class ColorEngine implements ZimPreferences.OnPreferenceChangeListener {
    private static ColorEngine INSTANCE;
    private ColorResolvers accentResolver = new ColorResolvers();


    public ColorEngine(Context context) {
        INSTANCE = this;
    }

    public int getAccent() {
        return 0;//accentResolver.resolveColor();
    }

    public static ColorEngine getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ColorEngine(context);
        }
        return INSTANCE;
    }


    @Override
    public void onValueChanged(@NotNull String key, @NotNull ZimPreferences prefs, boolean force) {

    }

    public interface OnColorChangeListener {
        void onColorChange(String resolver, int color, int foregroundColor);
    }

    public abstract class ColorResolver {
        public abstract int resolveColor();
    }

}
