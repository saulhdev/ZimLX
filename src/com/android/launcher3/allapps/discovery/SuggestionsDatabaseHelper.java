package com.android.launcher3.allapps.discovery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.launcher3.Utilities;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class SuggestionsDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "trebuchet_suggestions_db";

    private static final String TABLE_NAME = "suggestion_candidates";
    private static final String KEY_UID = "uid";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_CLASS_NAME = "className";
    private static final String KEY_DAY_COUNTER = "dayCounter";
    private static final String KEY_NIGHT_COUNTER = "nightCounter";
    private static final String KEY_HEADSET_COUNTER = "headsetCounter";

    private static final String[] ALL_COLUMNS = {
            KEY_UID, KEY_PACKAGE_NAME, KEY_CLASS_NAME,
            KEY_DAY_COUNTER, KEY_NIGHT_COUNTER, KEY_HEADSET_COUNTER
    };
    private static final String QUERY_FILTER =
            KEY_PACKAGE_NAME + " = ? AND " + KEY_CLASS_NAME + " = ?";

    private static final String CMD_CREATE_TABLE = "CREATE TABLE %1$s (" +
            "%2$s INTEGER PRIMARY KEY AUTOINCREMENT," + // uid
            "%3$s TEXT NOT NULL," + // packageName
            "%4$s TEXT NOT NULL, " + // className
            "%5$s INTEGER NOT NULL DEFAULT -1, " + // dayCounter
            "%6$s INTEGER NOT NULL DEFAULT -1, " + // nightCounter
            "%7$s INTEGER NOT NULL DEFAULT -1);"; // headsetCounter

    private static SuggestionsDatabaseHelper sInstance = null;

    private SuggestionsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SuggestionsDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SuggestionsDatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(CMD_CREATE_TABLE, TABLE_NAME, KEY_UID, KEY_PACKAGE_NAME,
                KEY_CLASS_NAME, KEY_DAY_COUNTER, KEY_NIGHT_COUNTER, KEY_HEADSET_COUNTER));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void increaseCounter(Context context, @NonNull SuggestionCandidate candidate) {
        candidate.increaseCounter(context);
        saveSuggestion(candidate);
    }

    public List<SuggestionCandidate> getSuggestionCandidates(Context context) {
        int numPredictedApps = Integer.valueOf(Utilities.getZimPrefs(context).getNumPredictedApps());

        List<SuggestionCandidate> candidates = new ArrayList<>();
        int i = 0;

        String counterColumn;
        if (Utilities.hasHeadset(context)) {
            counterColumn = KEY_HEADSET_COUNTER;
        } else if (Utilities.isDayTime()) {
            counterColumn = KEY_DAY_COUNTER;
        } else {
            counterColumn = KEY_NIGHT_COUNTER;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null,
                null, null, counterColumn + " DESC");
        if (cursor == null) {
            return candidates;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            return candidates;
        }

        do {
            candidates.add(new SuggestionCandidate(
                    cursor.getString(cursor.getColumnIndex(KEY_PACKAGE_NAME)),
                    cursor.getString(cursor.getColumnIndex(KEY_CLASS_NAME)),
                    cursor.getInt(cursor.getColumnIndex(KEY_DAY_COUNTER)),
                    cursor.getInt(cursor.getColumnIndex(KEY_NIGHT_COUNTER)),
                    cursor.getInt(cursor.getColumnIndex(KEY_HEADSET_COUNTER))));
            i++;
        }
        while (i < numPredictedApps && cursor.moveToNext());

        return candidates;
    }

    private void saveSuggestion(@NonNull SuggestionCandidate candidate) {
        boolean shouldUpdate = hasCandidate(candidate.getPackageName(), candidate.getClassName());

        ContentValues values = new ContentValues();
        values.put(KEY_PACKAGE_NAME, candidate.getPackageName());
        values.put(KEY_CLASS_NAME, candidate.getClassName());
        values.put(KEY_DAY_COUNTER, candidate.getDayCounter());
        values.put(KEY_NIGHT_COUNTER, candidate.getNightCounter());
        values.put(KEY_HEADSET_COUNTER, candidate.getHeadsetCounter());

        SQLiteDatabase db = getWritableDatabase();
        if (shouldUpdate) {
            String[] arguments = new String[]{
                    candidate.getPackageName(), candidate.getClassName()
            };
            db.update(TABLE_NAME, values, QUERY_FILTER, arguments);
        } else {
            db.insert(TABLE_NAME, null, values);
        }

        db.close();
    }

    @NonNull
    public SuggestionCandidate getCandidate(@NonNull String packageName,
                                            @NonNull String className) {
        SQLiteDatabase db = getReadableDatabase();
        String[] arguments = new String[]{packageName, className};
        Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, QUERY_FILTER, arguments,
                null, null, null, null);
        if (cursor == null) {
            return new SuggestionCandidate(packageName, className);
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            return new SuggestionCandidate(packageName, className);
        }

        SuggestionCandidate result = new SuggestionCandidate(
                cursor.getString(cursor.getColumnIndex(KEY_PACKAGE_NAME)),
                cursor.getString(cursor.getColumnIndex(KEY_CLASS_NAME)),
                cursor.getInt(cursor.getColumnIndex(KEY_DAY_COUNTER)),
                cursor.getInt(cursor.getColumnIndex(KEY_NIGHT_COUNTER)),
                cursor.getInt(cursor.getColumnIndex(KEY_HEADSET_COUNTER))
        );
        cursor.close();
        return result;
    }

    private boolean hasCandidate(@NonNull String packageName,
                                 @NonNull String className) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = new String[]{KEY_PACKAGE_NAME, KEY_CLASS_NAME};
        String[] arguments = new String[]{packageName, className};
        Cursor cursor = db.query(TABLE_NAME, columns, QUERY_FILTER, arguments,
                null, null, null, null);

        boolean result = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }
}