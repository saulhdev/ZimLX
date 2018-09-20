package org.zimmob.wallpaperpicker.tileinfo;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.zimmob.wallpaperpicker.BitmapRegionTileSource;
import org.zimmob.wallpaperpicker.BitmapRegionTileSource.BitmapSource;
import org.zimmob.wallpaperpicker.WallpaperPickerActivity;
import org.zimmob.wallpaperpicker.common.DialogUtils;
import org.zimmob.wallpaperpicker.common.InputStreamProvider;
import org.zimmob.wallpaperpicker.common.Utils;
import org.zimmob.wallpaperpicker.common.WallpaperManagerCompat;
import org.zimmob.zimlx.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileWallpaperInfo extends DrawableThumbWallpaperInfo {
    private static final String TAG = "FileWallpaperInfo";

    private final File mFile;

    public FileWallpaperInfo(File target, Drawable thumb) {
        super(thumb);
        mFile = target;
    }

    @Override
    public void onClick(final WallpaperPickerActivity a) {
        a.setWallpaperButtonEnabled(false);
        final BitmapRegionTileSource.FilePathBitmapSource bitmapSource =
                new BitmapRegionTileSource.FilePathBitmapSource(mFile, a);
        a.setCropViewTileSource(bitmapSource, false, true, null, () -> {
            if (bitmapSource.getLoadingState() == BitmapSource.State.LOADED) {
                a.setWallpaperButtonEnabled(true);
            }
        });
    }

    @Override
    public void onSave(final WallpaperPickerActivity a) {
        final InputStreamProvider isp = InputStreamProvider.fromUri(a, Uri.fromFile(mFile));
        AsyncTask<Integer, Void, Point> cropTask = new AsyncTask<Integer, Void, Point>() {

            @Override
            protected Point doInBackground(Integer... params) {
                InputStream is = null;
                try {
                    Point bounds = isp.getImageBounds();
                    if (bounds == null) {
                        Log.w(TAG, "Error loading image bounds");
                        return null;
                    }
                    is = isp.newStreamNotNull();
                    WallpaperManagerCompat.getInstance(a).setStream(is, null, true, params[0]);
                    return bounds;
                } catch (IOException e) {
                    Log.w(TAG, "cannot write stream to wallpaper", e);
                } finally {
                    Utils.closeSilently(is);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Point bounds) {
                if (bounds != null) {
                    a.setBoundsAndFinish(a.getWallpaperParallaxOffset() == 0f);
                } else {
                    Toast.makeText(a, R.string.wallpaper_set_fail, Toast.LENGTH_SHORT).show();
                }
            }
        };

        DialogUtils.executeCropTaskAfterPrompt(a, cropTask, a.getOnDialogCancelListener());
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isNamelessWallpaper() {
        return true;
    }
}