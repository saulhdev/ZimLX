package org.zimmob.zimlx.allapps.theme

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import org.zimmob.zimlx.R
import org.zimmob.zimlx.Utilities
import org.zimmob.zimlx.blur.BlurWallpaperProvider
import org.zimmob.zimlx.config.FeatureFlags
import org.zimmob.zimlx.dynamicui.ExtractedColors

open class AllAppsBaseTheme(val context: Context) : IAllAppsThemer {
    override val backgroundColor = Utilities
            .resolveAttributeData(FeatureFlags.applyDarkTheme(context, FeatureFlags.DARK_ALLAPPS), R.attr.allAppsContainerColor)
    override val backgroundColorBlur = Utilities
            .resolveAttributeData(FeatureFlags.applyDarkTheme(context, FeatureFlags.DARK_BLUR), R.attr.allAppsContainerColorBlur)

    override fun iconTextColor(backgroundAlpha: Int): Int {
        if (Utilities.getPrefs(context).useCustomAllAppsTextColor) {
            return Utilities.getPrefs(context).allAppsLabelColor
        } else if (FeatureFlags.getUseDarkTheme(FeatureFlags.DARK_ALLAPPS)) {
            return Color.WHITE
        } else if (backgroundAlpha < 128 && !BlurWallpaperProvider.isEnabled(BlurWallpaperProvider.BLUR_ALLAPPS) || backgroundAlpha < 50) {
            return Color.WHITE
        } else {
            return ContextCompat.getColor(context, R.color.quantum_panel_text_color)
        }
    }

    override val iconTextLines = 1
    override val searchTextColor = 0
    override val searchBarHintTextColor = Utilities.getDynamicAccent(context)
    override val fastScrollerHandleColor = Utilities.getDynamicAccent(context)
    override val fastScrollerPopupTintColor: Int
        get() {
            if (Utilities.getPrefs(context).enableDynamicUi) {
                val tint = Utilities.getDynamicAccent(context)
                if (tint != -1) {
                    return tint
                }
            }
            return 0
        }
    override val fastScrollerPopupTextColor: Int
        get() {
            var color = Color.WHITE
            if (Utilities.getPrefs(context).enableDynamicUi) {
                val tint = Utilities.getDynamicAccent(context)
                if (tint != -1) {
                    color = Utilities.getColor(context, ExtractedColors.VIBRANT_FOREGROUND_INDEX, Color.WHITE)
                }
            }
            return color
        }

    override val iconLayout = R.layout.all_apps_icon
    override fun numIconPerRow(default: Int) = default
    override fun iconHeight(default: Int) = default

}