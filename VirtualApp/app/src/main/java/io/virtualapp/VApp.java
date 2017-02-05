package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.github.moduth.blockcanary.BlockCanary;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.utils.VLog;

import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
public class VApp extends Application {

    private static final String[] GMS_PKG = {
            "com.android.vending",

            "com.google.android.gsf",
            "com.google.android.gsf.login",
            "com.google.android.gms",

            "com.google.android.backuptransport",
            "com.google.android.backup",
            "com.google.android.configupdater",
            "com.google.android.syncadapters.contacts",
            "com.google.android.feedback",
            "com.google.android.onetimeinitializer",
            "com.google.android.partnersetup",
            "com.google.android.setupwizard",
            "com.google.android.syncadapters.calendar",};

    private static VApp gDefault;

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            StubManifest.ENABLE_IO_REDIRECT = true;
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
        if (VirtualCore.get().isMainProcess()) {
            Once.initialise(this);
            installGms();
        } else if (VirtualCore.get().isVAppProcess()) {
            BlockCanary.install(this, new AppBlockCanaryContext());
            VirtualCore.get().setComponentDelegate(new MyComponentDelegate());
            VirtualCore.get().setPhoneInfoDelegate(new MyPhoneInfoDelegate());
            VirtualCore.get().setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
        }
    }

    private void installGms() {
        PackageManager pm = VirtualCore.getPM();
        for (String pkg : GMS_PKG) {
            if (VirtualCore.get().isAppInstalled(pkg)) {
                continue;
            }
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                String apkPath = appInfo.sourceDir;
                InstallResult res = VirtualCore.get().installApp(apkPath,
                        InstallStrategy.DEPEND_SYSTEM_IF_EXIST | InstallStrategy.TERMINATE_IF_EXIST);
                if (!res.isSuccess) {
                    VLog.e("VApp", "Unable to install app %s: %s.", appInfo.packageName, res.error);
                }
            } catch (Throwable e) {
                // Ignore
            }
        }
    }

}
