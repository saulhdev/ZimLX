package org.zimmob.zimlx.preferences

import android.content.Context
import android.util.AttributeSet
import org.zimmob.zimlx.settings.ui.ControlledPreference
import androidx.preference.Preference as SupportPreference

class Preference(context: Context, attrs: AttributeSet?) : SupportPreference(context, attrs),
        ControlledPreference by ControlledPreference.Delegate(context, attrs)

