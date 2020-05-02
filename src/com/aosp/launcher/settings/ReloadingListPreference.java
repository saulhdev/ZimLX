package com.aosp.launcher.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

import com.aosp.launcher.AospSettings;

public class ReloadingListPreference extends ListPreference
        implements AospSettings.OnResumePreferenceCallback {
    private OnReloadListener mOnReloadListener;

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReloadingListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        loadEntries();
        super.onClick();
    }

    public void setOnReloadListener(OnReloadListener onReloadListener) {
        mOnReloadListener = onReloadListener;
        loadEntries();

    }

    @Override
    public void onResume() {
        loadEntries();
    }

    private void loadEntries() {
        if (mOnReloadListener != null) {
            mOnReloadListener.updateList(this);
        }
    }

    public interface OnReloadListener {
        void updateList(ListPreference pref);
    }
}
