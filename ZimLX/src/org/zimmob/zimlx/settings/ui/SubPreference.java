package org.zimmob.zimlx.settings.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.R;

import androidx.preference.Preference;

public class SubPreference extends Preference implements View.OnLongClickListener {

    private int mContent;
    private int mLongClickContent;
    private boolean mLongClick;

    private boolean mHasPreview = false;

    public SubPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SubPreference);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SubPreference_content) {
                mContent = a.getResourceId(attr, 0);
            } else if (attr == R.styleable.SubPreference_longClickContent) {
                mLongClickContent = a.getResourceId(attr, 0);
            }
        }
        a.recycle();
        setFragment(SettingsActivity.SubSettingsFragment.class.getName());
    }

    @Override
    public Bundle getExtras() {
        Bundle b = new Bundle(2);
        b.putString(SettingsActivity.SubSettingsFragment.TITLE, (String) getTitle());
        b.putInt(SettingsActivity.SubSettingsFragment.CONTENT_RES_ID, getContent());
        return b;
    }

    public int getContent() {
        return mLongClick ? mLongClickContent : mContent;
    }

    @Override
    protected void onClick() {
        mLongClick = false;
        super.onClick();
    }

    @Override
    public boolean onLongClick(View view) {
        if (mLongClickContent != 0) {
            mLongClick = true;
            super.onClick();
            return true;
        } else {
            return false;
        }
    }

    public Boolean hasPreview() {
        return mHasPreview;
    }

    public void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(SettingsActivity.SubSettingsFragment.TITLE, getTitle());
        intent.putExtra(SettingsActivity.SubSettingsFragment.CONTENT_RES_ID, getContent());
        intent.putExtra(SettingsActivity.SubSettingsFragment.HAS_PREVIEW, hasPreview());
        context.startActivity(intent);
    }
}