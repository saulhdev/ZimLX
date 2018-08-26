package org.zimmob.zimlx.widget;

import org.zimmob.zimlx.model.PackageItemInfo;

import java.util.ArrayList;

public class WidgetListRowEntry {
    public final PackageItemInfo pkgItem;
    public final ArrayList widgets;
    public String titleSectionName;

    public WidgetListRowEntry(PackageItemInfo packageItemInfo, ArrayList arrayList) {
        this.pkgItem = packageItemInfo;
        this.widgets = arrayList;
    }
}