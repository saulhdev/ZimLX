package org.zimmob.zimlx.allapps;

import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;

import org.zimmob.zimlx.AppInfo;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.discovery.suggestions.SuggestionCandidate;
import org.zimmob.zimlx.discovery.suggestions.SuggestionsDatabaseHelper;
import org.zimmob.zimlx.util.ComponentKey;
import org.zimmob.zimlx.util.ComponentKeyMapper;

import java.util.ArrayList;
import java.util.List;

public class PredictiveAppsProvider {
    public static final int MAX_SUGGESTIONS = 9;
    private static final String TAG = "PredictiveAppsProvider";

    private Context mContext;
    private SuggestionsDatabaseHelper mHelper;

    public PredictiveAppsProvider(Context context) {
        mContext = context;
        mHelper = SuggestionsDatabaseHelper.getInstance(context);
    }
    public void updateComponentCount(ComponentName component) {
        if (component == null) {
            Log.w(TAG, "Can not update component count because component is null!");
            return;
        }

        if (!Utilities.arePredictiveAppsEnabled(mContext)) {
            // Don't log when predictive apps are disabled
            return;
        }

        SuggestionCandidate candidate = mHelper.getCandidate(component.getPackageName(),
                component.getClassName());
        mHelper.increaseCounter(mContext, candidate);
    }
    public List<ComponentKeyMapper<AppInfo>> getPredictions() {
        List<SuggestionCandidate> candidates = mHelper.getSuggestionCandidates(mContext);
        List<ComponentKeyMapper<AppInfo>> keys = new ArrayList<>();
        UserHandle handle = Process.myUserHandle();

        for (SuggestionCandidate candidate : candidates) {
            ComponentName name = new ComponentName(candidate.getPackageName(),
                    candidate.getClassName());

            keys.add(new ComponentKeyMapper<>(new ComponentKey(name, handle)));
        }

        return keys;
    }
}