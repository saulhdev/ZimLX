package org.zimmob.zimlx.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import net.gsantner.opoc.util.ContextUtils;

import org.zimmob.zimlx.BuildConfig;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.homeparts.HpAppDrawer;
import org.zimmob.zimlx.activity.homeparts.HpDesktopPickAction;
import org.zimmob.zimlx.activity.homeparts.HpInitSetup;
import org.zimmob.zimlx.activity.homeparts.HpSearchBar;
import org.zimmob.zimlx.apps.AppManager;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.dragndrop.DragOption;
import org.zimmob.zimlx.launcher.LauncherAction;
import org.zimmob.zimlx.launcher.LauncherAction.Action;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.manager.Setup.DataManager;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.pageindicator.PageIndicator;
import org.zimmob.zimlx.receiver.AppUpdateReceiver;
import org.zimmob.zimlx.receiver.ShortcutReceiver;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.DialogHelper;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.MinibarAdapter;
import org.zimmob.zimlx.viewutil.WidgetHost;
import org.zimmob.zimlx.widget.AppDrawerController;
import org.zimmob.zimlx.widget.AppItemView;
import org.zimmob.zimlx.widget.CalendarDropDownView;
import org.zimmob.zimlx.widget.CellContainer;
import org.zimmob.zimlx.widget.Desktop;
import org.zimmob.zimlx.widget.Desktop.OnDesktopEditListener;
import org.zimmob.zimlx.widget.DesktopOptionView;
import org.zimmob.zimlx.widget.Dock;
import org.zimmob.zimlx.widget.DragOptionLayout;
import org.zimmob.zimlx.widget.DragOptionView;
import org.zimmob.zimlx.folder.GroupPopupView;
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
public class HomeActivity extends Activity implements OnDesktopEditListener, DesktopOptionView.DesktopOptionViewListener, DrawerLayout.DrawerListener {

    public static final Companion Companion = new Companion();
    private static final int REQUEST_CREATE_APPWIDGET = 0x6475;
    public static final int REQUEST_PERMISSION_STORAGE = 0x3648;
    private static final int REQUEST_PICK_APPWIDGET = 0x2678;

    private static Resources resources;
    private static final IntentFilter _appUpdateIntentFilter = new IntentFilter();

    private static WidgetHost _appWidgetHost;

    private static AppWidgetManager _appWidgetManager;
    private static boolean _consumeNextResume;

    public static Setup.DataManager _db;
    public static float _itemTouchX;
    public static float _itemTouchY;

    private static HomeActivity _homeActivity;
    private static final IntentFilter _shortcutIntentFilter = new IntentFilter();
    private static final IntentFilter _timeChangesIntentFilter = new IntentFilter();
    private final AppUpdateReceiver _appUpdateReceiver = new AppUpdateReceiver();
    private int cx;
    private int cy;
    private int rad;
    private final ShortcutReceiver _shortcutReceiver = new ShortcutReceiver();
    private BroadcastReceiver _timeChangedReceiver;

    static {
        Companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIME_TICK");
        Companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIMEZONE_CHANGED");
        Companion.getTimeChangesIntentFilter().addAction("android.intent.action.TIME_SET");
        Companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_ADDED");
        Companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_REMOVED");
        Companion.getAppUpdateIntentFilter().addAction("android.intent.action.PACKAGE_CHANGED");
        Companion.getAppUpdateIntentFilter().addDataScheme("package");
        Companion.getShortcutIntentFilter().addAction("com.android.launcher.action.INSTALL_SHORTCUT");
    }

    protected void onCreate(Bundle savedInstanceState) {
        Companion.setResources(getResources());
        ContextUtils contextUtils = new ContextUtils(getApplicationContext());
        AppSettings appSettings = AppSettings.get();

        contextUtils.setAppLanguage(appSettings.getLanguage());
        super.onCreate(savedInstanceState);
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
        Companion.setLauncher(this);
        DataManager dataManager = Setup.dataManager();

        Companion.setDb(dataManager);
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
        Companion.setLauncher(this);
        WidgetHost appWidgetHost = Companion.getAppWidgetHost();
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
            Log.d(HomeActivity.class.getSimpleName(), "Unable to read settings", e);
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

    public final void openAppDrawer(View view) {
        openAppDrawer$default(this, view, 0, 0, 6, null);
    }

    private static void openAppDrawer$default(HomeActivity home, View view, int i, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            view = home.findViewById(R.id.desktop);
        }
        if ((i3 & 2) != 0) {
            i = -1;
        }
        if ((i3 & 4) != 0) {
            i2 = -1;
        }
        home.openAppDrawer(view, i, i2);
    }

