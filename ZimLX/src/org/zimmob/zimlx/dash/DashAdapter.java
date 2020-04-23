/*
 * 2020 Zim Launcher
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
package org.zimmob.zimlx.dash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.launcher3.R;

import java.util.List;

public class DashAdapter extends BaseAdapter {

    private Context context;
    private List<DashItem> dashItems;

    public DashAdapter(Context context, List<DashItem> items) {
        this.context = context;
        this.dashItems = items;
    }

    public int getCount() {
        return dashItems.size();
    }

    public Object getItem(int item) {
        return dashItems.get(item);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ImageView iv;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_minibar, parent, false);
        } else {
            view = convertView;
        }

        iv = view.findViewById(R.id.iv);
        iv.setImageResource(dashItems.get(position).icon);
        return view;
    }
}

