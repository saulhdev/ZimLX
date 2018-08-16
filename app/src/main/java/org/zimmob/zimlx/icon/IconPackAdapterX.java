package org.zimmob.zimlx.icon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.zimmob.zimlx.R;

import java.util.List;

class IconPackAdapterX extends RecyclerView.Adapter<IconPackAdapterX.Holder> {

    private final Context mContext;
    private final List<IconPackInfo> mIconPacks;
    private Listener mListener;

    IconPackAdapterX(Context context, List<IconPackInfo> iconPacks) {
        mContext = context;
        mIconPacks = iconPacks;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.icon_pack_item, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(mIconPacks.get(position));
    }

    @Override
    public int getItemCount() {
        return mIconPacks.size();
    }

    private void onSelect(int position) {
        if (mListener == null) return;

        mListener.startPicker(mIconPacks.get(position));
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    interface Listener {

        void startPicker(IconPackInfo iconPackInfo);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mIcon;
        private final TextView mTitle;

        Holder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mIcon = itemView.findViewById(android.R.id.icon);
            mTitle = itemView.findViewById(android.R.id.title);
        }

        public void bind(IconPackInfo iconPackInfo) {
            mIcon.setImageDrawable(iconPackInfo.icon);
            mTitle.setText(iconPackInfo.label);
        }

        @Override
        public void onClick(View view) {
            onSelect(getAdapterPosition());
        }
    }
}
