package org.zimmob.zimlx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.Tool;

public class ShortcutReceiver extends BroadcastReceiver {

    public ShortcutReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() == null) return;

        String name = intent.getExtras().getString(Intent.EXTRA_SHORTCUT_NAME);
        Intent newIntent = (Intent) intent.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT);
        Drawable shortcutIconDrawable = null;

        try {
            Parcelable iconResourceParcelable = intent.getExtras().getParcelable(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (iconResourceParcelable != null && iconResourceParcelable instanceof Intent.ShortcutIconResource) {
                Intent.ShortcutIconResource iconResource = (Intent.ShortcutIconResource) iconResourceParcelable;
                Resources resources = context.getPackageManager().getResourcesForApplication(iconResource.packageName);
                if (resources != null) {
                    int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    shortcutIconDrawable = resources.getDrawable(id);
                }
            }
        }
        catch (Exception ignore) {
        }
        finally {
            if (shortcutIconDrawable == null)
                shortcutIconDrawable = new BitmapDrawable(context.getResources(), (Bitmap) intent.getExtras().getParcelable(Intent.EXTRA_SHORTCUT_ICON));
        }

        App app = Setup.appLoader().createApp(newIntent);
        Item item;
        if (app != null) {
            item = Item.newAppItem(app);
        } else {
            item = Item.newShortcutItem(newIntent, shortcutIconDrawable, name);
        }
        Point preferredPos = HomeActivity.companion
                .getLauncher()
                .getDesktop()
                .getPages()
                .get(HomeActivity.companion
                        .getLauncher()
                        .getDesktop()
                        .getCurrentItem()).findFreeSpace();
        if (preferredPos == null) {
            Tool.toast(HomeActivity.companion.getLauncher(), R.string.toast_not_enough_space);
        } else {
            item.setX(preferredPos.x);
            item.setY(preferredPos.y);
            HomeActivity.companion.getDb().saveItem(item, HomeActivity.companion.getLauncher().getDesktop().getCurrentItem(), Config.ItemPosition.Desktop);
            boolean added = HomeActivity.companion.getLauncher().getDesktop().addItemToPage(item, HomeActivity.companion.getLauncher().getDesktop().getCurrentItem());

            Setup.logger().log(this, Log.INFO, null, "Shortcut installed - %s => Intent: %s (Item type: %s; x = %d, y = %d, added = %b)", name, newIntent, item.getType(), item.getX(), item.getY(), added);
        }
    }
}
