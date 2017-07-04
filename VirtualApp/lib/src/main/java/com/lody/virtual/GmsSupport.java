package com.lody.virtual;

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

    public static boolean isGmsFamilyPackage(String packageName) {
        return packageName.equals("com.android.vending")
                || packageName.equals("com.google.android.gms");
    }

    public static boolean isGoogleFrameworkInstalled() {
        return VirtualCore.get().isAppInstalled("com.google.android.gms");
    }

}