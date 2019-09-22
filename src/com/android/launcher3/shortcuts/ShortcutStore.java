/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.shortcuts;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

import com.android.launcher3.LauncherModel;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortcutStore implements Callback {

    public final Handler mWorkerThread = new Handler(LauncherModel.getWorkerLooper(), this);
    private final Handler mUiThread = new Handler(Looper.getMainLooper(), this);

    private static ShortcutStore sShortcutStore;
    public Map<ShortcutKey, ShortcutInfo> mComponentToShortcutMap;
    public OnUpdateListener mUpdateListeners;
    private Context mContext;

    private class Store {

        public ShortcutKey mShortcutKey;
        public ShortcutInfo mShortcutInfo;

        public Store(ShortcutKey shortcutKey, ShortcutInfo shortcutInfo) {
            mShortcutKey = shortcutKey;
            mShortcutInfo = shortcutInfo;
        }
    }

    public static synchronized ShortcutStore getInstance(Context context) {
        synchronized (ShortcutStore.class) {
            Preconditions.assertUIThread();
            if (sShortcutStore == null) {
                sShortcutStore = new ShortcutStore(context.getApplicationContext());
            }
        }
        return sShortcutStore;
    }

    private ShortcutStore(Context context) {
        mContext = context;
        mComponentToShortcutMap = new HashMap();
    }

    public boolean handleMessage(Message message) {
        if (message.what == 0) {
            List<ShortcutKey> keys = message.obj != null ? (List) message.obj : Collections.EMPTY_LIST;
            ArrayList transactionList = new ArrayList();
            for (ShortcutKey shortcutKey : keys) {
                ShortcutInfo shortcutInfo = getInfo(shortcutKey);
                if (shortcutInfo != null) {
                    transactionList.add(new Store(shortcutKey, shortcutInfo));
                }
            }
            Message.obtain(mUiThread, 1, transactionList).sendToTarget();
            return true;
        } else if (message.what != 1) {
            return false;
        } else {
            List<Store> items = (List) message.obj;
            mComponentToShortcutMap.clear();
            for (Store store : items) {
                mComponentToShortcutMap.put(store.mShortcutKey, store.mShortcutInfo);
            }
            if (mUpdateListeners != null) {
                mUpdateListeners.onShortcutsUpdated();
            }
            return true;
        }
    }

    private ShortcutInfo getInfo(ShortcutKey shortcutKey) {
        LauncherIcons obtain;
        List queryForFullDetails = DeepShortcutManager.getInstance(mContext).queryForFullDetails(shortcutKey.componentName.getPackageName(), Collections.singletonList(shortcutKey.getId()), shortcutKey.user);
        if (queryForFullDetails.isEmpty()) {
            return null;
        }
        ShortcutInfo shortcutInfo = new ShortcutInfo((ShortcutInfoCompat) queryForFullDetails.get(0), mContext);
        try {
            obtain = LauncherIcons.obtain(mContext);
            try {
                obtain.createShortcutIcon((ShortcutInfoCompat) queryForFullDetails.get(0), true, null).applyTo(shortcutInfo);
                if (obtain != null) {
                    obtain.close();
                }
                return shortcutInfo;
            } catch (Throwable ignored) {
            }
        } catch (Exception unused) {
            return null;
        }
        if (obtain != null) {
            try {
                obtain.close();
            } catch (Throwable ignored) {
            }
        }
        return shortcutInfo;
    }

    public interface OnUpdateListener {
        void onShortcutsUpdated();
    }
}
