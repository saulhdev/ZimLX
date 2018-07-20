package org.zimmob.zimlx.viewutil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.icon.IconProvider;
import org.zimmob.zimlx.icon.SimpleIconProvider;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.Tool;

import java.util.List;

public class IconLabelItem extends AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> {
    public static final String TAG = "IconLabelItem";
    public Drawable _icon;
    public SimpleIconProvider _iconProvider = null;
    public String _label;
    @Nullable
    public String _searchInfo;
    private View.OnLongClickListener _onLongClickListener;
    private View.OnClickListener _onClickListener;

    private int _forceSize = -1;
    private int _iconGravity;
    private int _textColor = Color.DKGRAY;
    private int _gravity = android.view.Gravity.CENTER_VERTICAL;
    private float _drawablePadding;
    private Typeface _typeface;
    private boolean _matchParent = true;
    private int _width = -1;
    private boolean _bold = false;
    private int _textGravity = Gravity.CENTER_VERTICAL;
    private int _maxTextLines = 1;
    private boolean hideLabel = false;

    public IconLabelItem(Item item) {
        _iconProvider = item != null ? item.getIconProvider() : null;
        _icon = item != null ? item.getIcon() : null;
        _label = item != null ? item.getLabel() : null;
    }

    public IconLabelItem(Context context, int icon, int label) {
        _iconProvider = Setup.imageLoader().createIconProvider(icon);
        if (icon != 0) {
            _icon = context.getDrawable(icon);
        }
        _label = context.getString(label);
    }

    public void setHideLabel(boolean hideLabel) {
        this.hideLabel = hideLabel;
    }

    public IconLabelItem(Context context, int icon, String label, int forceSize) {
        this(null);
        _label = label;
        _icon = context.getDrawable(icon);
        _iconProvider = Setup.imageLoader().createIconProvider(icon);
        _forceSize = forceSize;
    }

    public IconLabelItem(Context context, Drawable icon, String label, int forceSize) {
        _label = label;
        _icon = icon;
        _iconProvider = Setup.imageLoader().createIconProvider(icon);
        _forceSize = forceSize;
    }

    public IconLabelItem(Context context, SimpleIconProvider iconProvider, String label, @Nullable String searchInfo, int forceSize) {
        _label = label;
        _iconProvider = iconProvider;
        _forceSize = forceSize;
        _searchInfo = searchInfo;
    }

    public IconLabelItem(Context context, Drawable icon, String label, @Nullable String searchInfo, int forceSize) {
        this(context, icon, label, forceSize);
        _searchInfo = searchInfo;
    }

    public IconLabelItem withIconGravity(int iconGravity) {
        _iconGravity = iconGravity;
        return this;
    }

    public IconLabelItem withIconPadding(Context context, int drawablePadding) {
        _drawablePadding = Tool.dp2px(drawablePadding, context);
        return this;
    }

    public IconLabelItem withTextColor(int textColor) {
        _textColor = textColor;
        return this;
    }

    public IconLabelItem withBold(boolean bold) {
        _bold = bold;
        return this;
    }

    public IconLabelItem withTypeface(Typeface typeface) {
        _typeface = typeface;
        return this;
    }

    public IconLabelItem withGravity(int gravity) {
        _gravity = gravity;
        return this;
    }

    public IconLabelItem withTextGravity(int textGravity) {
        _textGravity = textGravity;
        return this;
    }

    public IconLabelItem withMatchParent(boolean matchParent) {
        _matchParent = matchParent;
        return this;
    }

    public IconLabelItem withWidth(int width) {
        _width = width;
        return this;
    }

    public IconLabelItem withOnClickListener(@Nullable View.OnClickListener listener) {
        _onClickListener = listener;
        return this;
    }

    public IconLabelItem withMaxTextLines(int maxTextLines) {
        _maxTextLines = maxTextLines;
        return this;
    }

    public IconLabelItem withOnLongClickListener(@Nullable View.OnLongClickListener onLongClickListener) {
        _onLongClickListener = onLongClickListener;
        return this;
    }

    public void setIcon(Context context, int icon) {
        this._icon = context.getResources().getDrawable(icon);
    }

    public void setIcon(int resId) {
        _iconProvider = Setup.imageLoader().createIconProvider(resId);
    }

    public void setIcon(Drawable icon) {
        _icon = icon;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v, this);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_icon_label;
    }

    @Override
    public int getType() {
        return R.id.id_adapter_icon_label_item;
    }

    @Override
    public void bindView(IconLabelItem.ViewHolder holder, List payloads) {
        if (_matchParent)
            holder.itemView.getLayoutParams().width = RecyclerView.LayoutParams.MATCH_PARENT;
        if (_width != -1)
            holder.itemView.getLayoutParams().width = _width;
        holder.textView.setMaxLines(_maxTextLines);
        if (_label != null)
            holder.textView.setText(_maxTextLines != 0 ? _label : "");
        holder.textView.setGravity(_gravity);
        holder.textView.setGravity(_textGravity);
        holder.textView.setCompoundDrawablePadding((int) _drawablePadding);
        if (hideLabel) {
            holder.textView.setText(null);
            _iconProvider.loadIconIntoTextView(holder.textView, _forceSize, Gravity.TOP);
        } else {
            _iconProvider.loadIconIntoTextView(holder.textView, _forceSize, _iconGravity);
        }
        holder.textView.setTypeface(_typeface);
        if (_bold)
            holder.textView.setTypeface(Typeface.DEFAULT_BOLD);
        holder.textView.setTextColor(_textColor);
        if (_onClickListener != null)
            holder.itemView.setOnClickListener(_onClickListener);
        if (_onLongClickListener != null)
            holder.itemView.setOnLongClickListener(_onLongClickListener);
        super.bindView(holder, payloads);
    }

    @Override
    public void unbindView(@NonNull ViewHolder holder) {
        super.unbindView(holder);
        if (_iconProvider != null) {
            _iconProvider.cancelLoad(IconProvider.IconTargetType.TextView, holder.textView);
        }
        holder.textView.setText("");
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView, IconLabelItem item) {
            super(itemView);
            textView = (TextView) itemView;
            textView.setTag(item);
        }
    }
}
