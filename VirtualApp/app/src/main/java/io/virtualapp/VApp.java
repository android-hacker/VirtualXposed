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
    private String[] PRE_INSTALL_PKG = {
            "com.google.android.gsf", "com.google.android.gsf.login", "com.google.android.gms", "com.android.vending"
    };

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
        preInstallPkgs();
    }

    private void preInstallPkgs() {
        if (VirtualCore.getCore().isMainProcess()) {
            for (String pkg : PRE_INSTALL_PKG) {
                if (!VirtualCore.getCore().isAppInstalled(pkg)) {
                    try {
                        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(pkg, 0);
                        String apkPath = applicationInfo.publicSourceDir;
                        VirtualCore.getCore().installApp(apkPath, InstallStrategy.COMPARE_VERSION);
                    } catch (PackageManager.NameNotFoundException e) {
                        // Ignore
                    }
                }
            }

        }
    }
}
