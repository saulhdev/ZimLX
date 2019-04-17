package org.zimmob.zimlx.settings;

import android.content.Context;

import com.android.launcher3.AppObject;

import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

import org.zimmob.zimlx.minibar.Minibar;
import org.zimmob.zimlx.util.ZimFlags;

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
        ArrayList<String> ret = getStringList(ZimFlags.MINIBAR_ITEMS);
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
        setStringList(ZimFlags.MINIBAR_ITEMS, value);
    }

    // internal preferences below here
    public boolean getMinibarEnable() {
        return getBool(ZimFlags.MINIBAR_ENABLE, true);
    }

    public void setMinibarEnable(boolean value) {
        setBool(ZimFlags.MINIBAR_ENABLE, value);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

}

