package org.zimmob.zimlx.activity.homeparts;

import android.view.View;
import android.widget.LinearLayout;

import net.gsantner.opoc.util.Callback;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.widget.AppDrawerController;
import org.zimmob.zimlx.widget.DragOptionView;
import org.zimmob.zimlx.widget.PagerIndicator;

import static org.zimmob.zimlx.config.Config.DRAWER_HORIZONTAL;
import static org.zimmob.zimlx.config.Config.DRAWER_VERTICAL;

public class HpAppDrawer implements Callback.a2<Boolean, Boolean> {
    private HomeActivity _home;
    private PagerIndicator _appDrawerIndicator;
    private DragOptionView _dragOptionPanel;
    //private LinearLayout _searchapps;

    public HpAppDrawer(HomeActivity home, PagerIndicator appDrawerIndicator, DragOptionView dragOptionPanel) {
        _home = home;
        _appDrawerIndicator = appDrawerIndicator;
        _dragOptionPanel = dragOptionPanel;
        //_searchapps  = _home.findViewById(R.id.search_apps);
    }

    public void initAppDrawer(AppDrawerController appDrawerController) {
        appDrawerController.setCallBack(this);
        AppSettings appSettings = Setup.appSettings();

        appDrawerController.setBackgroundColor(appSettings.getDrawerBackgroundColor());
        appDrawerController.getBackground().setAlpha(0);
        appDrawerController.reloadDrawerCardTheme();

        switch (appSettings.getDrawerStyle()) {
            case DRAWER_HORIZONTAL: {
                if (!appSettings.isDrawerShowIndicator()) {
                    appDrawerController.getChildAt(1).setVisibility(View.GONE);
                }
                break;
            }
            case DRAWER_VERTICAL: {
                // handled in the AppDrawerVertical class
                break;
            }
        }
    }

    @Override
    public void callback(Boolean openingOrClosing, Boolean startOrEnd) {
        if (openingOrClosing) {
            if (startOrEnd) {
                //Tool.visibleViews(_searchapps);
                Tool.visibleViews(_appDrawerIndicator);
                Tool.invisibleViews(_home.getDesktop());
                _home.hideDesktopIndicator();
                _home.updateDock(false);
                _home.updateSearchBar(false);
            }
        } else {
            if (startOrEnd) {
                Tool.invisibleViews(_appDrawerIndicator);
                //Tool.invisibleViews(_searchapps);
                Tool.visibleViews(_home.getDesktop());
                _home.showDesktopIndicator();
                if (Setup.appSettings().getDrawerStyle() == Config.DRAWER_HORIZONTAL)
                    _home.updateDock(true, 200);
                else
                    _home.updateDock(true);
                _home.updateSearchBar(!_dragOptionPanel._isDraggedFromDrawer);
                _dragOptionPanel._isDraggedFromDrawer = false;
            } else {
                if (!Setup.appSettings().isDrawerRememberPosition()) {
                    _home.getAppDrawerController().scrollToStart();
                }
                _home.getAppDrawerController().getDrawer().setVisibility(View.INVISIBLE);
            }
        }
    }
}
