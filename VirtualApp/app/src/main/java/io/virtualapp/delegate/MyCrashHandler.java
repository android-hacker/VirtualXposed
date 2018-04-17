package io.virtualapp.delegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Looper;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.CrashHandler;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.InstalledAppInfo;

import java.util.concurrent.TimeUnit;

/**
 * author: weishu on 18/3/10.
 */
public class MyCrashHandler implements CrashHandler {

    private static final String TAG = "XApp";
    private static final String CRASH_SP = "vxp_crash";
    private static final String KEY_LAST_CRASH_TIME = "last_crash_time";
    private static final String KEY_LAST_CRASH_TYPE = "last_crash_type";

    @SuppressLint("ApplySharedPref")
    @Override
    public void handleUncaughtException(Thread t, Throwable e) {
        SharedPreferences sp = VirtualCore.get().getContext().getSharedPreferences(CRASH_SP, Context.MODE_MULTI_PROCESS);

        try {
            ApplicationInfo currentApplicationInfo = VClientImpl.get().getCurrentApplicationInfo();
            if (currentApplicationInfo != null) {
                String packageName = currentApplicationInfo.packageName;
                String processName = currentApplicationInfo.processName;

                Crashlytics.setString("process", processName);
                Crashlytics.setString("package", packageName);

                int userId = VUserHandle.myUserId();

                Crashlytics.setInt("uid", userId);

                InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(packageName, 0);
                if (installedAppInfo != null) {
                    PackageInfo packageInfo = installedAppInfo.getPackageInfo(userId);
                    if (packageInfo != null) {
                        String versionName = packageInfo.versionName;
                        int versionCode = packageInfo.versionCode;

                        Crashlytics.setString("versionName", versionName);
                        Crashlytics.setInt("versionCode", versionCode);

                    }
                }
            }
        } catch (Throwable ignored) {
        }
        final String exceptionType = e.getClass().getName();
        final long now = System.currentTimeMillis();

        final long lastCrash = sp.getLong(KEY_LAST_CRASH_TIME, 0);
        final String lastCrashType = sp.getString(KEY_LAST_CRASH_TYPE, null);

        if (exceptionType.equals(lastCrashType) && (now - lastCrash) < TimeUnit.MINUTES.toMillis(1)) {
            // continues crash, do not upload
        } else {
            Crashlytics.logException(e);
        }

        Log.i(TAG, "uncaught :" + t, e);

        // must commit.
        sp.edit().putLong(KEY_LAST_CRASH_TIME, now).putString(KEY_LAST_CRASH_TYPE, exceptionType).commit();

        if (t == Looper.getMainLooper().getThread()) {
            System.exit(0);
        } else {
            Log.e(TAG, "ignore uncaught exception of sub thread: " + t);
        }
    }
}
