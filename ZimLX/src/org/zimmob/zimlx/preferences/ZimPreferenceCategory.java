/*
 * Copyright (C) 2020 Zim Launcher
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

package org.zimmob.zimlx.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import com.android.launcher3.Utilities;

import java.util.jar.Attributes;

public class ZimPreferenceCategory extends PreferenceCategory {
    private Context mContext;
    public ZimPreferenceCategory(Context context, AttributeSet attrs) {
        super(context);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder){
        super.onBindViewHolder(holder);
        TextView title =(TextView) holder.findViewById(android.R.id.title);
        title.setTextSize(16f);
        title.setTextColor(Utilities.getZimPrefs(mContext).getAccentColor());
    }


}
