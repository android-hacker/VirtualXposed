package com.lody.virtual.client.hook.secondary;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.server.pm.VAppManagerService;

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

    private static void installPackages(List<String> list, int userId) {
        VAppManagerService service = VAppManagerService.get();
        for (String packageName : list) {
            if (service.isAppInstalledAsUser(userId, packageName)) {
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
                service.installPackage(info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST, false);
            } else {
                service.installPackageAsUser(userId, packageName);
            }
        }
    }

    public static void installGms(int userId) {
        installPackages(GOOGLE_SERVICE, userId);
        installPackages(GOOGLE_APP, userId);
    }
}
