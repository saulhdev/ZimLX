package org.zimmob.zimlx.viewutil;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.SparseArray;

import org.zimmob.zimlx.config.FeatureFlags;
import org.zimmob.zimlx.util.Utilities;

import java.util.ArrayList;

public class WidgetHost extends AppWidgetHost {
    public static final int APPWIDGET_HOST_ID = 1024;
    private final ArrayList<ProviderChangedListener> mProviderChangeListeners = new ArrayList<>();
    private final SparseArray<WidgetHost> mViews = new SparseArray<>();
    private final Context mContext;

    public WidgetHost(Context context, int hostId) {
        super(context, APPWIDGET_HOST_ID);
        mContext = context;
    }


    @Override
    public void startListening() {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return;
        }

        try {
            super.startListening();
        } catch (Exception e) {
            if (!Utilities.isBinderSizeError(e)) {
                throw new RuntimeException(e);
            }
            // We're willing to let this slide. The exception is being caused by the list of
            // RemoteViews which is being passed back. The startListening relationship will
            // have been established by this point, and we will end up populating the
            // widgets upon bind anyway. See issue 14255011 for more context.
        }
    }

    @Override
    public void stopListening() {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return;
        }

        super.stopListening();
    }


    @Override
    public int allocateAppWidgetId() {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return AppWidgetManager.INVALID_APPWIDGET_ID;
        }

        return super.allocateAppWidgetId();
    }

    public void addProviderChangeListener(ProviderChangedListener callback) {
        mProviderChangeListeners.add(callback);
    }

    public void removeProviderChangeListener(ProviderChangedListener callback) {
        mProviderChangeListeners.remove(callback);
    }

    protected void onProvidersChanged() {
        if (!mProviderChangeListeners.isEmpty()) {
            for (ProviderChangedListener callback : new ArrayList<>(mProviderChangeListeners)) {
                callback.notifyWidgetProvidersChanged();
            }
        }
    }

    /**
     * Listener for getting notifications on provider changes.
     */
    public interface ProviderChangedListener {

        void notifyWidgetProvidersChanged();
    }
}
