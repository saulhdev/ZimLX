package org.zimmob.zimlx.settings;

import android.content.Context;

import com.android.launcher3.AppObject;
import com.android.launcher3.R;

import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

import org.zimmob.zimlx.minibar.Minibar;

import java.util.ArrayList;
import java.util.Arrays;

public class AppSettings extends SharedPreferencesPropertyBackend {
    private Context mContext;

    public AppSettings(Context context) {
        super(context);
        mContext = context;
    }

    public static AppSettings get() {
        return new AppSettings(AppObject.get());
    }

    public ArrayList<String> getMinibarArrangement() {
        ArrayList<String> ret = getStringList(R.string.pref_key__minibar_items);
        if (ret.isEmpty()) {
            for (Minibar.ActionDisplayItem item : Minibar.actionDisplayItems) {
                if (Arrays.asList(98, 36, 50, 71, 25, 46, 54, 73).contains(item.id)) {
                    ret.add(Integer.toString(item.id));
                }
            }
            setMinibarArrangement(ret);
        }
        return ret;
    }

    public void setMinibarArrangement(ArrayList<String> value) {
        setStringList(R.string.pref_key__minibar_items, value);
    }

    // internal preferences below here
    public boolean getMinibarEnable() {
        return getBool(R.string.pref_key__minibar_enable, true);
    }

    public void setMinibarEnable(boolean value) {
        setBool(R.string.pref_key__minibar_enable, value);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    public boolean isGestureFeedback() {
        return getBool(R.string.pref_key__gesture_feedback, false);
    }

    public boolean getGestureDockSwipeUp() {
        return getBool(R.string.pref_key__gesture_quick_swipe, true);
    }

    public int getGestureDoubleTap() {
        return getIntOfStringPref(R.string.pref_key__gesture_double_tap, 0);
    }

    public int getGestureSwipeUp() {
        return getIntOfStringPref(R.string.pref_key__gesture_swipe_up, 8);
    }

    public int getGestureSwipeDown() {
        return getIntOfStringPref(R.string.pref_key__gesture_swipe_down, 10);
    }

    public int getGesturePinch() {
        return getIntOfStringPref(R.string.pref_key__gesture_pinch, 0);
    }

    public int getGestureUnpinch() {
        return getIntOfStringPref(R.string.pref_key__gesture_unpinch, 0);
    }

}

