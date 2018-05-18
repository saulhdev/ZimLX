package org.zimmob.zimlx.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.interfaces.AppDeleteListener;
import org.zimmob.zimlx.interfaces.AppUpdateListener;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.IconLabelItem;
import org.zimmob.zimlx.model.Item;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class AppManager {
    private static AppManager _ref;

    /**
     * @param context
     * @return
     */
    public static AppManager getInstance(Context context) {
        return _ref == null ? (_ref = new AppManager(context)) : _ref;
    }

    private PackageManager _packageManager;
    private List<App> _apps = new ArrayList<>();
    private List<App> _nonFilteredApps = new ArrayList<>();
    public final List<AppUpdateListener> _updateListeners = new ArrayList<>();
    public final List<AppDeleteListener> _deleteListeners = new ArrayList<>();
    public boolean _recreateAfterGettingApps;
    private AsyncTask _task;
    private Context _context;

    public PackageManager getPackageManager() {
        return _packageManager;
    }

    public Context getContext() {
        return _context;
    }

    /**
     * @param c
     */
    public AppManager(Context c) {
        _context = c;
        _packageManager = c.getPackageManager();
    }

    public App findApp(Intent intent) {
        if (intent == null || intent.getComponent() == null) return null;

        String packageName = intent.getComponent().getPackageName();
        String className = intent.getComponent().getClassName();
        for (App app : _apps) {
            if (app.getClassName().equals(className) && app.getPackageName().equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    /**
     * @return
     */
    public List<App> getApps() {
        return _apps;
    }

    public List<App> getNonFilteredApps() {
        return _nonFilteredApps;
    }

    public void init() {
        getAllApps();
    }

    private void getAllApps() {
        if (_task == null || _task.getStatus() == AsyncTask.Status.FINISHED)
            _task = new AsyncGetApps().execute();
        else if (_task.getStatus() == AsyncTask.Status.RUNNING) {
            _task.cancel(false);
            _task = new AsyncGetApps().execute();
        }
    }

    public void startPickIconPackIntent(final Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.anddoes.launcher.THEME");

        FastItemAdapter<IconLabelItem> fastItemAdapter = new FastItemAdapter<>();

        final List<ResolveInfo> resolveInfos = _packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(_packageManager));
        final MaterialDialog d = new MaterialDialog.Builder(activity)
                .adapter(fastItemAdapter, null)
                .title((activity.getString(R.string.dialog__icon_pack_title)))
                .build();

        fastItemAdapter.add(new IconLabelItem(activity, R.drawable.ic_launcher, R.string.label_default, -1)
                .withIconGravity(Gravity.START)
                .withOnClickListener(v -> {
                    _recreateAfterGettingApps = true;
                    AppSettings.get().setIconPack("");
                    getAllApps();
                    d.dismiss();
                }));

        for (int i = 0; i < resolveInfos.size(); i++) {
            final int mI = i;
            fastItemAdapter.add(new IconLabelItem(activity, resolveInfos.get(i).loadIcon(_packageManager), resolveInfos.get(i).loadLabel(_packageManager).toString(), -1)
                    .withIconGravity(Gravity.START)
                    .withOnClickListener(v -> {
                        if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            _recreateAfterGettingApps = true;
                            AppSettings.get().setIconPack(resolveInfos.get(mI).activityInfo.packageName);
                            getAllApps();
                            d.dismiss();
                        } else {
                            Tool.toast(_context, (activity.getString(R.string.dialog__icon_pack_info_toast)));
                            ActivityCompat.requestPermissions(HomeActivity.Companion.getLauncher(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, HomeActivity.REQUEST_PERMISSION_STORAGE);
                        }
                    }));
        }
        d.show();
    }

    public void onReceive(Context p1, Intent p2) {
        getAllApps();
    }

    // -----------------------
    // AppLoader interface
    // -----------------------

    public List<App> getAllApps(Context context, boolean includeHidden) {
        return includeHidden ? getNonFilteredApps() : getApps();
    }

    public App findItemApp(Item item) {
        return findApp(item.getIntent());
    }

    public App createApp(Intent intent) {
        try {
            ResolveInfo info = _packageManager.resolveActivity(intent, 0);
            App app = new App(getContext(), info, _packageManager);
            if (_apps != null && !_apps.contains(app))
                _apps.add(app);
            return app;
        } catch (Exception e) {
            return null;
        }
    }

    public void onAppUpdated(Context p1, Intent p2) {
        onReceive(p1, p2);
    }

    public void addUpdateListener(AppUpdateListener updateListener) {
        _updateListeners.add(updateListener);
    }

    public void addDeleteListener(AppDeleteListener deleteListener) {
        _deleteListeners.add(deleteListener);
    }

    public void notifyUpdateListeners(@NonNull List<App> apps) {
        Iterator<AppUpdateListener> iter = _updateListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppUpdated(apps)) {
                iter.remove();
            }
        }
    }

    public void notifyRemoveListeners(@NonNull List<App> apps) {
        Iterator<AppDeleteListener> iter = _deleteListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppDeleted(apps)) {
                iter.remove();
            }
        }
    }

    private class AsyncGetApps extends AsyncTask {
        private List<App> tempApps;

        @Override
        protected void onPreExecute() {
            tempApps = new ArrayList<>(_apps);
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            tempApps = null;
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] p1) {
            _apps.clear();
            _nonFilteredApps.clear();

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> activitiesInfo = _packageManager.queryIntentActivities(intent, 0);
            AppSettings appSettings = AppSettings.get();
            String sort = appSettings.getSortMode();
            activitiesInfo = sortApplications(activitiesInfo, sort);
            /*switch (sort) {
                case "az":



                    /*Collections.sort(activitiesInfo, (pa, p2) -> Collator.getInstance().compare(
                            pa.loadLabel(_packageManager).toString(),
                            p2.loadLabel(_packageManager).toString()));

                    break;
                case "za":
                    activitiesInfo = sortApplications(activitiesInfo, "byNameDesc");
                    /*Collections.sort(activitiesInfo, (p2, pz) -> Collator.getInstance().compare(
                            pz.loadLabel(_packageManager).toString(),
                            p2.loadLabel(_packageManager).toString()));

                    break;
                case "li":
                   //activitiesInfo = sortApplications(activitiesInfo, "byInstalledDate");

                    Collections.sort(activitiesInfo, (pi, p2) -> Collator.getInstance().compare(
                            pi.activityInfo.applicationInfo.sourceDir,
                            p2.activityInfo.applicationInfo.sourceDir));

                    break;

                case "mu":
                    activitiesInfo = sortApplications(activitiesInfo, "byMostUsed");
                    break;
            }*/
            for (ResolveInfo info : activitiesInfo) {
                App app = new App(_context, info, _packageManager);
                _nonFilteredApps.add(app);
            }

            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
            if (hiddenList != null) {
                for (int i = 0; i < _nonFilteredApps.size(); i++) {
                    boolean shouldGetAway = false;
                    for (String hidItemRaw : hiddenList) {
                        if ((_nonFilteredApps.get(i).getPackageName() + "/" + _nonFilteredApps.get(i).getClassName()).equals(hidItemRaw)) {
                            shouldGetAway = true;
                            break;
                        }
                    }
                    if (!shouldGetAway) {
                        _apps.add(_nonFilteredApps.get(i));
                    }
                }
            } else {
                for (ResolveInfo info : activitiesInfo)
                    _apps.add(new App(_context, info, _packageManager));
            }

            if (!appSettings.getIconPack().isEmpty() && Tool.isPackageInstalled(appSettings.getIconPack(), _packageManager)) {
                IconPackHelper.applyIconPack(AppManager.this, Tool.dp2px(appSettings.getIconSize(), _context), appSettings.getIconPack(), _apps);
            }
            return null;
        }

        private List<ResolveInfo> sortApplications(List<ResolveInfo> activitiesInfo, String sort) {
            List<ResolveInfo> sortedAtivities = activitiesInfo;
            switch (sort) {
                default:
                case Config.APP_SORT_AZ:
                    Collections.sort(sortedAtivities, (p1, p2) -> Collator.getInstance().compare(
                            p1.loadLabel(_packageManager).toString(),
                            p2.loadLabel(_packageManager).toString()));
                break;
                case Config.APP_SORT_ZA:
                    Collections.sort(sortedAtivities, (p2, p1) -> Collator.getInstance().compare(
                            p2.loadLabel(_packageManager).toString(),
                            p1.loadLabel(_packageManager).toString()));
                    break;
                case Config.APP_SORT_LI:
                    Log.i("apps","sorting by Last Installed" );
                    Collections.sort(sortedAtivities, new InstallTimeComparator(_packageManager));
                    break;
                case Config.APP_SORT_MU:


                    break;
            }

            return  sortedAtivities;
        }

        @Override
        protected void onPostExecute(Object result) {

            notifyUpdateListeners(_apps);

            List<App> removed = Tool.getRemovedApps(tempApps, _apps);
            if (removed.size() > 0) {
                notifyRemoveListeners(removed);
            }

            if (_recreateAfterGettingApps) {
                _recreateAfterGettingApps = false;
                if (_context instanceof HomeActivity)
                    ((HomeActivity) _context).recreate();
            }

            super.onPostExecute(result);
        }
    }

    public static class InstallTimeComparator implements Comparator<ResolveInfo> {
        private final PackageManager mPackageManager;

        public InstallTimeComparator(PackageManager packageManager) {
            mPackageManager = packageManager;
        }

        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            try {
                long lhsInstallTime = mPackageManager.getPackageInfo(lhs.activityInfo.packageName, 0).firstInstallTime;
                long rhsInstallTime = mPackageManager.getPackageInfo(rhs.activityInfo.packageName, 0).firstInstallTime;
                if (lhsInstallTime < rhsInstallTime) {
                    return 1;
                } else if (rhsInstallTime < lhsInstallTime) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    public static abstract class AppUpdatedListener implements AppUpdateListener {
        private String listenerID;

        public AppUpdatedListener() {
            listenerID = UUID.randomUUID().toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AppUpdatedListener && ((AppUpdatedListener) obj).listenerID.equals(this.listenerID);
        }
    }
}
