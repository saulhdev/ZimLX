package org.zimmob.zimlx.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;

import org.zimmob.zimlx.R;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class ColorPreferenceCompat extends Preference implements ColorPickerDialogListener {

    private static final int SIZE_NORMAL = 0;
    private static final int SIZE_LARGE = 1;

    private OnShowDialogListener onShowDialogListener;
    private int color = Color.BLACK;
    private boolean showDialog;
    @ColorPickerDialog.DialogType
    private
    int dialogType;
    private int colorShape;
    private boolean allowPresets;
    private boolean allowCustom;
    private boolean showAlphaSlider;
    private boolean showColorShades;
    private int previewSize;
    private int[] presets;
    private int dialogTitle;

    public ColorPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ColorPreferenceCompat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setPersistent(true);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPreference);
        showDialog = a.getBoolean(R.styleable.ColorPreference_cpv_showDialog, true);
        //noinspection WrongConstant
        dialogType = a.getInt(R.styleable.ColorPreference_cpv_dialogType, ColorPickerDialog.TYPE_PRESETS);
        colorShape = a.getInt(R.styleable.ColorPreference_cpv_colorShape, ColorShape.CIRCLE);
        allowPresets = a.getBoolean(R.styleable.ColorPreference_cpv_allowPresets, true);
        allowCustom = a.getBoolean(R.styleable.ColorPreference_cpv_allowCustom, true);
        showAlphaSlider = a.getBoolean(R.styleable.ColorPreference_cpv_showAlphaSlider, false);
        showColorShades = a.getBoolean(R.styleable.ColorPreference_cpv_showColorShades, true);
        previewSize = a.getInt(R.styleable.ColorPreference_cpv_previewSize, SIZE_NORMAL);
        final int presetsResId = a.getResourceId(R.styleable.ColorPreference_cpv_colorPresets, 0);
        dialogTitle = a.getResourceId(R.styleable.ColorPreference_cpv_dialogTitle, R.string.cpv_default_title);
        if (presetsResId != 0) {
            presets = getContext().getResources().getIntArray(presetsResId);
        } else {
            presets = ColorPickerDialog.MATERIAL_COLORS;
        }
        if (colorShape == ColorShape.CIRCLE) {
            setWidgetLayoutResource(
                    previewSize == SIZE_LARGE ? R.layout.cpv_preference_circle_large : R.layout.cpv_preference_circle);
        } else {
            setWidgetLayoutResource(
                    previewSize == SIZE_LARGE ? R.layout.cpv_preference_square_large : R.layout.cpv_preference_square
            );
        }
        a.recycle();
    }

    public ColorPickerDialog getDialog() {
        return ColorPickerDialog.newBuilder()
                .setDialogType(dialogType)
                .setDialogTitle(dialogTitle)
                .setColorShape(colorShape)
                .setPresets(presets)
                .setAllowPresets(allowPresets)
                .setAllowCustom(allowCustom)
                .setShowAlphaSlider(showAlphaSlider)
                .setShowColorShades(showColorShades)
                .setColor(color)
                .create();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ColorPanelView preview = (ColorPanelView) holder.findViewById(R.id.cpv_preference_preview_color_panel);
        if (preview != null) {
            preview.setColor(color);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            color = getPersistedInt(0xFF000000);
        } else {
            color = (Integer) defaultValue;
            persistInt(color);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, Color.BLACK);
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        saveValue(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // no-op
    }

    /**
     * Set the new color
     *
     * @param color The newly selected color
     */
    public void saveValue(@ColorInt int color) {
        this.color = color;
        persistInt(this.color);
        notifyChanged();
        callChangeListener(color);
    }

    /**
     * Set the colors shown in the {@link ColorPickerDialog}.
     *
     * @param presets An array of color ints
     */
    public void setPresets(@NonNull int[] presets) {
        this.presets = presets;
    }

    /**
     * Get the colors that will be shown in the {@link ColorPickerDialog}.
     *
     * @return An array of color ints
     */
    public int[] getPresets() {
        return presets;
    }

    /**
     * The listener used for showing the {@link ColorPickerDialog}.
     * Call {@link #saveValue(int)} after the user chooses a color.
     * If this is set then it is up to you to show the dialog.
     *
     * @param listener The listener to show the dialog
     */
    public void setOnShowDialogListener(OnShowDialogListener listener) {
        onShowDialogListener = listener;
    }

    /**
     * The tag used for the {@link ColorPickerDialog}.
     *
     * @return The tag
     */
    public String getFragmentTag() {
        return "color_" + getKey();
    }

    interface OnShowDialogListener {

        void onShowColorPickerDialog(String title, int currentColor);
    }

}