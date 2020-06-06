//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.google.android.apps.nexuslauncher.util;

import android.content.Context;

import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.util.ComponentKey;

import org.jetbrains.annotations.NotNull;

public class ComponentKeyMapper {

    private final ComponentKey componentKey;
    private final Context mContext;

    public ComponentKeyMapper(Context var1, ComponentKey var2) {
        this.mContext = var1;
        this.componentKey = var2;
    }

    public ItemInfoWithIcon getApp(AllAppsStore allAppsStore) {
        /*AppInfo info = allAppsStore.getApp(this.componentKey);
        if (info != null) {
            return info;
        } else if (this.getComponentClass().equals("@instantapp")) {
            b var5 = b.b(this.mContext);
            String var6 = this.componentKey.componentName.getPackageName();
            return (a) var5.J.get(var6);
        } else if (this.componentKey instanceof ShortcutKey) {
            com.google.android.apps.nexuslauncher.a.a var4 = com.google.android.apps.nexuslauncher.a.a
                    .a(this.mContext);
            ShortcutKey var2 = (ShortcutKey) this.componentKey;
            return var4.d.get(var2);
        } else {
            return null;
        }*/
        return null;
    }

    public String getComponentClass() {
        return this.componentKey.componentName.getClassName();
    }

    public ComponentKey getComponentKey() {
        return this.componentKey;
    }

    public String getPackage() {
        return this.componentKey.componentName.getPackageName();
    }

    @NotNull
    public String toString() {
        return this.componentKey.toString();
    }
}
