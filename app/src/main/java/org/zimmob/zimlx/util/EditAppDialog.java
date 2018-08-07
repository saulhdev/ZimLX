package org.zimmob.zimlx.util;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.icon.IconProvider;
import org.zimmob.zimlx.model.Item;

public class EditAppDialog extends HomeActivity.LauncherDialog implements View.OnClickListener {
    private EditText title;
    private Switch visibility;
    private HomeActivity launcher;
    private Context context;
    private Item item;
    private IconProvider iconProvider;

    public EditAppDialog(@NonNull Context context, Item item, HomeActivity launcher) {
        super(context);
        this.launcher = launcher;
        this.context = context;
        this.item = item;
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_edit_dialog);
        title = findViewById(R.id.title);
        title.setText(item.getLabel());

        TextView packageName = findViewById(R.id.package_name);
        packageName.setText(item.getPackageName());
        visibility = findViewById(R.id.visibility);

        ImageView icon = findViewById(R.id.icon);
        iconProvider = item.getIconProvider();

        if (iconProvider != null)
            icon.setImageDrawable(iconProvider.scaleDrawable(item.icon, 64));

        else
            icon.setImageDrawable(item.icon);
        icon.setOnClickListener(view -> launcher.startEditIcon());

        ImageButton reset = findViewById(R.id.reset_title);
        reset.setOnClickListener(v -> EditAppDialog.this.title.setText(item.getLabel()));

    }

    @Override
    public void onResume() {
        ImageView icon = findViewById(R.id.icon);
        icon.setImageDrawable(item.getIconProvider().scaleDrawable(item.icon, 64));

        TextView packageName = findViewById(R.id.package_name);
        packageName.setText(item.getPackageName());
        visibility = findViewById(R.id.visibility);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View view) {

    }
}
