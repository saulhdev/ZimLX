package org.zimmob.zimlx.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.UserHandle;

/**
 * Created by saul on 04-14-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */

public class ContentWriter {
    private final ContentValues mValues;
    private final Context mContext;

    private CommitParams mCommitParams;
    private Bitmap mIcon;
    private UserHandle mUser;

    public ContentWriter(Context context, CommitParams commitParams) {
        this(context);
        mCommitParams = commitParams;
    }

    public ContentWriter(Context context) {
        this(new ContentValues(), context);
    }

    public ContentWriter(ContentValues values, Context context) {
        mValues = values;
        mContext = context;
    }

    public ContentWriter put(String key, Integer value) {
        mValues.put(key, value);
        return this;
    }

    public ContentWriter put(String key, Long value) {
        mValues.put(key, value);
        return this;
    }

    public ContentWriter put(String key, String value) {
        mValues.put(key, value);
        return this;
    }

    public ContentWriter put(String key, CharSequence value) {
        mValues.put(key, value == null ? null : value.toString());
        return this;
    }

    public ContentWriter put(String key, Intent value) {
        mValues.put(key, value == null ? null : value.toUri(0));
        return this;
    }

    public ContentWriter putIcon(Bitmap value, UserHandle user) {
        mIcon = value;
        mUser = user;
        return this;
    }

    /*
        public ContentWriter put(String key, UserHandle user) {
            return put(key, UserManagerCompat.getInstance(mContext).getSerialNumberForUser(user));
        }


        public ContentValues getValues(Context context) {
            Preconditions.assertNonUiThread();
            if (mIcon != null && !LauncherAppState.getInstance(context).getIconCache()
                    .isDefaultIcon(mIcon, mUser)) {
                mValues.put(LauncherSettings.Favorites.ICON, Utilities.flattenBitmap(mIcon));
                mIcon = null;
            }
            return mValues;
        }

        public int commit() {
            if (mCommitParams != null) {
                return mContext.getContentResolver().update(mCommitParams.mUri, getValues(mContext),
                        mCommitParams.mWhere, mCommitParams.mSelectionArgs);
            }
            return 0;
        }
    */
    public static final class CommitParams {

        //final Uri mUri = LauncherSettings.Favorites.CONTENT_URI;
        String mWhere;
        String[] mSelectionArgs;

        public CommitParams(String where, String[] selectionArgs) {
            mWhere = where;
            mSelectionArgs = selectionArgs;
        }

    }

}
