/*
 * Copyright (c) 2020 Zim Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.zimmob.zimlx.model;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace;
import com.android.launcher3.model.GridSizeMigrationTask;
import com.android.launcher3.provider.LauncherDbUtils;
import com.android.launcher3.util.GridOccupancy;
import com.android.launcher3.widget.custom.CustomWidgetParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.android.launcher3.LauncherSettings.*;
import static org.zimmob.zimlx.settings.SettingsActivity.ALLOW_OVERLAP_PREF;
import static org.zimmob.zimlx.settings.SettingsActivity.SMARTSPACE_PREF;

public class HomeWidgetMigrationTask extends GridSizeMigrationTask {
    public static final String PREF_MIGRATION_STATUS = "pref_migratedSmartspace";

    private final Context mContext;
    private final int mTrgX, mTrgY;

    private HomeWidgetMigrationTask(Context context,
                                    SQLiteDatabase db,
                                    HashSet<String> validPackages,
                                    Point size) {
        super(context, db, validPackages, size, size);

        mContext = context;

        mTrgX = size.x;
        mTrgY = size.y;
    }

    @SuppressLint("ApplySharedPref")
    public static void migrateIfNeeded(Context context) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        boolean needsMigration = !prefs.getBoolean(PREF_MIGRATION_STATUS, false)
                && prefs.getBoolean(SMARTSPACE_PREF, true);
        if (!needsMigration) return;
        // Save the pref so we only run migration once
        prefs.edit().putBoolean(PREF_MIGRATION_STATUS, true).commit();

        HashSet<String> validPackages = getValidPackages(context);
        SQLiteDatabase mDb;
        InvariantDeviceProfile idp = LauncherAppState.getIDP(context);
        Point size = new Point(idp.numColumns, idp.numRows);
        try (LauncherDbUtils.SQLiteTransaction transaction = (LauncherDbUtils.SQLiteTransaction) Settings.call(
                context.getContentResolver(), Settings.METHOD_NEW_TRANSACTION)
                .getBinder(Settings.EXTRA_VALUE)) {
            if (!new HomeWidgetMigrationTask(context, transaction.getDb(),
                    validPackages, size).migrateWorkspace()) {
                throw new RuntimeException("Failed to migrate Smartspace");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean migrateWorkspace() throws Exception {
        ArrayList<Integer> allScreens = LauncherModel.loadWorkspaceScreensDb(mContext);
        if (allScreens.isEmpty()) {
            throw new Exception("Unable to get workspace screens");
        }

        boolean allowOverlap = Utilities.getPrefs(mContext)
                .getBoolean(ALLOW_OVERLAP_PREF, false);
        GridOccupancy occupied = new GridOccupancy(mTrgX, mTrgY);

        if (!allowOverlap) {
            ArrayList<DbEntry> firstScreenItems = new ArrayList<>();
            for (int screenId : allScreens) {
                ArrayList<DbEntry> items = loadWorkspaceEntries(screenId);
                if (screenId == Workspace.FIRST_SCREEN_ID) {
                    firstScreenItems.addAll(items);
                    break;
                }
            }

            for (DbEntry item : firstScreenItems) {
                occupied.markCells(item, true);
            }
        }

        if (allowOverlap || occupied.isRegionVacant(0, 0, mTrgX, 1)) {
            List<LauncherAppWidgetProviderInfo> customWidgets =
                    CustomWidgetParser.getCustomWidgets(mContext);
            if (!customWidgets.isEmpty()) {
                LauncherAppWidgetProviderInfo provider = customWidgets.get(0);
                int widgetId = CustomWidgetParser
                        .getWidgetIdForCustomProvider(mContext, provider.provider);
                long itemId = LauncherSettings.Settings.call(mContext.getContentResolver(),
                        Settings.METHOD_NEW_ITEM_ID)
                        .getLong(Settings.EXTRA_VALUE);

                ContentValues values = new ContentValues();
                values.put(Favorites._ID, itemId);
                values.put(Favorites.CONTAINER, Favorites.CONTAINER_DESKTOP);
                values.put(Favorites.SCREEN, Workspace.FIRST_SCREEN_ID);
                values.put(Favorites.CELLX, 0);
                values.put(Favorites.CELLY, 0);
                values.put(Favorites.SPANX, mTrgX);
                values.put(Favorites.SPANY, 1);
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_CUSTOM_APPWIDGET);
                values.put(Favorites.APPWIDGET_ID, widgetId);
                values.put(Favorites.APPWIDGET_PROVIDER, provider.provider.flattenToString());
                mUpdateOperations.put(mUpdateOperations.size() + 1, values);
            }
        }

        return applyOperations();
    }
}
