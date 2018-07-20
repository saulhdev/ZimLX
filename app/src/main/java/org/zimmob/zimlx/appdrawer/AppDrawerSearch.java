package org.zimmob.zimlx.appdrawer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.activity.SettingsActivity;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DialogHelper;
import org.zimmob.zimlx.util.Tool;

import java.util.Objects;
import java.util.logging.Logger;

import static org.zimmob.zimlx.config.Config.DRAWER_HORIZONTAL;
import static org.zimmob.zimlx.config.Config.DRAWER_VERTICAL;

public class AppDrawerSearch extends LinearLayout {
    private final Logger LOG = Logger.getLogger(AppDrawerSearch.class.getName());

    private SearchView searchView;
    private Button menuButton;
    private Context context;
    int drawerMode = 0;

    public AppDrawerSearch(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public AppDrawerSearch(@NonNull Context context, AttributeSet attrs) {
        super(context);
        init();
    }

    public void init() {
        int dp1 = Tool.dp2px(1, getContext());
        drawerMode = AppSettings.get().getDrawerStyle();

        //INIT BUTTON
        menuButton = new Button(context);
        LayoutParams buttonsParams = new LayoutParams(dp1 * 40, dp1 * 40);
        buttonsParams.weight = 0.1f;
        buttonsParams.setMargins(0, 0, dp1 * 5, 0);
        menuButton.setLayoutParams(buttonsParams);
        menuButton.setBackground(Setup.appContext().getResources().getDrawable(R.drawable.ic_more_horiz_black_16dp));
        menuButton.setOnClickListener(this::showDrawerMenu);

        //INIT SEARCH VIEW
        LayoutParams searchParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        searchParams.weight = 0.9f;
        searchView = new SearchView(context);
        searchView.setLayoutParams(searchParams);
        searchView.setQueryHint(context.getText(R.string.search_hint));
        searchView.setActivated(true);
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        EditText searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));

        //TODO: Implement App Drawer Search in both vertical and horizontal;
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (drawerMode == DRAWER_HORIZONTAL) {
                    AppDrawerPaged.Companion.FilterApps(s);
                } else {
                    AppDrawerVertical.Filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //INIT LAYOUT
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp1 * 40);
        layoutParams.setMargins(0, dp1 * 1, 0, dp1 * 1);
        this.setLayoutMode(LinearLayout.HORIZONTAL);
        this.setWeightSum(1);
        this.setLayoutParams(layoutParams);
        this.addView(searchView);
        this.addView(menuButton);
        this.setId(R.id.id_search_layout);
    }

    private void showDrawerMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.drawer_menu, popupMenu.getMenu());
        if (Setup.appSettings().getDrawerStyle() == DRAWER_HORIZONTAL) {
            popupMenu.getMenu().getItem(1).setTitle(R.string.horizontal_paged_drawer);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            int menuItem = item.getItemId();
            switch (menuItem) {
                case R.id.menu_settings:
                    //TODO: Open App Drawer Setting instead of general setting
                    Intent settings = new Intent(context, SettingsActivity.class);
                    context.startActivity(settings);
                    break;

                case R.id.menu_layout_mode:
                    //TODO: Autorefresh drawer on layout change
                    if (Setup.appSettings().getDrawerStyle() == DRAWER_HORIZONTAL) {
                        Setup.appSettings().setDrawerStyle(DRAWER_VERTICAL);
                        item.setTitle(R.string.horizontal_paged_drawer);
                    } else {
                        Setup.appSettings().setDrawerStyle(DRAWER_HORIZONTAL);
                        item.setTitle(R.string.vertical_scroll_drawer);
                        PendingIntent restartIntentP = PendingIntent.getActivity(context, 123556, new Intent(context, HomeActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Objects.requireNonNull(mgr).set(AlarmManager.RTC, 1000, restartIntentP);
                    }

                    break;

                //TODO: Remover opciones sin programar
                case R.id.menu_hidden_apps:
                    break;

                case R.id.menu_add_apps_home:
                    break;
                case R.id.menu_sort_mode:
                    DialogHelper.sortModeDialog(getContext());
                    break;
            }

            return false;
        });
        popupMenu.show();
    }
}
