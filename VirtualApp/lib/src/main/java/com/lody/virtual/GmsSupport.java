package com.lody.virtual;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lody
 */
public class GmsSupport {

    private static final List<String> GOOGLE_APP = Arrays.asList(
            "com.android.vending",
            "com.google.android.play.games",
            "com.google.android.wearable.app",
            "com.google.android.wearable.app.cn"
    );

    private static final List<String> GOOGLE_SERVICE = Arrays.asList(
            "com.google.android.gsf",
            "com.google.android.gms",
            "com.google.android.gsf.login",
            "com.google.android.backuptransport",
            "com.google.android.backup",
            "com.google.android.configupdater",
            "com.google.android.syncadapters.contacts",
            "com.google.android.feedback",
            "com.google.android.onetimeinitializer",
            "com.google.android.partnersetup",
            "com.google.android.setupwizard",
            "com.google.android.syncadapters.calendar"
    );

    public static boolean isGmsFamilyPackage(String packageName) {
        return packageName.equals("com.android.vending")
                || packageName.equals("com.google.android.gms");
    }

    public static boolean isGoogleFrameworkInstalled() {
        return VirtualCore.get().isAppInstalled("com.google.android.gms");
    }

    public static boolean isOutsideGoogleFrameworkExist() {
        return VirtualCore.get().isOutsideInstalled("com.google.android.gms");
    }

    private static void installPackages(List<String> list, int userId) {
        VirtualCore core = VirtualCore.get();
        for (String packageName : list) {
            if (core.isAppInstalledAsUser(userId, packageName)) {
                continue;
            }
            ApplicationInfo info = null;
            try {
                info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
            if (info == null || info.sourceDir == null) {
                continue;
            }
            if (userId == 0) {
                core.installPackage(info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
            } else {
                core.installPackageAsUser(userId, packageName);
            }
        }
    }

    public static void installGApps(int userId) {
        installPackages(GOOGLE_SERVICE, userId);
        installPackages(GOOGLE_APP, userId);
    }

    public static void installGoogleService(int userId) {
        installPackages(GOOGLE_SERVICE, userId);
    }

    public static void installGoogleApp(int userId) {
        installPackages(GOOGLE_APP, userId);
    }
}