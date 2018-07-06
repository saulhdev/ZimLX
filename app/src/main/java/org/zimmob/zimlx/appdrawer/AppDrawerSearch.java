package org.zimmob.zimlx.appdrawer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.SettingsActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DialogHelper;
import org.zimmob.zimlx.util.Tool;

public class AppDrawerSearch extends LinearLayout {
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
        LayoutParams buttonsParams = new LayoutParams(dp1 * 48, dp1 * 48);
        buttonsParams.weight = 0.1f;
        buttonsParams.setMargins(0, 0, dp1 * 5, 0);
        menuButton.setLayoutParams(buttonsParams);
        menuButton.setBackground(Setup.appContext().getResources().getDrawable(R.drawable.ic_more_horiz_black_16dp));
        menuButton.setOnClickListener(this::showDrawerMenu);

        //INIT SEARCH VIEW
        LayoutParams searchParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        searchParams.weight = 0.9f;
        searchView = new android.support.v7.widget.SearchView(context);
        searchView.setLayoutParams(searchParams);
        searchView.setQueryHint(context.getText(R.string.search_hint));
        searchView.setActivated(true);
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        EditText searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (drawerMode == Config.DRAWER_HORIZONTAL) {
                    //AppDrawerPaged.Adapter .filter(newText);
                } else {
                    //AppDrawerVertical.GridAppDrawerAdapter.filter(newText);
                }
                return false;
            }
        });

        //INIT LAYOUT
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp1 * 50);
        layoutParams.setMargins(0, dp1 * 1, 0, dp1 * 24);
        this.setLayoutMode(LinearLayout.HORIZONTAL);
        this.setWeightSum(1);
        this.setLayoutParams(layoutParams);
        this.addView(searchView);
        this.addView(menuButton);
    }

    private void showDrawerMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.drawer_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int menuItem = item.getItemId();
            switch (menuItem) {
                case R.id.menu_settings:
                    Intent settings = new Intent(context, SettingsActivity.class);
                    settings.putExtra("option", R.string.pref_key__cat_app_drawer);
                    context.startActivity(settings);
                    break;

                case R.id.menu_layout_mode:
                    break;

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
