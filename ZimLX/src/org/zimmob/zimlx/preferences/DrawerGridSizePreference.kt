package org.zimmob.zimlx.preferences

import android.content.Context
import android.util.AttributeSet
import com.android.launcher3.Utilities

class DrawerGridSizePreference(context: Context, attrs: AttributeSet?) : SingleDimensionGridSizePreference(context, attrs, Utilities.getZimPrefs(context).drawerGridSize)
class PredictionGridSizePreference(context: Context, attrs: AttributeSet?) : SingleDimensionGridSizePreference(context, attrs, Utilities.getZimPrefs(context).predictionGridSize)