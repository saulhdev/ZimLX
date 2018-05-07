package org.zimmob.zimlx.viewutil;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.IconLabelItem;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppManager;
import org.zimmob.zimlx.util.Tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class DialogHelper {
    public static void editItemDialog(String title, String defaultText, Context c, final OnItemEditListener listener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(c);
        builder.title(title)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(null, defaultText, (dialog, input) -> listener.itemLabel(input.toString())).show();
    }

    public static void alertDialog(Context context, String title, String msg, MaterialDialog.SingleButtonCallback onPositive) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .onPositive(onPositive)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(R.string.ok)
                .show();
    }

    public static void alertDialog(Context context, String title, String msg, String positive, MaterialDialog.SingleButtonCallback onPositive) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .onPositive(onPositive)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(positive)
                .show();
    }


    public static void addActionItemDialog(final Context context, MaterialDialog.ListCallback callback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(R.string.desktop_action)
                .items(R.array.entries__desktop_actions)
                .itemsCallback(callback)
                .show();
    }

    public static void selectAppDialog(final Context context, final OnAppSelectedListener onAppSelectedListener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        FastItemAdapter<IconLabelItem> fastItemAdapter = new FastItemAdapter<>();
        builder.title(R.string.select_app)
                .adapter(fastItemAdapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
                .negativeText(R.string.cancel);

        final MaterialDialog dialog = builder.build();
        List<IconLabelItem> items = new ArrayList<>();
        final List<App> apps = AppManager.getInstance(context).getApps();
        int size = Tool.dp2px(18, context);
        int sizePad = Tool.dp2px(8, context);
        for (int i = 0; i < apps.size(); i++) {
            items.add(new IconLabelItem(context, apps.get(i).getIconProvider(), apps.get(i).getLabel(), size)
                    .withIconGravity(Gravity.START)
                    .withDrawablePadding(context, sizePad));
        }
        fastItemAdapter.set(items);
        fastItemAdapter.withOnClickListener((v, adapter, item, position) -> {
            if (onAppSelectedListener != null) {
                onAppSelectedListener.onAppSelected(apps.get(position));
            }
            dialog.dismiss();
            return true;
        });
        dialog.show();
    }

    /**
     * @param context
     * @param item
     */
    public static void deletePackageDialog(Context context, Item item) {
        if (item.getType() == Item.Type.APP) {
            try {
                Uri packageURI = Uri.parse("package:" + item.getIntent().getComponent().getPackageName());
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                context.startActivity(uninstallIntent);
            } catch (Exception e) {

            }
        }
    }

    /**
     * @param context
     */
    public static void backupDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.pref_title__backup)
                .positiveText(R.string.cancel)
                .items(R.array.entries__backup_options)
                .itemsCallback((dialog, itemView, item, text) -> {
                    PackageManager m = context.getPackageManager();
                    String s = context.getPackageName();

                    if (context.getResources().getStringArray(R.array.entries__backup_options)[item].equals(context.getResources().getString(R.string.dialog__backup_app_settings__backup))) {
                        File directory = new File(Environment.getExternalStorageDirectory() + "/ZimLx/");
                        if (!directory.exists()) {
                            directory.mkdir();
                        }
                        try {
                            PackageInfo p = m.getPackageInfo(s, 0);
                            s = p.applicationInfo.dataDir;
                            Tool.copy(context, s + "/databases/home.db", directory + "/home.db");
                            Tool.copy(context, s + "/shared_prefs/app.xml", directory + "/app.xml");
                            Toast.makeText(context, R.string.dialog__backup_app_settings__success, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (context.getResources().getStringArray(R.array.entries__backup_options)[item].equals(context.getResources().getString(R.string.dialog__backup_app_settings__restore))) {
                        File directory = new File(Environment.getExternalStorageDirectory() + "/ZimLx/");
                        try {
                            PackageInfo p = m.getPackageInfo(s, 0);
                            s = p.applicationInfo.dataDir;
                            Tool.copy(context, directory + "/home.db", s + "/databases/home.db");
                            Tool.copy(context, directory + "/app.xml", s + "/shared_prefs/app.xml");
                            Toast.makeText(context, R.string.dialog__backup_app_settings__success, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show();
                        }
                        System.exit(1);
                    }
                }).show();
    }

    public interface OnAppSelectedListener {
        void onAppSelected(App app);
    }

    /**
     *
     */
    public interface OnItemEditListener {
        void itemLabel(String label);
    }
}