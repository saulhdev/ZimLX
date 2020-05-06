package org.zimmob.zimlx.settings

import com.android.launcher3.Utilities
import org.zimmob.zimlx.ZimPreferences
import org.zimmob.zimlx.util.JavaField

class IconScale @JvmOverloads constructor(
        prefs: ZimPreferences,
        scaleKey: String,
        private val fallbackScaleKey: String? = null,
        landscapeScaleKey: String = "landscape${Utilities.upperCaseFirstLetter(scaleKey)}",
        private val landscapeFallbackScaleKey: String = "landscape${Utilities.upperCaseFirstLetter(fallbackScaleKey)}",
        private val targetObject: Any,
        private val onChangeListener: () -> Unit = prefs.restart) {

    var scale by JavaField<Float>(targetObject, scaleKey)
    val scaleOriginal by JavaField<Float>(targetObject, "${scaleKey}Original")

    var landscapeScale by JavaField<Float>(targetObject, landscapeScaleKey)
    val landscapeScaleOriginal by JavaField<Float>(targetObject, "${landscapeScaleKey}Original")

    val hasFallback = fallbackScaleKey != null

    private val onChange = {
        applyCustomization()
        onChangeListener.invoke()
    }

    var scalePref by prefs.FloatPref("pref_$scaleKey", if (hasFallback) -1f else 1f, onChange)

    init {
        applyCustomization()
    }

    private fun applyCustomization() {
        scale = fromPref(scalePref, scaleOriginal, fallbackScaleKey)
        landscapeScale = fromPref(scalePref, landscapeScaleOriginal, landscapeFallbackScaleKey)
    }

    fun fromPref(value: Float, default: Float, fallbackKey: String?): Float {
        if (value < 0 && hasFallback) {
            val fallback by JavaField<Float>(targetObject, fallbackKey!!)
            return fallback
        }
        return value * default
    }
}