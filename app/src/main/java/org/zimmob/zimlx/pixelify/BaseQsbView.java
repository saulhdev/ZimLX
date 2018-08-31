package org.zimmob.zimlx.pixelify;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import org.zimmob.zimlx.BuildConfig;
import org.zimmob.zimlx.DeviceProfile;
import org.zimmob.zimlx.Launcher;
import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.compat.LauncherAppsCompat;
import org.zimmob.zimlx.config.FeatureFlags;
import org.zimmob.zimlx.overlay.LawnfeedClient;
import org.zimmob.zimlx.preferences.IPreferenceProvider;
import org.zimmob.zimlx.preferences.PreferenceProvider;
import org.zimmob.zimlx.util.PackageManagerHelper;

public abstract class BaseQsbView extends FrameLayout implements OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TEXT_ASSIST = "com.google.android.googlequicksearchbox.TEXT_ASSIST";
    private static final String VOICE_ASSIST = Intent.ACTION_VOICE_COMMAND;
    protected final Launcher mLauncher;
    private final BroadcastReceiver packageChangedReceiver = new PackageChangedReceiver(this);
    protected View mQsbView;
    protected boolean showMic;
    protected QsbConnector qsbConnector;
    private ObjectAnimator elevationAnimator;
    private boolean qsbHidden;
    private int mQsbViewId = 0;
    private boolean bM;
    private boolean mUseWhiteLogo;

    public BaseQsbView(Context context, AttributeSet attributeSet, int i) {
        super(FeatureFlags.applyDarkTheme(context, FeatureFlags.DARK_QSB), attributeSet, i);
        mLauncher = Launcher.getLauncher(context);
    }

    protected abstract int getQsbView(boolean withMic);

    public void applyVoiceSearchPreference() {
        showMic = Utilities.getPrefs(getContext()).getShowVoiceSearchButton() && Utilities.getPrefs(getContext()).getShowSearchPill();
        boolean useWhiteLogo = Utilities.getPrefs(getContext()).getUseWhiteGoogleIcon();
        int qsbView = getQsbView(showMic);
        if (qsbView != mQsbViewId || mUseWhiteLogo != useWhiteLogo) {
            mQsbViewId = qsbView;
            mUseWhiteLogo = useWhiteLogo;
            if (mQsbView != null) {
                removeView(mQsbView);
            }
            mQsbView = LayoutInflater.from(getContext()).inflate(mQsbViewId, this, false);
            float qsbButtonElevation = (float) getResources().getDimensionPixelSize(R.dimen.qsb_button_elevation);
            addView(mQsbView);
            elevationAnimator = ObjectAnimator.ofFloat(mQsbView, "elevation", 0.0f, qsbButtonElevation);
            elevationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            if (qsbHidden) {
                hideQsb();
            }
            mQsbView.setOnClickListener(this);
            mQsbView.setAccessibilityDelegate(new AccessibilityHelper());
            if (showMic) {
                mQsbView.findViewById(R.id.mic_icon).setOnClickListener(this);
            }
            setupViews();
        }
    }

    protected void setupViews() {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!Utilities.getPrefs(getContext()).getShowPixelBar()) {
            return;
        }
        IPreferenceProvider sharedPreferences = PreferenceProvider.INSTANCE.getPreferences(getContext());
        applyVoiceSearchPreference();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        getContext().registerReceiver(packageChangedReceiver, Util.createIntentFilter("android.intent.action.PACKAGE_CHANGED"));
        initializeQsbConnector();
        applyVisibility();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            Utilities.getPrefs(getContext()).unregisterOnSharedPreferenceChangeListener(this);
            getContext().unregisterReceiver(packageChangedReceiver);
        } catch (IllegalArgumentException ignored) {
            // Not supposed to happen but we'll ignore it
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        if (FeatureFlags.KEY_SHOW_VOICE_SEARCH_BUTTON.equals(str) ||
                FeatureFlags.KEY_PREF_WHITE_GOOGLE_ICON.equals(str) ||
                FeatureFlags.KEY_SHOW_SEARCH_PILL.equals(str)) {
            applyVoiceSearchPreference();
            applyVisibility();
        }
    }

    private void initializeQsbConnector() {
        DeviceProfile deviceProfile = mLauncher.getDeviceProfile();
        if (qsbConnector == null && !Utilities.getPrefs(getContext()).getUseFullWidthSearchBar()
                && Utilities.getPrefs(mLauncher).getShowGoogleNowTab() && !deviceProfile.isLandscape && !deviceProfile.isTablet) {
            qsbConnector = (QsbConnector) mLauncher.getLayoutInflater().inflate(R.layout.qsb_connector, this, false);
            addView(qsbConnector, 0);
        } else if (Utilities.getPrefs(getContext()).getUseFullWidthSearchBar() || !Utilities.getPrefs(mLauncher).getShowGoogleNowTab()) {
            removeView(qsbConnector);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mic_icon:
                startQsbActivity(VOICE_ASSIST);
                break;
            default:
                if (BuildConfig.ENABLE_LAWNFEED) {
                    if (mLauncher.isClientConnected()) {
                        ((LawnfeedClient) mLauncher.getClient()).onQsbClick(bm("com.google.nexuslauncher.FAST_TEXT_SEARCH"), new Receiver(this));
                    } else {
                        startQsbActivity(TEXT_ASSIST);
                    }
                } else {
                    getContext().sendOrderedBroadcast(bm("com.google.nexuslauncher.FAST_TEXT_SEARCH"), null, new C0287l(this), null, 0, null, null);
                }
                break;
        }

    }

    private Intent bm(String str) {
        int[] iArr = new int[2];
        mQsbView.getLocationOnScreen(iArr);
        Rect rect = new Rect(iArr[0], iArr[1], iArr[0] + mQsbView.getWidth(), iArr[1] + mQsbView.getHeight());
        Intent intent = new Intent(str);
        aL(rect, intent);
        intent.setSourceBounds(rect);
        View micIcon = findViewById(R.id.mic_icon);
        if (micIcon != null) {
            intent.putExtra("source_mic_offset", bn(micIcon, rect));
        }
        return intent
                .putExtra("source_round_left", true)
                .putExtra("source_round_right", true)
                .putExtra("source_logo_offset", bn(findViewById(R.id.g_icon), rect))
                .setPackage("com.google.android.googlequicksearchbox");//.addFlags(1342177280);
    }

    private Point bn(View view, Rect rect) {
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        Point point = new Point();
        point.x = (iArr[0] - rect.left) + (view.getWidth() / 2);
        point.y = (iArr[1] - rect.top) + (view.getHeight() / 2);
        return point;
    }

    protected void aL(Rect rect, Intent intent) {
    }

    private void bq() {
        if (hasWindowFocus()) {
            bM = true;
        } else {
            hideQsb();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus && bM) {
            hideQsb();
        } else if (hasWindowFocus) {
            showQsb(true);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        showQsb(false);
    }

    private void hideQsb() {
        bM = false;
        qsbHidden = true;
        if (mQsbView != null) {
            mQsbView.setAlpha(0.0f);
            if (elevationAnimator != null && elevationAnimator.isRunning()) {
                elevationAnimator.end();
            }
        }
        if (qsbConnector != null) {
            qsbConnector.setAlpha(0.0f);
        }
    }

    private void showQsb(boolean animated) {
        bM = false;
        if (qsbHidden) {
            qsbHidden = false;
            if (mQsbView != null) {
                mQsbView.setAlpha(1.0f);
                if (elevationAnimator != null) {
                    elevationAnimator.start();
                    if (!animated) {
                        elevationAnimator.end();
                    }
                }
            }
            if (qsbConnector != null) {
                qsbConnector.setAlpha(1.0f);
                qsbConnector.bc(animated);
            }
        }
    }

    private void startQsbActivity(String str) {
        try {
            getContext().startActivity(new Intent(str).addFlags(268468224).setPackage("com.google.android.googlequicksearchbox"));
        } catch (ActivityNotFoundException e) {
            LauncherAppsCompat.getInstance(getContext()).showAppDetailsForProfile(new ComponentName("com.google.android.googlequicksearchbox", ".SearchActivity"), Utilities.myUserHandle());
        }
    }

    private void applyVisibility() {
        boolean isQsbAppEnabled = PackageManagerHelper.isAppEnabled(getContext().getPackageManager(), "com.google.android.googlequicksearchbox", 0) && (Utilities.getPrefs(getContext()).getShowSearchPill() || Utilities.getPrefs(getContext()).getUseFullWidthSearchBar());
        int visibility = isQsbAppEnabled ? View.VISIBLE : View.GONE;

        if (mQsbView != null) {
            mQsbView.setVisibility(visibility);
        }

        if (qsbConnector != null) {
            qsbConnector.setVisibility(visibility);
        }
    }

    public void translateBlurX(int translationX) {
    }

    public void translateBlurY(int translationY) {
    }

    final class C0287l extends BroadcastReceiver {
        final /* synthetic */ BaseQsbView qsbView;

        C0287l(BaseQsbView qsbView) {
            this.qsbView = qsbView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == 0) {
                qsbView.startQsbActivity(BaseQsbView.TEXT_ASSIST);
            } else {
                qsbView.bq();
            }
        }
    }

    final class Receiver implements LawnfeedClient.QsbReceiver {

        private final BaseQsbView qsbView;

        Receiver(BaseQsbView qsbView) {
            this.qsbView = qsbView;
        }

        @Override
        public void onResult(int resultCode) {
            if (resultCode == 0) {
                qsbView.startQsbActivity(BaseQsbView.TEXT_ASSIST);
            } else {
                qsbView.bq();
            }
        }
    }

    final class PackageChangedReceiver extends BroadcastReceiver {
        final /* synthetic */ BaseQsbView cp;

        PackageChangedReceiver(BaseQsbView qsbView) {
            cp = qsbView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            cp.applyVisibility();
        }
    }
}