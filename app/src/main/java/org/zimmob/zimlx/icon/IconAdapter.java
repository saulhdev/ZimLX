package org.zimmob.zimlx.icon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.util.AppSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class IconAdapter extends BaseAdapter {
    ArrayList<IconPackInfo> mSupportedPackages;
    LayoutInflater mLayoutInflater;
    String mCurrentIconPack;

    IconAdapter(Context context, Map<String, IconPackInfo> supportedPackages) {
        mLayoutInflater = LayoutInflater.from(context);
        mSupportedPackages = new ArrayList<>(supportedPackages.values());
        Collections.sort(mSupportedPackages, (lhs, rhs) ->
                lhs.label.toString().compareToIgnoreCase(rhs.label.toString()));

        Resources res = context.getResources();

        Drawable icon = res.getDrawable(android.R.mipmap.sym_def_app_icon);
        String defaultLabel = res.getString(R.string.icon_pack_system);

        mSupportedPackages.add(0, new IconPackInfo(defaultLabel, icon, defaultLabel));
        mCurrentIconPack = AppSettings.get().getIconPack();
    }

    @Override
    public int getCount() {
        return mSupportedPackages.size();
    }

    @Override
    public String getItem(int position) {
        return mSupportedPackages.get(position).packageName;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.target_edit_iconpack_chooser, null);
        }
        IconPackInfo info = mSupportedPackages.get(position);
        TextView txtView = (TextView) convertView.findViewById(R.id.title);
        txtView.setText(info.label);
        ImageView imgView = (ImageView) convertView.findViewById(R.id.icon);
        imgView.setImageDrawable(info.icon);
        RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.radio);
        radioButton.setChecked(info.packageName.equals(mCurrentIconPack));
        return convertView;
    }
}
