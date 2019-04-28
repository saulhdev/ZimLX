package com.google.android.apps.nexuslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;

import com.android.launcher3.LauncherModel;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;

import java.util.HashMap;
import java.util.List;

public class CustomIconUtils {
    private final static String[] ICON_INTENTS = new String[]{
            "com.novalauncher.THEME",
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.fede.launcher.THEME_ICONPACK",
            "com.gau.go.launcherex.theme",
            "com.dlto.atom.launcher.THEME"

    };

    static HashMap<String, CharSequence> getPackProviders(Context context) {
        PackageManager pm = context.getPackageManager();
        HashMap<String, CharSequence> packs = new HashMap<>();
        for (String intent : ICON_INTENTS) {
            for (ResolveInfo info : pm.queryIntentActivities(new Intent(intent), PackageManager.GET_META_DATA)) {
                packs.put(info.activityInfo.packageName, info.loadLabel(pm));
            }
        }
        return packs;
    }

    public static void reloadIcon(DeepShortcutManager shortcutManager, LauncherModel model, UserHandle user, String pkg) {
        model.onPackageChanged(pkg, user);
        if (shortcutManager.wasLastCallSuccess()) {
            List<ShortcutInfoCompat> shortcuts = shortcutManager.queryForPinnedShortcuts(pkg, user);
            if (!shortcuts.isEmpty()) {
                model.updatePinnedShortcuts(pkg, shortcuts, user);
            }
        }
    }
}
