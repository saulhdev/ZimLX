package org.zimmob.zimlx.activity.homeparts;

import android.view.View;

import net.gsantner.opoc.util.Callback;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.pageindicator.PageIndicator;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.widget.AppDrawerController;
import org.zimmob.zimlx.widget.DragOptionView;

import static org.zimmob.zimlx.config.Config.DRAWER_HORIZONTAL;
import static org.zimmob.zimlx.config.Config.DRAWER_VERTICAL;

public class HpAppDrawer implements Callback.a2<Boolean, Boolean> {
    private HomeActivity _homeActivity;
    private PageIndicator _appDrawerIndicator;
    private DragOptionView _dragOptionPanel;

    public HpAppDrawer(HomeActivity home, PageIndicator appDrawerIndicator, DragOptionView dragOptionPanel) {
        _homeActivity = home;
        _appDrawerIndicator = appDrawerIndicator;
        _dragOptionPanel = dragOptionPanel;
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
                Tool.visibleViews(_appDrawerIndicator);
                Tool.invisibleViews(_homeActivity.getDesktop());
                _homeActivity.hideDesktopIndicator();
                _homeActivity.updateDock(false);
                _homeActivity.updateSearchBar(false);
            }
        } else {
            if (startOrEnd) {
                Tool.invisibleViews(_appDrawerIndicator);
                Tool.visibleViews(_homeActivity.getDesktop());
                _homeActivity.showDesktopIndicator();
                if (Setup.appSettings().getDrawerStyle() == Config.DRAWER_HORIZONTAL)
                    _homeActivity.updateDock(true, 200);
                else{
                    _homeActivity.updateDock(true);
                _homeActivity.updateSearchBar(!_dragOptionPanel._isDraggedFromDrawer);
                _dragOptionPanel._isDraggedFromDrawer = false;
                }
            } else {
                if (!Setup.appSettings().isDrawerRememberPosition()) {
                    _homeActivity.getAppDrawerController().scrollToStart();
                }
                _homeActivity.getAppDrawerController().getDrawer().setVisibility(View.INVISIBLE);
            }
        }
    }
}
