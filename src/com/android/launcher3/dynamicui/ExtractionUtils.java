/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.launcher3.dynamicui;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import com.android.launcher3.Utilities;

import java.util.List;

/**
 * Contains helper fields and methods related to extracting colors from the wallpaper.
 */
public class ExtractionUtils {
    public static final String EXTRACTED_COLORS_PREFERENCE_KEY = "pref_extractedColors";
    public static final String WALLPAPER_ID_PREFERENCE_KEY = "pref_wallpaperId";

    private static final float MIN_CONTRAST_RATIO = 2f;

    private static boolean hasWallpaperIdChanged(Context context) {
        if (!Utilities.ATLEAST_NOUGAT) {
            // TODO: update an id in sharedprefs in onWallpaperChanged broadcast, and read it here.
            return false;
        }
        final SharedPreferences sharedPrefs = Utilities.getPrefs(context);
        int wallpaperId = getWallpaperId(WallpaperManager.getInstance(context));
        int savedWallpaperId = sharedPrefs.getInt(ExtractionUtils.WALLPAPER_ID_PREFERENCE_KEY, -1);
        return wallpaperId != savedWallpaperId;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static int getWallpaperId(WallpaperManager wallpaperManager) {
        return Utilities.ATLEAST_NOUGAT ?
                wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM) : -1;
    }

    public static boolean isSuperLight(Palette p) {
        return !isLegibleOnWallpaper(Color.WHITE, p.getSwatches());
    }

    public static boolean isSuperDark(Palette p) {
        return !isLegibleOnWallpaper(Color.BLACK, p.getSwatches());
    }

    /**
     * Given a color, returns true if that color is legible on
     * the given wallpaper color swatches, else returns false.
     */
    private static boolean isLegibleOnWallpaper(int color, List<Palette.Swatch> wallpaperSwatches) {
        int legiblePopulation = 0;
        int illegiblePopulation = 0;
        for (Palette.Swatch swatch : wallpaperSwatches) {
            if (isLegible(color, swatch.getRgb())) {
                legiblePopulation += swatch.getPopulation();
            } else {
                illegiblePopulation += swatch.getPopulation();
            }
        }
        return legiblePopulation > illegiblePopulation;
    }

    /**
     * @return Whether the foreground color is legible on the background color.
     */
    private static boolean isLegible(int foreground, int background) {
        background = ColorUtils.setAlphaComponent(background, 255);
        return ColorUtils.calculateContrast(foreground, background) >= MIN_CONTRAST_RATIO;
    }

}
