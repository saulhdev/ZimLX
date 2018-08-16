package org.zimmob.zimlx.icon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.compat.LauncherActivityInfoCompat;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;

public class EditIconActivity extends AppCompatActivity implements CustomIconAdapter.Listener {
    private static final String TAG = EditIconActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    private EditText title;
    private Switch visibility;
    private Button buttonApply;
    private Button buttonPlayStore;
    private HomeActivity launcher;
    private Context context;
    private Item mInfo;
    private IconProvider iconProvider;
    private ArrayList<String> listActivitiesHidden = new ArrayList();
    private App myApp;
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

        buttonApply = findViewById(R.id.button_apply);
        buttonApply.setOnClickListener(v -> {
            saveChanges();
        });

        buttonPlayStore = findViewById(R.id.play_store);
        buttonPlayStore.setOnClickListener(v -> {
            openPlayStore();
        });

        ImageView icon = findViewById(R.id.icon);
        iconProvider = mInfo.getIconProvider();
        if (mInfo.getType() == Item.Type.APP) {
            myApp = Setup.appLoader().findItemApp(mInfo);
            icon.setImageDrawable(myApp.getIconProvider().drawable);
        } else {
            icon.setImageDrawable(mInfo.getIcon());
        }
        ImageButton reset = findViewById(R.id.reset_title);
        reset.setOnClickListener(v -> title.setText(mInfo.getLabel()));
        /*ICON PACKS*/
        IconPackHandler iconPack = new IconPackHandler(this);
        ArrayList<IconPackInfo> iconPacks = new ArrayList(iconPack.getIconPacks().values());
        Collections.sort(iconPacks, (lhs, rhs) -> lhs.label.toString().compareToIgnoreCase(rhs.label.toString()));
        IconPackAdapterX iconPackAdapter = new IconPackAdapterX(AppSettings.get().getContext(), iconPacks);
        RecyclerView iconPackRecyclerView = findViewById(R.id.openIconPack);
        iconPackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        iconPackRecyclerView.setAdapter(iconPackAdapter);

        /*ICONS*/
        if (mInfo.getType() == Item.Type.APP) {
            ComponentName componentName = new ComponentName(mInfo.getPackageName(), mInfo.getClassName());
            Log.e(TAG, "Componente: " + componentName);
            UserHandle user = Utilities.myUserHandle();
            if (mInfo.getLabel() != null) {
                RecyclerView iconRecyclerView = findViewById(R.id.chooseIcon);
                Intent i = new Intent(Intent.ACTION_MAIN).setComponent(componentName);
                LauncherActivityInfoCompat laic = LauncherActivityInfoCompat.create(this, user, i);
                //CustomIconAdapter iconAdapter = new CustomIconAdapter(this, laic, iconPacks);
                //iconRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                //iconRecyclerView.setAdapter(iconAdapter);
            } else {
                findViewById(R.id.chooseIcon).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onSelect(CustomIconAdapter.IconInfo iconInfo) {
        setAlternateIcon(iconInfo.toString());
    }

    private void setAlternateIcon(String alternateIcon) {
        Intent data = new Intent();
        data.putExtra("alternateIcon", alternateIcon);
        setResult(RESULT_OK, data);
        finish();
    }

    private void resetIcon() {
        Intent data = new Intent();
        data.putExtra("alternateIcon", "-1");
        setResult(RESULT_OK, data);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void openPlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/search?q=iconpack&c=apps"));
        startActivity(intent);
    }

    private void saveChanges() {
        if (visibility.isChecked()) {
            listActivitiesHidden.add(myApp.getComponentName());
        }
        if (listActivitiesHidden.size() > 0)
            confirmSelection();
        onBackPressed();
    }

    private void confirmSelection() {
        Thread actionSend_Thread = new Thread() {
            @Override
            public void run() {
                AppSettings.get().setHiddenAppsList(listActivitiesHidden);
            }
        };
        if (!actionSend_Thread.isAlive()) {
            actionSend_Thread.start();
        }
    }
}
