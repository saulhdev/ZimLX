package com.android.launcher3.ui;

import android.content.pm.LauncherActivityInfo;

import com.android.launcher3.util.Condition;
import com.android.launcher3.util.Wait;
import com.android.launcher3.util.rule.LauncherActivityRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertTrue;

/**
 * Test for verifying apps is launched from all-apps
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AllAppsAppLaunchTest extends AbstractLauncherUiTest {

    @Rule
    public LauncherActivityRule mActivityMonitor = new LauncherActivityRule();

    @Test
    public void testAppLauncher_portrait() throws Exception {
        lockRotation(true);
        performTest();
    }

    @Test
    public void testAppLauncher_landscape() throws Exception {
        lockRotation(false);
        performTest();
    }

    private void performTest() {
        mActivityMonitor.startLauncher();

        LauncherActivityInfo settingsApp = getSettingsApp();

        // Open all apps and wait for load complete
        final UiObject2 appsContainer = openAllApps();
        assertTrue(Wait.atMost(Condition.minChildCount(appsContainer, 2), DEFAULT_UI_TIMEOUT));

        // Open settings app and verify app launched
        scrollAndFind(appsContainer, By.text(settingsApp.getLabel().toString())).click();
        assertTrue(mDevice.wait(Until.hasObject(By.pkg(
                settingsApp.getComponentName().getPackageName()).depth(0)), DEFAULT_UI_TIMEOUT));
    }
}
