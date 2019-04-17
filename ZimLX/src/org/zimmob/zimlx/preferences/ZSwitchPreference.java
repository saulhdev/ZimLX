package org.zimmob.zimlx.preferences;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;

import org.zimmob.zimlx.color.ColorEngine;

public class ZSwitchPreference extends SwitchPreference implements ColorEngine.OnColorChangeListener {

    private View checkableView = null;
    private Context mContext;

    public ZSwitchPreference(Context context) {
        super(context);
        mContext = context;
    }

    public ZSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public ZSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public ZSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        //checkableView = view.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        //ColorEngine.getInstance(mContext).addColorChangeListeners(this, ColorEngine.Resolvers.ACCENT)
    }

    @Override
    public void onColorChange(String resolver, int color, int foregroundColor) {
        //if (resolver == ColorEngine.Resolvers.ACCENT && checkableView instanceof Switch) {

        //    (checkableView instanceof Switch).applyColor(color);
        //}
    }

    public void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        //ColorEngine.getInstance(mContext).removeColorChangeListeners(this, ColorEngine.Resolvers.ACCENT);
    }
}
