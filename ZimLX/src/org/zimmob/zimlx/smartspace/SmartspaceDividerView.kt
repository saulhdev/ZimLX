package org.zimmob.zimlx.smartspace

import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.zimmob.zimlx.zimPrefs

class SmartspaceDividerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    init {
        setBackgroundColor(context.zimPrefs.accentColor)
    }
}
