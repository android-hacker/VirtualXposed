package io.virtualapp.delegate;

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

/**
 * author: weishu on 18/3/10.
 */
public class MyCrashHandler implements CrashHandler {

    private static final String TAG = "XApp";

    @Override
    public void handleUncaughtException(Thread t, Throwable e) {
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

        Crashlytics.logException(e);

        Log.i(TAG, "uncaught :" + t, e);

        if (t == Looper.getMainLooper().getThread()) {
            System.exit(0);
        } else {
            Log.e(TAG, "ignore uncaught exception of sub thread: " + t);
        }
    }
}
