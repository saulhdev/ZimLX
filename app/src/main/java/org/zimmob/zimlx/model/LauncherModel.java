package org.zimmob.zimlx.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;

import org.zimmob.zimlx.DeferredHandler;
import org.zimmob.zimlx.util.Thunk;

public class LauncherModel {
    @Thunk
    DeferredHandler mHandler = new DeferredHandler();

    @Thunk
    static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");

    static {
        sWorkerThread.start();
    }

    @Thunk
    static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    /**
     * Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler.
     */
    private void runOnMainThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r);
        } else {
            r.run();
        }
    }


    public LauncherModel() {

    }

    public void onPackageChanged(String packageName, UserHandle user) {
        //int op = PackageUpdatedTask.OP_UPDATE;
        //enqueueItemUpdatedTask(new PackageUpdatedTask(op, new String[]{packageName},user));
    }

    /**
     * @return the looper for the worker thread which can be used to start background tasks.
     */
    public static Looper getWorkerLooper() {
        return sWorkerThread.getLooper();
    }


}
