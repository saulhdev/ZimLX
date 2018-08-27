package org.zimmob.zimlx.settings;

import android.content.Context;

import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

import org.zimmob.zimlx.App;
import org.zimmob.zimlx.R;
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
        return new AppSettings(App.get());
    }

    public ArrayList<String> getMinibarArrangement() {
        ArrayList<String> ret = getStringList(R.string.pref_key__minibar_items);
        if (ret.isEmpty()) {
            for (Minibar.ActionDisplayItem item : Minibar.actionDisplayItems) {
                if (Arrays.asList(98, 36, 24, 50, 71, 25, 73).contains(item.id)) {
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
}

