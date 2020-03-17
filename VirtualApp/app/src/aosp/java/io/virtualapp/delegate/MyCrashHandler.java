package io.virtualapp.delegate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.InstalledAppInfo;
import com.microsoft.appcenter.crashes.Crashes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author weishu
 * @date 2019/2/25.
 */
public class MyCrashHandler extends BaseCrashHandler {
    private static final String CRASH_SP = "vxp_crash";
    private static final String KEY_LAST_CRASH_TIME = "last_crash_time";
    private static final String KEY_LAST_CRASH_TYPE = "last_crash_type";

    @Override
    public void handleUncaughtException(Thread t, Throwable e) {
        SharedPreferences sp = VirtualCore.get().getContext().getSharedPreferences(CRASH_SP, Context.MODE_MULTI_PROCESS);

        Map<String, String> properties = new HashMap<>();
        try {
            ApplicationInfo currentApplicationInfo = VClientImpl.get().getCurrentApplicationInfo();
            if (currentApplicationInfo != null) {
                String packageName = currentApplicationInfo.packageName;
                String processName = currentApplicationInfo.processName;

                properties.put("process", processName);
                properties.put("package", packageName);

                int userId = VUserHandle.myUserId();

                properties.put("uid", String.valueOf(userId));

                InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(packageName, 0);
                if (installedAppInfo != null) {
                    PackageInfo packageInfo = installedAppInfo.getPackageInfo(userId);
                    if (packageInfo != null) {
                        String versionName = packageInfo.versionName;
                        int versionCode = packageInfo.versionCode;

                        properties.put("versionName", versionName);
                        properties.put("versionCode", String.valueOf(versionCode));

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
            Crashes.trackError(e, properties, null);
        }

        Log.i(TAG, "uncaught :" + t, e);

        // must commit.
        sp.edit().putLong(KEY_LAST_CRASH_TIME, now).putString(KEY_LAST_CRASH_TYPE, exceptionType).commit();

        super.handleUncaughtException(t, e);
    }
}