    public final void openAppDrawer(View view, int x, int y) {
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
        }
        else {
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
        TextView textView = searchBar._searchClock;
        if (textView.getText() != null) {
            try {
                searchBar = findViewById(R.id.searchBar);
                searchBar.updateClock();
            } catch (Exception e) {
                ((SearchBar) findViewById(R.id.searchBar))._searchClock.setText(R.string.bad_format);
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

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = Objects.requireNonNull(extras).getInt("appWidgetId", -1);
        AppWidgetProviderInfo appWidgetInfo = Companion.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
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
        int appWidgetId = Objects.requireNonNull(extras).getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = Companion.getAppWidgetManager().getAppWidgetInfo(appWidgetId);
        Item item = Item.newWidgetItem(appWidgetId);
        Desktop desktop = getDesktop();
        List<CellContainer> pages = desktop.getPages();
        item.spanX = (appWidgetInfo.minWidth - 1) / pages.get(desktop.getCurrentItem()).getCellWidth() + 1;
        item.spanY = (appWidgetInfo.minHeight - 1) / pages.get(desktop.getCurrentItem()).getCellHeight() + 1;
        Point point = desktop.getCurrentPage().findFreeSpace(item.getSpanX(), item.getSpanY());
        if (point != null) {
            item.x = point.x;
            item.y = point.y;

            // add item to database
            _db.saveItem(item, desktop.getCurrentItem(), Config.ItemPosition.Desktop);
            desktop.addItemToPage(item, desktop.getCurrentItem());
        } else {
            Tool.toast(this, R.string.toast_not_enough_space);
        }
    }

    private void registerBroadcastReceiver() {
        registerReceiver(_appUpdateReceiver, Companion.getAppUpdateIntentFilter());
        if (_timeChangedReceiver != null) {
            registerReceiver(_timeChangedReceiver, Companion.getTimeChangesIntentFilter());
        }
        registerReceiver(_shortcutReceiver, Companion.getShortcutIntentFilter());
    }

    private void handleLauncherPause(boolean wasHomePressed) {
        if (!Companion.getConsumeNextResume() || wasHomePressed) {
            onHandleLauncherPause();
        } else {
            Companion.setConsumeNextResume(false);
        }
    }

    public final void onUninstallItem(@NonNull Item item) {
        Companion.setConsumeNextResume(true);
        Setup.eventHandler().showDeletePackageDialog(this, item);
    }

    public final void onRemoveItem(@NonNull Item item) {
        Desktop desktop = getDesktop();
        View coordinateToChildView;
        switch (item._locationInLauncher) {
            case 0:
                coordinateToChildView = desktop.getCurrentPage().coordinateToChildView(new Point(item.x, item.y));
                desktop.removeItem(coordinateToChildView, true);
                break;
            case 1:
                Dock dock = getDock();
                coordinateToChildView = dock.coordinateToChildView(new Point(item.x, item.y));
                dock.removeItem(coordinateToChildView, true);
                break;
            default:
                break;
        }
        Companion.getDb().deleteItem(item, true);
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

    public final void closeAppDrawer() {

        int finalRadius = Math.max(getAppDrawerController().getDrawer().getWidth(), getAppDrawerController().getDrawer().getHeight());
        getAppDrawerController().close(cx, cy, rad, finalRadius);
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
        else if (VERSION.SDK_INT < 21) {
            opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        if (opts != null) {
            bundle = opts.toBundle();
        }
        return bundle;
    }

    private void onHandleLauncherPause() {
        ((GroupPopupView) findViewById(R.id.groupPopup)).dismissPopup();
        ((CalendarDropDownView) findViewById(R.id.calendarDropDownView)).animateHide();
        ((DragOptionLayout) findViewById(R.id.dragNDropView)).hidePopupMenu();
        if (!((SearchBar) findViewById(R.id.searchBar)).collapse()) {
            if ((findViewById(R.id.desktop)) != null) {
                Desktop desktop = findViewById(R.id.desktop);
                if (desktop.getInEditMode()) {
                    desktop = findViewById(R.id.desktop);
                    List pages = desktop.getPages();
                    Desktop desktop2 = findViewById(R.id.desktop);
                    ((CellContainer) pages.get(desktop2.getCurrentItem())).performClick();
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

    public void onBackPressed() {
        Tool.goneViews(100,  findViewById(R.id.search_apps));
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

    public final void initMinibar() {
        final ArrayList<LauncherAction.ActionDisplayItem> items = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<Integer> icons = new ArrayList<>();

        for (String act : AppSettings.get().getMinibarArrangement()) {
            if (act.length() > 1) {
                LauncherAction.ActionDisplayItem item = LauncherAction.getActionItemFromString(act);
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
            LauncherAction.RunAction(action, HomeActivity.this);
            if (action != Action.DeviceSettings && action != Action.LauncherSettings && action != Action.EditMinibar) {
                getDrawerLayout().closeDrawers();
            }
        });
        // frame layout spans the entire side while the minibar container has gaps at the top and bottom
        ((FrameLayout) minibar.getParent()).setBackgroundColor(AppSettings.get().getMinibarBackgroundColor());
    }

    public final GroupPopupView getGroupPopup() {
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
        Companion.setAppWidgetHost(new WidgetHost(getApplicationContext(), R.id.app_widget_host));
        AppWidgetManager instance = AppWidgetManager.getInstance(this);

        Companion.setAppWidgetManager(instance);
        WidgetHost appWidgetHost = Companion.getAppWidgetHost();
        Objects.requireNonNull(appWidgetHost).startListening();
        initViews();
        DragOption dragOption = new DragOption();
        View findViewById = findViewById(R.id.leftDragHandle);
        View findViewById2 = findViewById(R.id.rightDragHandle);
        DragOptionLayout dragOptionLayout = findViewById(R.id.dragNDropView);
        dragOption.initDragNDrop(this, findViewById, findViewById2, dragOptionLayout);
        registerBroadcastReceiver();
        initAppManager();
        initSettings();
        System.runFinalization();
        System.gc();
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
        AppSettings appSettings2 = Setup.appSettings();

        desktop.setBackgroundColor(appSettings2.getDesktopBackgroundColor());
        Dock dock = findViewById(R.id.dock);
        appSettings2 = Setup.appSettings();

        dock.setBackgroundColor(appSettings2.getDockColor());
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
                        Item appDrawerBtnItem = Item.newActionItem(8);
                        appDrawerBtnItem.x = 2;
                        Companion.getDb().saveItem(appDrawerBtnItem, 0, Config.ItemPosition.Dock);

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

    //private void addDockApps(String appCategory, int position) {
    private void addDockApps() {

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
        for (ResolveInfo info : phoneInfo) {
            App app = new App(this, info, packageManager);
            Item item = Item.newAppItem(app);
            item.x = 0;
            Companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
        }

        List<ResolveInfo> messagingInfo = packageManager.queryIntentActivities(messaging, 0);
        for (ResolveInfo info : messagingInfo) {
            App app = new App(this, info, packageManager);
            Item item = Item.newAppItem(app);
            item.x = 1;
            Companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
        }

        List<ResolveInfo> browserInfo = packageManager.queryIntentActivities(browser, 0);
        for (ResolveInfo info : browserInfo) {
            App app = new App(this, info, packageManager);
            Item item = Item.newAppItem(app);
            item.x = 4;
            Companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
        }

        //start apps count
        List<App> allApps = Setup.appLoader().getAllApps(this, false);
        Log.i("InitSetup","Loading count apps: "+allApps.size());
        for (App app: allApps) {
            Companion.getDb().saveApp(app.getPackageName());
        }

    }

    private void addDockCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        PackageManager packageManager = this.getPackageManager();
        List<ResolveInfo> activitiesInfo = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : activitiesInfo) {
            App app = new App(this, info, packageManager);
            Log.i("HOME", app.getPackageName());
            Item item = Item.newAppItem(app);
            item.x = 3;
            Companion.getDb().saveItem(item, 0, Config.ItemPosition.Dock);
        }

    }

    private void initDock() {
        int iconSize = Setup.appSettings().getDockIconSize();
        Dock dock = findViewById(R.id.dock);
        dock.setHome(this);
        dock.init();
        AppSettings appSettings = Setup.appSettings();

        if (appSettings.isDockShowLabel()) {
            dock.getLayoutParams().height = Tool.dp2px(((16 + iconSize) + 14) + 10, this) + dock.getBottomInset();
        } else {
            dock.getLayoutParams().height = Tool.dp2px((16 + iconSize) + 10, this) + dock.getBottomInset();
        }
    }

    private void initViews() {
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
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                DesktopOptionView desktopOptionView = findViewById(R.id.desktopEditOptionPanel);
                AppSettings appSettings = Setup.appSettings();

                desktopOptionView.updateHomeIcon(appSettings.getDesktopPageCurrent() == position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        desktop = findViewById(R.id.desktop);
        desktop.setPageIndicator(findViewById(R.id.desktopIndicator));
        ((DragOptionView) findViewById(R.id.dragOptionPanel)).setAutoHideView((SearchBar) findViewById(R.id.searchBar));
        new HpAppDrawer(this, findViewById(R.id.appDrawerIndicator), findViewById(R.id.dragOptionPanel)).initAppDrawer(findViewById(R.id.appDrawerController));
        initMinibar();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == -1) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(Objects.requireNonNull(data));
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(Objects.requireNonNull(data));
            }
        } else if (resultCode == 0 && data != null) {
            int appWidgetId = data.getIntExtra("appWidgetId", -1);
            if (appWidgetId != -1) {
                WidgetHost appWidgetHost = Companion.getAppWidgetHost();
                if (appWidgetHost != null) {
                    appWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }
        }
    }

    public final void onStartApp(@NonNull Context context, @NonNull Intent intent, @Nullable View view) {
        ComponentName component = intent.getComponent();

        if (BuildConfig.APPLICATION_ID.equals(Objects.requireNonNull(component).getPackageName())) {
            LauncherAction.RunAction(Action.LauncherSettings, context);
            Companion.setConsumeNextResume(true);
        } else {
            try {
                context.startActivity(intent, getActivityAnimationOpts(view));
                Companion.setConsumeNextResume(true);
            } catch (Exception e) {
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public final void onStartApp(@NonNull Context context, @NonNull App app, @Nullable View view) {
        if (BuildConfig.APPLICATION_ID.equals(app.getPackageName())) {
            LauncherAction.RunAction(Action.LauncherSettings, context);
            Companion.setConsumeNextResume(true);
        } else {
            try {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(app.getPackageName(), app.getClassName());
                context.startActivity(intent, getActivityAnimationOpts(view));
                Companion.setConsumeNextResume(true);
            } catch (Exception e) {
                Tool.toast(context, R.string.toast_app_uninstalled);
            }
        }
    }

    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        Log.i("Slide: ", "Works left and right");
    }

    public void onDrawerOpened(@NonNull View drawerView) {

    }

    public void onDrawerClosed(@NonNull View drawerView) {
    }

    protected void onDestroy() {
        WidgetHost appWidgetHost = Companion.getAppWidgetHost();
        if (appWidgetHost != null) {
            appWidgetHost.stopListening();
        }
        Companion.setAppWidgetHost(null);
        unregisterReceiver(_appUpdateReceiver);
        if (_timeChangedReceiver != null) {
            unregisterReceiver(_timeChangedReceiver);
        }
        unregisterReceiver(_shortcutReceiver);
        Companion.setLauncher(null);
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
        DialogHelper.alertDialog(this, getString(R.string.remove), "This page is not empty. Those item will also be removed.", (dialog, which) -> getDesktop().removeCurrentPage());
    }
    @Override
    public void onSetPageAsHome() {
        AppSettings appSettings = Setup.appSettings();
        Desktop desktop = findViewById(R.id.desktop);
        appSettings.setDesktopPageCurrent(desktop.getCurrentItem());
    }

    @Override
    public void onLaunchSettings() {
        Companion.setConsumeNextResume(true);
        Setup.eventHandler().showLauncherSettings(this);
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
        Companion.setConsumeNextResume(true);
        int appWidgetId = Objects.requireNonNull(Companion.getAppWidgetHost()).allocateAppWidgetId();
        Intent pickIntent = new Intent("android.appwidget.action.APPWIDGET_PICK");
        pickIntent.putExtra("appWidgetId", appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    public static final class Companion {
        private Companion() {
        }

        @Nullable
        public final HomeActivity getLauncher() {
            return _homeActivity;
        }

        final void setLauncher(@Nullable HomeActivity v) {
            _homeActivity = v;
        }

        @Nullable
        public final Resources get_resources() {
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

}
