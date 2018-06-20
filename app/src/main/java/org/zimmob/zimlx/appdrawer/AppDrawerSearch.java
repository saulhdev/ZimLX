package org.zimmob.zimlx.appdrawer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.util.DialogHelper;

public class AppDrawerSearch extends LinearLayout {
    private SearchView searchView;
    private Button menuButton;
    private Context context;
    private LinearLayout linearLayout;
    private OnClickListener menuListener;

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
        menuButton = findViewById(R.id.menu_button);
        searchView = findViewById(R.id.search_view);
    }

    public void setMenuButtonListener(){
        menuButton.setOnClickListener(this::showDrawerMenu);
    }

    public OnClickListener getMenuListener() {
        return menuListener;
    }

    private void showDrawerMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.drawer_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int menuItem = item.getItemId();
            switch (menuItem) {
                case R.id.menu_settings:
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
