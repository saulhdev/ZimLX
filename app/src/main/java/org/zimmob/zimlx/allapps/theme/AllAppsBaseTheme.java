package org.zimmob.zimlx.allapps.theme;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.blur.BlurWallpaperProvider;
import org.zimmob.zimlx.config.FeatureFlags;
import org.zimmob.zimlx.dynamicui.ExtractedColors;

public class AllAppsBaseTheme implements IAllAppsThemer {
    private Context mContext;

    public AllAppsBaseTheme(Context context) {
        mContext = context;
    }

    @Override
    public int getBackgroundColor() {
        int backgroundColor = Utilities.resolveAttributeData(FeatureFlags.applyDarkTheme(mContext, FeatureFlags.DARK_ALLAPPS), R.attr.allAppsContainerColor);
        return backgroundColor;
    }

    @Override
    public int getBackgroundColorBlur() {
        int backgroundColorBlur = Utilities
                .resolveAttributeData(FeatureFlags.applyDarkTheme(mContext, FeatureFlags.DARK_BLUR), R.attr.allAppsContainerColorBlur);
        return backgroundColorBlur;
    }

    @Override
    public int iconTextColor(int backgroundAlpha) {
        if (Utilities.getPrefs(mContext).getUseCustomAllAppsTextColor()) {
            return Utilities.getPrefs(mContext).getAllAppsLabelColor();
        } else if (FeatureFlags.getUseDarkTheme(FeatureFlags.DARK_ALLAPPS)) {
            return Color.WHITE;
        } else if (backgroundAlpha < 128 && !BlurWallpaperProvider.Companion.isEnabled(BlurWallpaperProvider.BLUR_ALLAPPS) || backgroundAlpha < 50) {
            return Color.WHITE;
        } else {
            return ContextCompat.getColor(mContext, R.color.quantum_panel_text_color);
        }
    }

    @Override
    public int getIconTextLines() {
        return 1;
    }

    @Override
    public int getSearchTextColor() {
        return 0;
    }

    @Override
    public int getSearchBarHintTextColor() {
        int searchBarHintTextColor = Utilities.getDynamicAccent(mContext);
        return searchBarHintTextColor;
    }

    @Override
    public int getFastScrollerHandleColor() {
        int fastScrollerHandleColor = Utilities.getDynamicAccent(mContext);
        return fastScrollerHandleColor;
    }

    @Override
    public int getFastScrollerPopupTintColor() {
        if (Utilities.getPrefs(mContext).getEnableDynamicUi()) {
            int tint = Utilities.getDynamicAccent(mContext);
            if (tint != -1) {
                return tint;
            }
        }
        return 0;
    }

    @Override
    public int getFastScrollerPopupTextColor() {
        int color = Color.WHITE;
        if (Utilities.getPrefs(mContext).getEnableDynamicUi()) {
            int tint = Utilities.getDynamicAccent(mContext);
            if (tint != -1) {
                color = Utilities.getColor(mContext, ExtractedColors.VIBRANT_FOREGROUND_INDEX, Color.WHITE);
            }
        }
        return color;
    }

    @Override
    public int getIconLayout() {
        return 0;
    }

    @Override
    public int numIconPerRow(int i) {
        return i;
    }

    @Override
    public int iconHeight(int i) {
        return i;
    }
}
