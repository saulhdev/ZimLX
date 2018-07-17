package org.zimmob.zimlx.activity.homeparts;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.widget.CalendarDropDownView;
import org.zimmob.zimlx.widget.SearchBar;

public class HpSearchBar implements SearchBar.CallBack, View.OnClickListener {
    private HomeActivity _home;
    private SearchBar searchBar;
    private CalendarDropDownView _calendarDropDownView;

    /**
     * @param home
     * @param searchBar
     * @param calendarDropDownView
     */
    public HpSearchBar(HomeActivity home, SearchBar searchBar, CalendarDropDownView calendarDropDownView) {
        this._home = home;
        this.searchBar = searchBar;
        this._calendarDropDownView = calendarDropDownView;
    }


    public void initSearchBar() {
        searchBar.setCallback(this);
        searchBar.searchClock.setOnClickListener(this);
        _home.updateSearchClock();
    }

    @Override
    public void onInternetSearch(String string) {
        Intent intent = new Intent();
        if (Tool.isIntentActionAvailable(_home.getApplicationContext(), Intent.ACTION_WEB_SEARCH) && !Setup.appSettings().getSearchBarForceBrowser()) {
            intent.setAction(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, string);
        } else {
            String baseUri = Setup.appSettings().getSearchBarBaseURI();
            String searchUri = baseUri.contains("{query}") ? baseUri.replace("{query}", string) : (baseUri + string);

            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(searchUri));
        }
        try {
            _home.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onExpand() {
        _home.clearRoomForPopUp();
        _home.dimBackground();
        searchBar.searchInput.setFocusable(true);
        searchBar.searchInput.setFocusableInTouchMode(true);
        searchBar.searchInput.post(() -> searchBar.searchInput.requestFocus());
        Tool.showKeyboard(_home, searchBar.searchInput);
    }

    @Override
    public void onCollapse() {
        _home.getDesktop().postDelayed(() -> _home.unClearRoomForPopUp(), 100);
        _home.unDimBackground();
        searchBar.searchInput.clearFocus();
        Tool.hideKeyboard(_home, searchBar.searchInput);
    }

    @Override
    public void onClick(View v) {
        _calendarDropDownView.animateShow();
    }
}
