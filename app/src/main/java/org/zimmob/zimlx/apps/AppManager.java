package org.zimmob.zimlx.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.icon.IconPackHandler;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.Tool;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class AppManager {
    private static AppManager _ref;

    public static AppManager getInstance(Context context) {
        return _ref == null ? (_ref = new AppManager(context)) : _ref;
    }

    private final List<IAppUpdateListener> updateListeners = new ArrayList<>();
    private final List<IAppDeleteListener> deleteListeners = new ArrayList<>();
    public boolean recreateAfterGettingApps;
    private PackageManager packageManager;
    private List<App> apps = new ArrayList<>();
    private List<App> nonFilteredApps = new ArrayList<>();
    private AsyncTask task;
    private Context _context;

    private AppManager(Context c) {
        _context = c;
        packageManager = c.getPackageManager();
    }

    public Context getContext() {
        return _context;
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }

    private App findApp(Intent intent) {
        if (intent == null || intent.getComponent() == null) return null;
        String packageName = intent.getComponent().getPackageName();
        String className = intent.getComponent().getClassName();
        for (App app : apps) {
            if (app.getClassName().equals(className) && app.getPackageName().equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    public List<App> getApps() {
        getAllApps();
        return apps;
    }

    public List<App> getNonFilteredApps() {
        return nonFilteredApps;
    }

    public void init() {
        getAllApps();
    }

    private void getAllApps() {
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED)
            task = new AsyncGetApps().execute();
        else if (task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(false);
            task = new AsyncGetApps().execute();
        }
    }

    private void onReceive() {
        getAllApps();
    }

    public List<App> getAllApps(Context context, boolean includeHidden) {
        return includeHidden ? getNonFilteredApps() : getApps();
    }

    public App findItemApp(Item item) {
        return findApp(item.getIntent());
    }

    public App createApp(Intent intent) {
        try {
            ResolveInfo info = packageManager.resolveActivity(intent, 0);
            App app = new App(info, packageManager);
            if (apps != null && !apps.contains(app))
                apps.add(app);
            return app;
        } catch (Exception e) {
            return null;
        }
    }

    public void onAppUpdated(Context p1, Intent p2) {
        onReceive(p1, p2);
    }

    public void onReceive(Context p1, Intent p2) {
        getAllApps();
    }

    public void addUpdateListener(IAppUpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

    public void addDeleteListener(IAppDeleteListener deleteListener) {
        deleteListeners.add(deleteListener);
    }

    private void notifyUpdateListeners(@NonNull List<App> apps) {
        Iterator<IAppUpdateListener> iter = updateListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppUpdated(apps)) {
                iter.remove();
            }
        }
    }

    private void notifyRemoveListeners(@NonNull List<App> apps) {
        Iterator<IAppDeleteListener> iter = deleteListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppDeleted(apps)) {
                iter.remove();
            }
        }
    }

    public interface IAppDeleteListener {
        /**
         * @param apps list of apps
         * @return true, if the listener should be removed
         */
        boolean onAppDeleted(List<App> apps);
    }

    public interface IAppUpdateListener {

        /**
         * @param apps list of apps
         * @return true, if the listener should be removed
         */
        boolean onAppUpdated(List<App> apps);
    }

    public static class MostUsedComparator implements Comparator<ResolveInfo> {

        public MostUsedComparator() {
        }

        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            int item1 = HomeActivity.companion.getDb().getAppCount(lhs.activityInfo.packageName);
            int item2 = HomeActivity.companion.getDb().getAppCount(rhs.activityInfo.packageName);
            if (item1 < item2) {
                return 1;
            } else if (item2 < item1) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static class InstallTimeComparator implements Comparator<ResolveInfo> {
        private final PackageManager mPackageManager;

        InstallTimeComparator(PackageManager packageManager) {
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

    public static abstract class AppUpdatedListener implements IAppUpdateListener {
        private String listenerID;

        public AppUpdatedListener() {
            listenerID = UUID.randomUUID().toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AppUpdatedListener && ((AppUpdatedListener) obj).listenerID.equals(this.listenerID);
        }
    }

    private class AsyncGetApps extends AsyncTask {
        private List<App> tempApps;

        @Override
        protected void onPreExecute() {
            tempApps = new ArrayList<>(apps);
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            tempApps = null;
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] p1) {
            apps.clear();
            nonFilteredApps.clear();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> activitiesInfo = packageManager.queryIntentActivities(intent, 0);
            AppSettings appSettings = AppSettings.get();
            int sort = appSettings.getSortMode();
            activitiesInfo = sortApplications(activitiesInfo, sort);


            for (ResolveInfo info : activitiesInfo) {
                App app = new App(info, packageManager);
                nonFilteredApps.add(app);
            }
            String myApp;
            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
            if (hiddenList != null) {
                for (int i = 0; i < nonFilteredApps.size(); i++) {
                    boolean shouldGetAway = false;
                    for (String hidItemRaw : hiddenList) {
                        myApp = "ComponentInfo{" + nonFilteredApps.get(i).getPackageName() + "/" + nonFilteredApps.get(i).getClassName() + "}";

                        if (myApp.equals(hidItemRaw)) {
                            shouldGetAway = true;
                            break;
                        }
                    }
                    if (!shouldGetAway) {
                        apps.add(nonFilteredApps.get(i));
                    }
                }
            } else {
                for (ResolveInfo info : activitiesInfo)
                    apps.add(new App(info, packageManager));
            }

            if (!appSettings.getIconPack().isEmpty() && Tool.isPackageInstalled(appSettings.getIconPack(), packageManager)) {
                IconPackHandler iconsHandler = new IconPackHandler(_context);
                iconsHandler.applyIconPack(AppManager.this, Tool.dp2px(appSettings.getIconSize(), _context), appSettings.getIconPack(), apps);
            }
            return null;
        }

        private List<ResolveInfo> sortApplications(List<ResolveInfo> activitiesInfo, int sort) {
            List<ResolveInfo> sortedActivities = activitiesInfo;
            switch (sort) {
                default:
                case Config.APP_SORT_AZ:
                    Log.i("apps", "sorting by AZ");
                    Collections.sort(sortedActivities, (p1, p2) -> Collator.getInstance().compare(
                            p1.loadLabel(packageManager).toString(),
                            p2.loadLabel(packageManager).toString()));
                    break;
                case Config.APP_SORT_ZA:
                    Log.i("apps", "sorting by ZA");
                    Collections.sort(sortedActivities, (p2, p1) -> Collator.getInstance().compare(
                            p1.loadLabel(packageManager).toString(),
                            p2.loadLabel(packageManager).toString()));
                    break;
                case Config.APP_SORT_LI:
                    if (AppSettings.get().getDrawerStyle() == Config.DRAWER_HORIZONTAL) {
                        Log.i("apps", "sorting by Last Installed");
                        Collections.sort(sortedActivities, new InstallTimeComparator(packageManager));
                    } else {
                        Collections.sort(sortedActivities, (p1, p2) -> Collator.getInstance().compare(
                                p1.loadLabel(packageManager).toString(),
                                p2.loadLabel(packageManager).toString()));
                    }

                    break;
                case Config.APP_SORT_MU:
                    if (AppSettings.get().getDrawerStyle() == Config.DRAWER_HORIZONTAL) {
                        Log.i("apps", "Sorting by Most Used");
                        Collections.sort(sortedActivities, new MostUsedComparator());
                    } else {
                        Collections.sort(sortedActivities, (p2, p1) -> Collator.getInstance().compare(
                                p1.loadLabel(packageManager).toString(),
                                p2.loadLabel(packageManager).toString()));
                    }
                    break;
            }
            return sortedActivities;
        }

        @Override
        protected void onPostExecute(Object result) {
            notifyUpdateListeners(apps);
            List<App> removed = Tool.getRemovedApps(tempApps, apps);
            if (removed.size() > 0) {
                notifyRemoveListeners(removed);
            }
            if (recreateAfterGettingApps) {
                recreateAfterGettingApps = false;
                if (_context instanceof HomeActivity)
                    ((HomeActivity) _context).recreate();
            }
            super.onPostExecute(result);
        }
    }
}
