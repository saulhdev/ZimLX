package org.zimmob.zimlx.icon;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppSettings;

import butterknife.BindView;

public class EditIconActivity extends AppCompatActivity {
    private static final String TAG = EditIconActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    private EditText title;
    private Switch visibility;
    private HomeActivity launcher;
    private Context context;
    private Item mInfo;
    private IconProvider iconProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_icon);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(AppSettings.get().getPrimaryColor());
        toolbar.setTitle(R.string.edit_icon);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mInfo = getIntent().getExtras().getParcelable("itemInfo");
        title = findViewById(R.id.title);
        title.setText(mInfo.getLabel());

        TextView packageName = findViewById(R.id.package_name);
        packageName.setText(mInfo.getPackageName());
        visibility = findViewById(R.id.visibility);

        ImageView icon = findViewById(R.id.icon);
        iconProvider = mInfo.getIconProvider();
        if (mInfo.getType() == Item.Type.APP) {
            App myApp = Setup.appLoader().findItemApp(mInfo);
            icon.setImageDrawable(myApp.getIcon());
        } else {
            icon.setImageDrawable(mInfo.getIcon());
        }
        ImageButton reset = findViewById(R.id.reset_title);
        reset.setOnClickListener(v -> title.setText(mInfo.getLabel()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
