package io.virtualapp.delegate;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.util.Log;

import com.lody.virtual.client.core.CrashHandler;

/**
 * author: weishu on 18/3/10.
 */
public class BaseCrashHandler implements CrashHandler {

    protected static final String TAG = "XApp";

    @SuppressLint("ApplySharedPref")
    @Override
    public void handleUncaughtException(Thread t, Throwable e) {


        if (t == Looper.getMainLooper().getThread()) {
            System.exit(0);
        } else {
            Log.e(TAG, "ignore uncaught exception of sub thread: " + t);
        }
    }
}
