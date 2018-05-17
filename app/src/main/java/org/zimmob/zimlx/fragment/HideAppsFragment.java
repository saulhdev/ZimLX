package org.zimmob.zimlx.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.util.AppManager;
import org.zimmob.zimlx.util.AppSettings;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class HideAppsFragment extends Fragment {
    private ArrayList<String> listActivitiesHidden = new ArrayList();
    private ArrayList<App> listActivitiesAll = new ArrayList();
    private ViewSwitcher switcherLoad;
    private AsyncWorkerList taskList = new AsyncWorkerList();
    private Typeface tf;

    private static final String TAG = "RequestActivity";
    private static final boolean DEBUG = true;

    private ViewSwitcher viewSwitcher;
    private ListView grid;
    private AppAdapter appInfoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.request, container, false);
        switcherLoad = rootView.findViewById(R.id.viewSwitcherLoadingMain);

        FloatingActionButton fab = rootView.findViewById(R.id.fab_rq);
        fab.setOnClickListener(view -> confirmSelection());

        if (taskList.getStatus() == AsyncTask.Status.PENDING) {
            // My AsyncTask has not started yet
            taskList.execute();
        }

        if (taskList.getStatus() == AsyncTask.Status.FINISHED) {
            // My AsyncTask is done and onPostExecute was called
            new AsyncWorkerList().execute();
        }

        return rootView;
    }

    public class AsyncWorkerList extends AsyncTask<String, Integer, String> {

        private AsyncWorkerList() {
        }

        @Override
        protected void onPreExecute() {
            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
            listActivitiesHidden.addAll(hiddenList);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                // Compare them to installed apps
                prepareData();
                return null;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            populateView();
            //Switch from loading screen to the main view
            switcherLoad.showNext();

            super.onPostExecute(result);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (DEBUG) Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
    }

    private void confirmSelection() {
        Thread actionSend_Thread = new Thread() {

            @Override
            public void run() {
                AppSettings.get().setHiddenAppsList(listActivitiesHidden);
                getActivity().finish();
            }
        };
        if (!actionSend_Thread.isAlive()) {
            //Prevents the thread to be executed twice (or more) times.
            actionSend_Thread.start();
        }
    }

    private void prepareData() {
        List<App> apps = AppManager.getInstance(getContext()).getNonFilteredApps();
        listActivitiesAll.addAll(apps);
    }

    @SuppressWarnings("unchecked")
    private void populateView() {
        grid = getActivity().findViewById(R.id.app_grid);

        assert grid != null;
        grid.setFastScrollEnabled(true);
        grid.setFastScrollAlwaysVisible(false);

        appInfoAdapter = new AppAdapter(getActivity(), listActivitiesAll);

        grid.setAdapter(appInfoAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> AdapterView, View view, int position, long row) {
                App appInfo = (App) AdapterView.getItemAtPosition(position);
                CheckBox checker = view.findViewById(R.id.CBappSelect);
                ViewSwitcher icon = view.findViewById(R.id.viewSwitcherChecked);

                checker.toggle();
                if (checker.isChecked()) {
                    listActivitiesHidden.add(appInfo.getComponentName());
                    if (DEBUG) Log.v(TAG, "Selected App: " + appInfo.getLabel());
                    if (icon.getDisplayedChild() == 0) {
                        icon.showNext();
                    }
                } else {
                    listActivitiesHidden.remove(appInfo.getComponentName());
                    if (DEBUG) Log.v(TAG, "Deselected App: " + appInfo.getLabel());
                    if (icon.getDisplayedChild() == 1) {
                        icon.showPrevious();
                    }
                }
            }
        });
    }

    private class AppAdapter extends ArrayAdapter<App> {
        @SuppressWarnings("unchecked")

        private AppAdapter(Context context, ArrayList<App> adapterArrayList) {
            super(context, R.layout.request_item_list, adapterArrayList);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.request_item_list, parent, false);
                holder = new ViewHolder();
                holder.apkIcon = convertView.findViewById(R.id.IVappIcon);
                holder.apkName = convertView.findViewById(R.id.TVappName);
                holder.apkPackage = convertView.findViewById(R.id.TVappPackage);
                holder.checker = convertView.findViewById(R.id.CBappSelect);
                holder.switcherChecked = convertView.findViewById(R.id.viewSwitcherChecked);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            App appInfo = getItem(position);

            holder.apkPackage.setText(appInfo.getComponentName());
            holder.apkName.setText(appInfo.getLabel());
            holder.apkIcon.setImageDrawable(appInfo.getIcon());

            holder.switcherChecked.setInAnimation(null);
            holder.switcherChecked.setOutAnimation(null);
            holder.checker.setChecked(listActivitiesHidden.contains(appInfo.getComponentName()));
            if (listActivitiesHidden.contains(appInfo.getComponentName())) {
                if (holder.switcherChecked.getDisplayedChild() == 0) {
                    holder.switcherChecked.showNext();
                }
            } else {
                if (holder.switcherChecked.getDisplayedChild() == 1) {
                    holder.switcherChecked.showPrevious();
                }
            }
            return convertView;
        }   
    }

    private class ViewHolder {
        TextView apkName;
        TextView apkPackage;
        ImageView apkIcon;
        CheckBox checker;
        ViewSwitcher switcherChecked;
    }

}
