package org.zimmob.zimlx.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.gsantner.opoc.util.ContextUtils;

import org.zimmob.zimlx.BuildConfig;
import org.zimmob.zimlx.DeviceProfile;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.homeparts.HpAppDrawer;
import org.zimmob.zimlx.activity.homeparts.HpDesktopPickAction;
import org.zimmob.zimlx.activity.homeparts.HpInitSetup;
import org.zimmob.zimlx.activity.homeparts.HpSearchBar;
import org.zimmob.zimlx.appdrawer.AppDrawerController;
import org.zimmob.zimlx.apps.AppManager;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.folder.Folder;
import org.zimmob.zimlx.icon.EditIconActivity;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.manager.Setup.DataManager;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.pageindicator.PageIndicator;
import org.zimmob.zimlx.popup.PopupMenuItems;
import org.zimmob.zimlx.receiver.AppUpdateReceiver;
import org.zimmob.zimlx.receiver.ShortcutReceiver;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DialogHelper;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.util.Utilities;
import org.zimmob.zimlx.viewutil.MinibarAdapter;
import org.zimmob.zimlx.viewutil.WidgetHost;
import org.zimmob.zimlx.widget.AppItemView;
import org.zimmob.zimlx.widget.CalendarDropDownView;
import org.zimmob.zimlx.widget.CellContainer;
import org.zimmob.zimlx.widget.Desktop;
import org.zimmob.zimlx.widget.Desktop.OnDesktopEditListener;
import org.zimmob.zimlx.widget.DesktopOptionView;
import org.zimmob.zimlx.widget.Dock;
import org.zimmob.zimlx.widget.DragOptionLayout;
import org.zimmob.zimlx.widget.DragOptionView;
import org.zimmob.zimlx.widget.Minibar;
import org.zimmob.zimlx.widget.Minibar.Action;
import org.zimmob.zimlx.widget.SearchBar;
import org.zimmob.zimlx.widget.SmoothViewPager;
import org.zimmob.zimlx.widget.SwipeListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class HomeActivity extends Activity implements OnDesktopEditListener, DesktopOptionView.DesktopOptionViewListener, DrawerLayout.DrawerListener, DialogInterface.OnDismissListener {
    public static final String TAG = "Launcher";
    public static final Companion companion = new Companion();
    public static final int REQUEST_PERMISSION_STORAGE_ACCESS = 666;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    public static final int REQUEST_PICK_APPWIDGET = 0x2678;
    public static final int REQUEST_EDIT_ICON = 14;
    private static final int REQUEST_BIND_APPWIDGET = 1;
    private static Resources resources;

    private static WidgetHost _appWidgetHost;
    private static AppWidgetManager _appWidgetManager;
    private static boolean _consumeNextResume;

    public static Setup.DataManager _db;
    public static float _itemTouchX;
    public static float _itemTouchY;

    private static HomeActivity launcher;
    private static final IntentFilter _appUpdateIntentFilter = new IntentFilter();
    private static final IntentFilter _shortcutIntentFilter = new IntentFilter();
    private static final IntentFilter _timeChangesIntentFilter = new IntentFilter();
    private final AppUpdateReceiver _appUpdateReceiver = new AppUpdateReceiver();
    private int cx;
    private int cy;
    private int rad;
    private final ShortcutReceiver _shortcutReceiver = new ShortcutReceiver();
    private BroadcastReceiver _timeChangedReceiver;
    private AppSettings appSettings;

    protected DeviceProfile mDeviceProfile;

    static {
        companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIME_TICK");
        companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIMEZONE_CHANGED");
        companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIME_SET");
        companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_ADDED");
        companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_REMOVED");
        companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_CHANGED");
        companion.getAppUpdateIntentFilter().addDataScheme("package");
        companion.getShortcutIntentFilter().addAction("com.android.launcher.action.INSTALL_SHORTCUT");
    }

    private LauncherDialog mCurrentDialog;
    private boolean mHasFocus;

    protected void onCreate(Bundle savedInstanceState) {
        companion.setResources(getResources());
        ContextUtils contextUtils = new ContextUtils(getApplicationContext());
        appSettings = AppSettings.get();

        contextUtils.setAppLanguage(appSettings.getLanguage());
        super.onCreate(savedInstanceState);
        /*LauncherAppState app = LauncherAppState.getInstance(this);

        // Load configuration-specific DeviceProfile
        mDeviceProfile = app.getInvariantDeviceProfile().getDeviceProfile(this);
        if (isInMultiWindowModeCompat()) {
            Display display = getWindowManager().getDefaultDisplay();
            Point mwSize = new Point();
            display.getSize(mwSize);
            mDeviceProfile = mDeviceProfile.getMultiWindowProfile(this, mwSize);
        }
        */
        if (!Setup.wasInitialised()) {
            Setup.init(new HpInitSetup(this));
        }
        if (appSettings.isSearchBarTimeEnabled()) {
            _timeChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_TIME_TICK)) {
                        updateSearchClock();
                    }
                }
            };
        }
        companion.setLauncher(this);
        DataManager dataManager = Setup.dataManager();

        companion.setDb(dataManager);
        setContentView(getLayoutInflater().inflate(R.layout.activity_home, null));
        Window window = getWindow();
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);


        init();
    }

    protected void onResume() {
        super.onResume();
        AppSettings appSettings = Setup.appSettings();
        boolean rotate = false;
        if (appSettings.getAppRestartRequired()) {
            appSettings.setAppRestartRequired(false);
            PendingIntent restartIntentP = PendingIntent.getActivity(this, 123556, new Intent(this, HomeActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Objects.requireNonNull(mgr).set(AlarmManager.RTC, System.currentTimeMillis() + ((long) 100), restartIntentP);
            System.exit(0);
            return;
        }
        companion.setLauncher(this);
        WidgetHost appWidgetHost = companion.getAppWidgetHost();
        if (appWidgetHost != null) {
            appWidgetHost.startListening();
        }
        Intent intent = getIntent();
        handleLauncherPause(Intent.ACTION_MAIN.equals(intent.getAction()));
        boolean user = AppSettings.get().getBool(R.string.pref_key__desktop_rotate, false);
        boolean system = false;
        try {
            system = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "Unable to read settings", e);
        }
        if (mCurrentDialog != null) {
            mCurrentDialog.onResume();
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            rotate = system;
        } else if (user && system) {
            rotate = true;
        }
        if (rotate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public final void openAppDrawer() {
        openAppDrawer$default(this, null, 0, 0, 7, null);
    }

    public final void openAppDrawer(@Nullable View view) {
        openAppDrawer$default(this, view, 0, 0, 6, null);
    }

    public static void openAppDrawer$default(HomeActivity homeActivity, View view, int i, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            view = homeActivity.findViewById(R.id.desktop);
        }
        if ((i3 & 2) != 0) {
            i = -1;
        }
        if ((i3 & 4) != 0) {
            i2 = -1;
        }
        homeActivity.openAppDrawer(view, i, i2);
    }

    public final void openAppDrawer(@Nullable View view, int x, int y) {
        if (!(x > 0 && y > 0)) {
            int[] pos = new int[2];
            view.getLocationInWindow(pos);
            cx = pos[0];
            cy = pos[1];

            cx += view.getWidth() / 2f;
            cy += view.getHeight() / 2f;
            if (view instanceof AppItemView) {
                AppItemView appItemView = (AppItemView) view;
                if (appItemView.getShowLabel()) {
                    cy -= Tool.dp2px(14, this) / 2f;
                }
                rad = (int) (appItemView.getIconSize() / 2f - Tool.toPx(4));
            }
            cx -= ((MarginLayoutParams) getAppDrawerController().getDrawer().getLayoutParams()).getMarginStart();
            cy -= ((MarginLayoutParams) getAppDrawerController().getDrawer().getLayoutParams()).topMargin;
            cy -= getAppDrawerController().getPaddingTop();
        } else {
            cx = x;
            cy = y;
            rad = 0;
        }
        int finalRadius = Math.max(getAppDrawerController().getDrawer().getWidth(), getAppDrawerController().getDrawer().getHeight());
        getAppDrawerController().open(cx, cy, rad, finalRadius);
    }

    public final void updateDock(boolean z) {
        updateDock$default(this, z, 0, 2, null);
    }

    private static void updateDock$default(HomeActivity home, boolean z, long j, int i, Object obj) {
        if ((i & 2) != 0) {
            j = 0;
        }
        home.updateDock(z, j);
    }

    public final void updateDock(boolean show, long delay) {
        AppSettings appSettings = Setup.appSettings();
        Desktop desktop;
        LayoutParams layoutParams;
        PageIndicator pagerIndicator;
        if (appSettings.getDockEnable() && show) {
            Tool.visibleViews(100, delay, (Dock) findViewById(R.id.dock));
            desktop = findViewById(R.id.desktop);
            layoutParams = desktop.getLayoutParams();
            ((MarginLayoutParams) layoutParams).bottomMargin = Tool.dp2px(4, this);
            pagerIndicator = findViewById(R.id.desktopIndicator);
            layoutParams = pagerIndicator.getLayoutParams();
            ((MarginLayoutParams) layoutParams).bottomMargin = Tool.dp2px(4, this);
        } else {
            appSettings = Setup.appSettings();

            if (appSettings.getDockEnable()) {
                Tool.invisibleViews(100, (Dock) findViewById(R.id.dock));
            } else {
                Tool.goneViews(100, (Dock) findViewById(R.id.dock));
                pagerIndicator = findViewById(R.id.desktopIndicator);
                layoutParams = pagerIndicator.getLayoutParams();
                ((MarginLayoutParams) layoutParams).bottomMargin = Desktop._bottomInset + Tool.dp2px(4, this);
                desktop = findViewById(R.id.desktop);
                layoutParams = desktop.getLayoutParams();
                ((MarginLayoutParams) layoutParams).bottomMargin = Tool.dp2px(4, this);
            }
        }
    }

    public final void updateSearchClock() {
        SearchBar searchBar = findViewById(R.id.searchBar);
        TextView textView = searchBar.searchClock;
        if (textView.getText() != null) {
            try {
                searchBar = findViewById(R.id.searchBar);
                searchBar.updateClock();
            } catch (Exception e) {
                ((SearchBar) findViewById(R.id.searchBar)).searchClock.setText(R.string.bad_format);
            }
        }
    }

    public final void updateHomeLayout() {
        updateSearchBar(true);
        updateDock$default(this, true, 0, 2, null);
        updateDesktopIndicatorVisibility();
        AppSettings appSettings = Setup.appSettings();

        if (!appSettings.getSearchBarEnable()) {
            View findViewById = findViewById(R.id.leftDragHandle);
            LayoutParams layoutParams = findViewById.getLayoutParams();
            ((MarginLayoutParams) layoutParams).topMargin = Desktop._topInset;
            findViewById = findViewById(R.id.rightDragHandle);
            layoutParams = findViewById.getLayoutParams();
            ((MarginLayoutParams) layoutParams).topMargin = Desktop._topInset;
            Desktop desktop = findViewById(R.id.desktop);
            desktop.setPadding(0, Desktop._topInset, 0, 0);
        }
        if (!appSettings.getDockEnable()) {
            getDesktop().setPadding(0, 0, 0, Desktop._bottomInset);
        }
    }

    public DeviceProfile getDeviceProfile() {
        return mDeviceProfile;
    }

    public boolean isInMultiWindowModeCompat() {
        return Utilities.ATLEAST_NOUGAT && isInMultiWindowMode();
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = Objects.requireNonNull(extras).getInt("appWidgetId", -1);
        AppWidgetProviderInfo appWidgetInfo = companion.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra("appWidgetId", appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    private void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = companion.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
        Item item = Item.newWidgetItem(appWidgetId);
        Desktop desktop = getDesktop();
        List<CellContainer> pages = desktop.getPages();
        item.setSpanX((appWidgetInfo.minWidth - 1) / pages.get(desktop.getCurrentItem()).getCellWidth() + 1);
        item.setSpanY((appWidgetInfo.minHeight - 1) / pages.get(desktop.getCurrentItem()).getCellHeight() + 1);
        Point point = desktop.getCurrentPage().findFreeSpace(item.getSpanX(), item.getSpanY());
        if (point != null) {
            item.setX(point.x);
            item.setY(point.y);

            // add item to database
            _db.saveItem(item, desktop.getCurrentItem(), Config.ItemPosition.Desktop);
            desktop.addItemToPage(item, desktop.getCurrentItem());
        } else {
            Tool.toast(this, R.string.toast_not_enough_space);
        }
    }

    private void registerBroadcastReceiver() {
        registerReceiver(_appUpdateReceiver, companion.getAppUpdateIntentFilter());
        if (_timeChangedReceiver != null) {
            registerReceiver(_timeChangedReceiver, companion.getTimeChangesIntentFilter());
        }
        registerReceiver(_shortcutReceiver, companion.getShortcutIntentFilter());
    }

    public final void onUninstallItem(@NonNull Item item) {
        companion.setConsumeNextResume(true);
        Setup.eventHandler().showDeletePackageDialog(this, item);
    }

    public void onDrawerOpened(@NonNull View drawerView) {

    }

    public void onDrawerClosed(@NonNull View drawerView) {
    }

    public final void onRemoveItem(@NonNull Item item) {
        Desktop desktop = getDesktop();
        View coordinateToChildView;
        switch (item._locationInLauncher) {
            case 0:
                coordinateToChildView = desktop.getCurrentPage().coordinateToChildView(new Point(item.getX(), item.getY()));
                desktop.removeItem(coordinateToChildView, true);
                break;
            case 1:
                Dock dock = getDock();
                coordinateToChildView = dock.coordinateToChildView(new Point(item.getX(), item.getY()));
                dock.removeItem(coordinateToChildView, true);
                break;
            default:
                break;
        }
        companion.getDb().deleteItem(item, true);
    }

    public final void onInfoItem(@NonNull Item item) {
        if (item.getType() == Item.Type.APP) {
            try {
                String str = "android.settings.APPLICATION_DETAILS_SETTINGS";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("package:");
                Intent intent = item.intent;
                ComponentName component = intent.getComponent();
                stringBuilder.append(Objects.requireNonNull(component).getPackageName());
                startActivity(new Intent(str, Uri.parse(stringBuilder.toString())));
            } catch (Exception e) {
                Tool.toast(this, R.string.toast_app_uninstalled);
            }
        }
    }

    public final void updateSearchBar(boolean show) {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.getSearchBarEnable() && show) {
            Tool.visibleViews(100, (SearchBar) findViewById(R.id.searchBar));
        } else {
            appSettings = Setup.appSettings();

            if (appSettings.getSearchBarEnable()) {
                Tool.invisibleViews(100, (SearchBar) findViewById(R.id.searchBar));
            } else {
                Tool.goneViews((SearchBar) findViewById(R.id.searchBar));
            }
        }
    }

    private Bundle getActivityAnimationOpts(View view) {
        Bundle bundle = null;
        if (view == null) {
            return null;
        }
        ActivityOptions opts = null;
        if (VERSION.SDK_INT >= 23) {
            int left = 0;
            int top = 0;
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();
            if (view instanceof AppItemView) {
                width = (int) ((AppItemView) view).getIconSize();
                left = (int) ((AppItemView) view).getDrawIconLeft();
                top = (int) ((AppItemView) view).getDrawIconTop();
            }
            opts = ActivityOptions.makeClipRevealAnimation(view, left, top, width, height);
        }
        if (opts != null) {
            bundle = opts.toBundle();
        }
        return bundle;
    }

    private void onHandleLauncherPause() {
        ((Folder) findViewById(R.id.groupPopup)).dismissPopup();
        ((CalendarDropDownView) findViewById(R.id.calendarDropDownView)).animateHide();
        ((DragOptionLayout) findViewById(R.id.dragNDropView)).hidePopupMenu();
        if (!((SearchBar) findViewById(R.id.searchBar)).collapse()) {
            if ((findViewById(R.id.desktop)) != null) {
                Desktop desktop = findViewById(R.id.desktop);
                if (desktop.getInEditMode()) {
                    desktop = findViewById(R.id.desktop);
                    List pages = desktop.getPages();
                    //Desktop desktop2 = findViewById(R.id.desktop);
                    ((CellContainer) pages.get(desktop.getCurrentItem())).performClick();
                } else {
                    AppDrawerController appDrawerController = findViewById(R.id.appDrawerController);
                    View drawer = appDrawerController.getDrawer();
                    if (drawer.getVisibility() == View.VISIBLE) {
                        closeAppDrawer();
                    } else {
                        setToHomePage();
                    }
                }
            }
        }
    }

    private void setToHomePage() {
        Desktop desktop = findViewById(R.id.desktop);
        AppSettings appSettings = Setup.appSettings();
        desktop.setCurrentItem(appSettings.getDesktopPageCurrent());
    }

    private void handleLauncherPause(boolean wasHomePressed) {
        if (!companion.getConsumeNextResume() || wasHomePressed) {
            onHandleLauncherPause();
        } else {
            companion.setConsumeNextResume(false);
        }
    }

    @Override
    public void onBackPressed() {
        handleLauncherPause(false);
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
    }

    public final void updateDesktopIndicatorVisibility() {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.isDesktopShowIndicator()) {
            Tool.visibleViews(100, (PageIndicator) findViewById(R.id.desktopIndicator));
            return;
        }
        Tool.goneViews(100, (PageIndicator) findViewById(R.id.desktopIndicator));
    }

    public final Folder getFolder() {
        return findViewById(R.id.groupPopup);
    }

    public final View getBackground() {
        return findViewById(R.id.background);
    }

    public final SearchBar getSearchBar() {
        return findViewById(R.id.searchBar);
    }

    public final void dimBackground() {
        Tool.visibleViews(findViewById(R.id.background));
    }

    public final void unDimBackground() {
        Tool.invisibleViews(findViewById(R.id.background));
    }

    public final void clearRoomForPopUp() {
        Tool.invisibleViews((Desktop) findViewById(R.id.desktop));
        hideDesktopIndicator();
        updateDock$default(this, false, 0, 2, null);
    }

    public final void unClearRoomForPopUp() {
        Tool.visibleViews((Desktop) findViewById(R.id.desktop));
        showDesktopIndicator();
        updateDock$default(this, true, 0, 2, null);
    }

    public final PageIndicator getDesktopIndicator() {
        return findViewById(R.id.desktopIndicator);
    }

    @NonNull
    public final AppDrawerController getAppDrawerController() {
        return findViewById(R.id.appDrawerController);
    }

    @NonNull
    public final DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout);
    }

    @NonNull
    public final Desktop getDesktop() {
        return findViewById(R.id.desktop);
    }

    @NonNull
    public final Dock getDock() {
        return findViewById(R.id.dock);
    }

    @NonNull
    public final DragOptionLayout getDragNDropView() {
        return findViewById(R.id.dragNDropView);
    }

    private void init() {
        companion.setAppWidgetHost(new WidgetHost(getApplicationContext(), R.id.app_widget_host));
        HomeActivity.Companion companion;
        companion = HomeActivity.companion;
        AppWidgetManager instance = AppWidgetManager.getInstance(this);

        companion.setAppWidgetManager(instance);
        WidgetHost appWidgetHost = companion.getAppWidgetHost();
        Objects.requireNonNull(appWidgetHost).startListening();

        initViews();
        PopupMenuItems hpDragOption = new PopupMenuItems();
        View findViewById = findViewById(R.id.leftDragHandle);

        View findViewById2 = findViewById(R.id.rightDragHandle);

        DragOptionLayout dragOptionLayout = findViewById(R.id.dragNDropView);

        hpDragOption.initDragNDrop(this, findViewById, findViewById2, dragOptionLayout);

        registerBroadcastReceiver();
        initAppManager();
        initSettings();
        System.runFinalization();
        System.gc();
    }

    protected void initViews() {
        new HpSearchBar(this, findViewById(R.id.searchBar), findViewById(R.id.calendarDropDownView)).initSearchBar();
        initDock();
        ((AppDrawerController) findViewById(R.id.appDrawerController)).init();
        ((AppDrawerController) findViewById(R.id.appDrawerController)).setHome(this);

        ((DragOptionView) findViewById(R.id.dragOptionPanel)).setHome(this);
        ((Desktop) findViewById(R.id.desktop)).init();
        Desktop desktop = findViewById(R.id.desktop);


        desktop.setDesktopEditListener(this);
        ((DesktopOptionView) findViewById(R.id.desktopEditOptionPanel)).setDesktopOptionViewListener(this);
        DesktopOptionView desktopOptionView = findViewById(R.id.desktopEditOptionPanel);
        AppSettings appSettings = Setup.appSettings();

        desktopOptionView.updateLockIcon(appSettings.isDesktopLock());
        ((Desktop) findViewById(R.id.desktop)).addOnPageChangeListener(new SmoothViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                DesktopOptionView desktopOptionView = findViewById(R.id.desktopEditOptionPanel);
                AppSettings appSettings = Setup.appSettings();

                desktopOptionView.updateHomeIcon(appSettings.getDesktopPageCurrent() == position);
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        desktop = findViewById(R.id.desktop);
        desktop.setPageIndicator(findViewById(R.id.desktopIndicator));
        ((DragOptionView) findViewById(R.id.dragOptionPanel)).setAutoHideView((SearchBar) findViewById(R.id.searchBar));
        initAppDrawer();
        initMinibar();
    }

    private void initAppDrawer() {
        HomeActivity launcher = this;
        AppDrawerController drawerController = findViewById(R.id.appDrawerController);
        PageIndicator indicator = findViewById(R.id.appDrawerIndicator);
        DragOptionView optionPanel = findViewById(R.id.dragOptionPanel);

        HpAppDrawer hpInit = new HpAppDrawer(launcher, indicator, optionPanel);
        hpInit.initAppDrawer(drawerController);
    }

    private void initSettings() {
        updateHomeLayout();
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.isDesktopFullscreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        Desktop desktop = findViewById(R.id.desktop);
        desktop.setBackgroundColor(appSettings.getDesktopBackgroundColor());
        Dock dock = findViewById(R.id.dock);
        dock.setBackgroundColor(appSettings.getDockColor());
        getDrawerLayout().setDrawerLockMode(AppSettings.get().getMinibarEnable() ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void initAppManager() {
        Setup.appLoader().addUpdateListener(new AppManager.AppUpdatedListener() {
            @Override
            public boolean onAppUpdated(List<App> it) {
                AppSettings appSettings = Setup.appSettings();
                if (appSettings.getDesktopStyle() == 0) {
                    getDesktop().initDesktopNormal(HomeActivity.this);
                    if (appSettings.isAppFirstLaunch()) {
                        appSettings.setAppFirstLaunch(false);
                        addAppDrawerItem();
                        addDockCamera();
                        addDockApps();
                    }
                } else {
                    getDesktop().initDesktopShowAll(HomeActivity.this, HomeActivity.this);
                }
                getDock().initDockItem(HomeActivity.this);
                return true;
            }
        });
        Setup.appLoader().addDeleteListener(apps -> {
            AppSettings appSettings = Setup.appSettings();

            if (appSettings.getDesktopStyle() == 0) {
                getDesktop().initDesktopNormal(HomeActivity.this);
            } else {
                getDesktop().initDesktopShowAll(HomeActivity.this, HomeActivity.this);
            }
            getDock().initDockItem(HomeActivity.this);
            setToHomePage();
            return false;
        });
        AppManager.getInstance(this).init();
    }

    public void addAppDrawerItem() {
        Item appDrawerBtnItem = Item.newActionItem(8);
        appDrawerBtnItem.setX(2);
        companion.getDb().saveItem(appDrawerBtnItem, 0, Config.ItemPosition.Dock);
    }

    public void addDockApps() {

        //PHONE
        Intent phone = new Intent(Intent.ACTION_MAIN, null);
        phone.addCategory(Intent.ACTION_DIAL);
        phone.addCategory(Intent.CATEGORY_DEFAULT);

        //MESSAGING
        Intent messaging = new Intent(Intent.ACTION_MAIN, null);
        messaging.addCategory(Intent.CATEGORY_APP_MESSAGING);
        messaging.addCategory(Intent.CATEGORY_DEFAULT);

        //BROWSER
        Intent browser = new Intent(Intent.ACTION_MAIN, null);
        browser.addCategory(Intent.CATEGORY_APP_BROWSER);
        browser.addCategory(Intent.CATEGORY_DEFAULT);

        PackageManager packageManager = this.getPackageManager();
        List<ResolveInfo> phoneInfo = packageManager.queryIntentActivities(phone, 0);
        if (!Config.DEBUG_MODE) {
            if (phoneInfo != null || phoneInfo.size() > 0) {
                ResolveInfo dockApp = phoneInfo.get(1);
                App app = new App(dockApp, packageManager);
                Item item = Item.newAppItem(app);
                item.setX(0);
                Log.i(TAG, "Loading App: " + item.getLabel());
                companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
            }
        }
        List<ResolveInfo> messagingInfo = packageManager.queryIntentActivities(messaging, 0);
        if (messagingInfo != null || messagingInfo.size() > 0) {
            ResolveInfo dockApp = messagingInfo.get(0);
            App app = new App(dockApp, packageManager);
            Item item = Item.newAppItem(app);
            item.setX(1);
            Log.i(TAG, "Loading App: " + item.getLabel());
            companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
        }
        List<ResolveInfo> browserInfo = packageManager.queryIntentActivities(browser, 0);
        if (browserInfo != null || Objects.requireNonNull(browserInfo).size() > 0) {
            ResolveInfo dockApp = browserInfo.get(0);
            App app = new App(dockApp, packageManager);
            Item item = Item.newAppItem(app);
            item.setX(4);
            Log.i(TAG, "Loading App: " + item.getLabel());
            companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
        }
    }

    public void addDockCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        PackageManager packageManager = this.getPackageManager();
        List<ResolveInfo> activitiesInfo = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : activitiesInfo) {
            App app = new App(info, packageManager);
            Log.i(TAG, app.getPackageName());
            Item item = Item.newAppItem(app);
            item.setX(3);
            companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
        }

    }

    public void initMinibar() {
        final ArrayList<Minibar.ActionDisplayItem> items = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<Integer> icons = new ArrayList<>();
        for (String act : AppSettings.get().getMinibarArrangement()) {
            if (act.length() > 1) {
                Minibar.ActionDisplayItem item = Minibar.getActionItemFromString(act);
                if (item != null) {
                    items.add(item);
                    labels.add(item.label);
                    icons.add(item.icon);
                }
            }
        }

        SwipeListView minibar = findViewById(R.id.minibar);
        minibar.setAdapter(new MinibarAdapter(this, labels, icons));
        minibar.setOnItemClickListener((parent, view, i, id) -> {
            Action action = Action.valueOf(labels.get(i));
            if (action == Action.DeviceSettings || action == Action.LauncherSettings || action == Action.EditMinibar) {
                _consumeNextResume = true;
            }
            Minibar.RunAction(action, HomeActivity.this);
            if (action != Action.DeviceSettings && action != Action.LauncherSettings && action != Action.EditMinibar) {
                getDrawerLayout().closeDrawers();
            }
        });
        // frame layout spans the entire side while the minibar container has gaps at the top and bottom
        ((FrameLayout) minibar.getParent()).setBackgroundColor(AppSettings.get().getMinibarBackgroundColor());
    }

    private void initDock() {
        int iconSize = Setup.appSettings().getDockIconSize();
        Dock dock = findViewById(R.id.dock);
        dock.setHome(this);
        dock.init();
        if (appSettings.isDockShowLabel()) {
            dock.getLayoutParams().height = Tool.dp2px(((16 + iconSize) + 14) + 10, this) + dock.getBottomInset();
        } else {
            dock.getLayoutParams().height = Tool.dp2px((16 + iconSize) + 10, this) + dock.getBottomInset();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == -1) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == 0 && data != null) {
            int appWidgetId = data.getIntExtra("appWidgetId", -1);
            if (appWidgetId != -1) {
                WidgetHost appWidgetHost = companion.getAppWidgetHost();
                if (appWidgetHost != null) {
                    appWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }
        }
    }

    public final void onStartApp(@NonNull Context context, @NonNull Intent intent, @Nullable View view) {
        ComponentName component = intent.getComponent();

        if (BuildConfig.APPLICATION_ID.equals(Objects.requireNonNull(component).getPackageName())) {
            Minibar.RunAction(Action.LauncherSettings, context);
            companion.setConsumeNextResume(true);
        } else {
            try {
                context.startActivity(intent, getActivityAnimationOpts(view));
                companion.setConsumeNextResume(true);
            } catch (Exception e) {
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public final void onStartApp(@NonNull Context context, @NonNull App app, @Nullable View view) {
        if (BuildConfig.APPLICATION_ID.equals(app.getPackageName())) {
            Minibar.RunAction(Action.LauncherSettings, context);
            companion.setConsumeNextResume(true);
        } else {
            try {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(app.getPackageName(), app.getClassName());
                companion.getDb().updateAppCount(app.getPackageName());
                context.startActivity(intent, getActivityAnimationOpts(view));
                companion.setConsumeNextResume(true);
            } catch (Exception e) {
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        Log.i(TAG, "Slide Works left and right");
    }

    @Override
    protected void onDestroy() {
        WidgetHost appWidgetHost = companion.getAppWidgetHost();
        if (appWidgetHost != null) {
            appWidgetHost.stopListening();
        }
        companion.setAppWidgetHost(null);
        unregisterReceiver(_appUpdateReceiver);
        if (_timeChangedReceiver != null) {
            unregisterReceiver(_timeChangedReceiver);
        }
        unregisterReceiver(_shortcutReceiver);
        companion.setLauncher(null);
        super.onDestroy();
    }

    public final void hideDesktopIndicator() {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.isDesktopShowIndicator()) {
            Tool.invisibleViews(100, (PageIndicator) findViewById(R.id.desktopIndicator));
        }
    }

    public final void showDesktopIndicator() {
        AppSettings appSettings = Setup.appSettings();
        if (appSettings.isDesktopShowIndicator()) {
            Tool.visibleViews(100, (PageIndicator) findViewById(R.id.desktopIndicator));
        }
    }

    public void onDrawerStateChanged(int newState) {
    }

    public final void closeAppDrawer() {
        int finalRadius = Math.max(getAppDrawerController().getDrawer().getWidth(), getAppDrawerController().getDrawer().getHeight());
        getAppDrawerController().close(cx, cy, rad, finalRadius);
    }

    @Override
    public void onDesktopEdit() {
        Tool.visibleViews(100, 20, (DesktopOptionView) findViewById(R.id.desktopEditOptionPanel));
        updateDock$default(this, false, 0, 2, null);
        updateSearchBar(false);
    }

    @Override
    public void onFinishDesktopEdit() {
        Tool.invisibleViews(100, 20, (DesktopOptionView) findViewById(R.id.desktopEditOptionPanel));
        ((PageIndicator) findViewById(R.id.desktopIndicator)).hideDelay();
        updateDock$default(this, true, 0, 2, null);
        updateSearchBar(true);
    }

    @Override
    public void onRemovePage() {
        if (getDesktop().isCurrentPageEmpty()) {
            getDesktop().removeCurrentPage();
            return;
        }
        DialogHelper.alertDialog(this, getString(R.string.remove),
                "This page is not empty. Those item will also be removed.",
                (dialog, which) -> getDesktop().removeCurrentPage());
    }

    @Override
    public void onSetPageAsHome() {
        AppSettings appSettings = Setup.appSettings();
        Desktop desktop = findViewById(R.id.desktop);
        appSettings.setDesktopPageCurrent(desktop.getCurrentItem());
    }

    @Override
    public void onLaunchSettings() {
        companion.setConsumeNextResume(true);
        Setup.eventHandler().showLauncherSettings(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "Testing Dialog");

        boolean alreadyOnHome = mHasFocus && ((intent.getFlags() &
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        boolean isActionMain = Intent.ACTION_MAIN.equals(intent.getAction());
        if (isActionMain) {
            closeSystemDialogs();
            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

        dismissDialog();
    }

    public void closeSystemDialogs() {
        getWindow().closeAllPanels();
    }

    @Override
    public void onPickDesktopAction() {
        new HpDesktopPickAction(this).onPickDesktopAction();
    }

    @Override
    public void onPickWidget() {
        pickWidget();
    }

    private void pickWidget() {
        companion.setConsumeNextResume(true);
        int appWidgetId = companion.getAppWidgetHost().allocateAppWidgetId();
        Intent pickIntent = new Intent("android.appwidget.action.APPWIDGET_PICK");
        pickIntent.putExtra("appWidgetId", appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    public void openDialog(LauncherDialog dialog) {
        dismissDialog();
        mCurrentDialog = dialog;
        mCurrentDialog.setOnDismissListener(this);
        mCurrentDialog.show();
    }

    private void dismissDialog() {
        if (mCurrentDialog != null) {
            mCurrentDialog.dismiss();
            mCurrentDialog = null;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        mCurrentDialog = null;
    }

    public void startEditIcon(Item item) {
        Intent intent = new Intent(this, EditIconActivity.class);
        intent.putExtra("itemInfo", item);
        startActivityForResult(intent, REQUEST_EDIT_ICON);
    }

    public static final class Companion {

        @Nullable
        public final HomeActivity getLauncher() {
            return launcher;
        }

        final void setLauncher(@Nullable HomeActivity v) {
            launcher = v;
        }

        @Nullable
        public final Resources getResources() {
            return resources;
        }

        final void setResources(@Nullable Resources v) {
            resources = v;
        }

        @NonNull
        public final Setup.DataManager getDb() {
            return _db;
        }

        private void setDb(@NonNull Setup.DataManager v) {
            _db = v;
        }

        @Nullable
        public WidgetHost getAppWidgetHost() {
            return _appWidgetHost;
        }

        private void setAppWidgetHost(@Nullable WidgetHost v) {
            _appWidgetHost = v;
        }

        @NonNull
        public final AppWidgetManager getAppWidgetManager() {
            return _appWidgetManager;
        }

        private void setAppWidgetManager(@NonNull AppWidgetManager v) {
            _appWidgetManager = v;
        }

        public final void setItemTouchX(float v) {
            _itemTouchX = v;
        }

        public final void setItemTouchY(float v) {
            _itemTouchY = v;
        }

        private boolean getConsumeNextResume() {
            return _consumeNextResume;
        }

        private void setConsumeNextResume(boolean v) {
            _consumeNextResume = v;
        }

        private IntentFilter getTimeChangesIntentFilter() {
            return _timeChangesIntentFilter;
        }

        private IntentFilter getAppUpdateIntentFilter() {
            return _appUpdateIntentFilter;
        }

        private IntentFilter getShortcutIntentFilter() {
            return _shortcutIntentFilter;
        }
    }

    public static class LauncherDialog extends Dialog {

        public LauncherDialog(@NonNull Context context) {
            super(context);
        }

        public LauncherDialog(@NonNull Context context, int themeResId) {
            super(context, themeResId);
        }

        protected LauncherDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        public void onResume() {

        }
    }
}
