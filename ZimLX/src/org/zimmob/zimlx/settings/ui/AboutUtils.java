package org.zimmob.zimlx.settings.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;

import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.preference.Preference;

import com.android.launcher3.R;

import net.gsantner.opoc.util.ContextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AboutUtils {
    private Activity activity;
    private Context context;

    public AboutUtils(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public void showGooglePlayEntryForThisApp() {
        String pkgId = "details?id=" + activity.getPackageName();
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://" + pkgId));
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                (Build.VERSION.SDK_INT >= 25 ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/" + pkgId)));
        }
    }

    public void openWebpageInExternalBrowser(final String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void showDialogWithHtmlTextView(@StringRes int resTitleId, String html) {
        showDialogWithHtmlTextView(resTitleId, html, true, null);
    }

    public void showDialogWithHtmlTextView(@StringRes int resTitleId, String text, boolean isHtml, DialogInterface.OnDismissListener dismissedListener) {
        AppCompatTextView textView = new AppCompatTextView(context);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                context.getResources().getDisplayMetrics());
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setPadding(padding, 0, padding, 0);

        textView.setText(isHtml ? new SpannableString(Html.fromHtml(text)) : text);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(dismissedListener)
                .setTitle(resTitleId)
                .setView(textView);
        dialog.show();
    }

    public void updateAppSummary(Preference prefx) {
        Locale locale = Locale.getDefault();
        Preference pref;
        if ((pref = prefx) != null && pref.getSummary() == null) {
            pref.setIcon(R.drawable.ic_launcher);
            pref.setSummary(String.format(locale, "%s\nVersion v%s (build %d)", getPackageName(), getAppVersionName(), getAppVersionCode()));
        }
    }

    public void updateAppInfoSummary(Preference prefx) {
        Locale locale = Locale.getDefault();
        Preference pref;
        String tmp;

        if ((pref = prefx) != null && pref.getSummary() == null) {

            String summary = String.format(locale, "\n<b>Package:</b> %s\n<b>Version:</b> v%s (build %s)", getPackageName(), getAppVersionName(), getAppVersionCode());
            summary += (tmp = bcstr("FLAVOR", "")).isEmpty() ? "" : ("\n<b>Flavor:</b> " + tmp.replace("flavor", ""));
            summary += (tmp = bcstr("BUILD_TYPE", "")).isEmpty() ? "" : (" (" + tmp + ")");
            summary += (tmp = bcstr("BUILD_DATE", "")).isEmpty() ? "" : ("\n<b>Build date:</b> " + tmp);
            summary += (tmp = getAppInstallationSource()).isEmpty() ? "" : ("\n<b>ISource:</b> " + tmp);
            summary += (tmp = bcstr("GITHASH", "")).isEmpty() ? "" : ("\n<b>VCS Hash:</b> " + tmp);
            pref.setSummary(htmlToSpanned(summary.trim().replace("\n", "<br/>")));

        }
    }


    public Spanned htmlToSpanned(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public String bcstr(String fieldName, String defaultValue) {
        Object field = getBuildConfigValue(fieldName);
        if (field != null && field instanceof String) {
            return (String) field;
        }
        return defaultValue;
    }

    public String readTextfileFromRawRes(@RawRes int rawResId, String linePrefix, String linePostfix) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        String line;

        linePrefix = linePrefix == null ? "" : linePrefix;
        linePostfix = linePostfix == null ? "" : linePostfix;

        try {
            br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(rawResId)));
            while ((line = br.readLine()) != null) {
                sb.append(linePrefix);
                sb.append(line);
                sb.append(linePostfix);
                sb.append("\n");
            }
        } catch (Exception ignored) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        return sb.toString();
    }

    public Object getBuildConfigValue(String fieldName) {
        //String pkg = getPackageName() + ".BuildConfig";
        String pkg = "com.android.launcher3.BuildConfig";
        try {
            Class<?> c = Class.forName(pkg);
            return c.getField(fieldName).get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAppVersionName() {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "?";
        }
    }

    public int getAppVersionCode() {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getAppInstallationSource() {
        String src = null;
        try {
            src = context.getPackageManager().getInstallerPackageName(getPackageName());
        } catch (Exception ignored) {
        }
        if (TextUtils.isEmpty(src)) {
            src = "Sideloaded";
        }
        switch (src) {
            case "com.android.vending":
            case "com.google.android.feedback": {
                return "Google Play Store";
            }
            case "org.fdroid.fdroid.privileged":
            case "org.fdroid.fdroid": {
                return "F-Droid";
            }
            case "com.github.yeriomin.yalpstore": {
                return "Yalp Store";
            }
            case "cm.aptoide.pt": {
                return "Aptoide";
            }
        }
        if (src.toLowerCase().contains(".amazon.")) {
            return "Amazon Appstore";
        }
        return src;
    }

    public String getPackageName() {
        String pkg = rstr("manifest_package_id");
        return pkg != null ? pkg : context.getPackageName();
    }


    /**
     * Get String by given string ressource id (nuermic)
     */
    public String rstr(@StringRes int strResId) {
        return context.getString(strResId);
    }

    /**
     * Get String by given string ressource identifier (textual)
     */
    public String rstr(String strResKey) {
        try {
            return rstr(getResId(ContextUtils.ResType.STRING, strResKey));
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    public int getResId(ContextUtils.ResType resType, final String name) {
        return context.getResources().getIdentifier(name, resType.name().toLowerCase(), context.getPackageName());
    }

    public boolean setClipboard(CharSequence text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager cm = ((android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE));
            if (cm != null) {
                cm.setText(text);
                return true;
            }
        } else {
            android.content.ClipboardManager cm = ((android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE));
            if (cm != null) {
                ClipData clip = ClipData.newPlainText(context.getPackageName(), text);
                cm.setPrimaryClip(clip);
                return true;
            }
        }
        return false;
    }
}
