package org.zimmob.zimlx.pixelify;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextClock;

import org.zimmob.zimlx.R;

public class DoubleShadowTextClock extends TextClock {
    private final float ambientShadowBlur;
    private final int ambientShadowColor;
    private final float keyShadowBlur;
    private final int keyShadowColor;
    private final float keyShadowOffset;

    public DoubleShadowTextClock(Context context) {
        this(context, null);
    }

    public DoubleShadowTextClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DoubleShadowTextClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ShadowInfo, i, 0);
        ambientShadowBlur = obtainStyledAttributes.getDimension(R.styleable.ShadowInfo_ambientShadowBlur, 0.0f);
        keyShadowBlur = obtainStyledAttributes.getDimension(R.styleable.ShadowInfo_keyShadowBlur, 0.0f);
        keyShadowOffset = obtainStyledAttributes.getDimension(R.styleable.ShadowInfo_keyShadowOffset, 0.0f);
        ambientShadowColor = obtainStyledAttributes.getColor(R.styleable.ShadowInfo_ambientShadowColor, 0);
        keyShadowColor = obtainStyledAttributes.getColor(R.styleable.ShadowInfo_keyShadowColor, 0);
        obtainStyledAttributes.recycle();
        setShadowLayer(Math.max(keyShadowBlur + keyShadowOffset, ambientShadowBlur), 0.0f, 0.0f, keyShadowColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setShadowLayer(keyShadowBlur, 0.0f, keyShadowOffset, keyShadowColor);
        super.onDraw(canvas);
        getPaint().setShadowLayer(ambientShadowBlur, 0.0f, 0.0f, ambientShadowColor);
        super.onDraw(canvas);
    }

    public void setFormat(CharSequence charSequence) {
        setFormat24Hour(charSequence);
        setFormat12Hour(charSequence);
    }
}