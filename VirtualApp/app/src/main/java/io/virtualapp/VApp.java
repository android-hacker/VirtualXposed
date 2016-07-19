package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;

import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
public class VApp extends Application {

    private static VApp gDefault;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            VirtualCore.getCore().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
//        BlockCanary.install(this, new AppBlockCanaryContext()).start();
        if (VirtualCore.getCore().isMainProcess()) {
            Once.initialise(this);
        }
        installGms();
    }

    private void installGms() {
        if (VirtualCore.getCore().isMainProcess()) {
            String gmsPkg = "com.google.android.gms";
            if (!VirtualCore.getCore().isAppInstalled(gmsPkg)) {
                try {
                    ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(gmsPkg, 0);
                    String apkPath = applicationInfo.publicSourceDir;
                    VirtualCore.getCore().installApp(apkPath, InstallStrategy.COMPARE_VERSION);
                } catch (PackageManager.NameNotFoundException e) {
                    // Ignore
                }
            }
        }
    }
}
