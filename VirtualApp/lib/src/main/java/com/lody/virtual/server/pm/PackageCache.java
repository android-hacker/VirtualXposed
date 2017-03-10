package com.lody.virtual.server.pm;

import android.content.pm.PackageParser;

import com.lody.virtual.helper.utils.collection.ArrayMap;

/**
 * @author Lody
 */

public class PackageCache {

    static final ArrayMap<String, PackageParser.Package> sPackageCaches = new ArrayMap<>();


    public static void put(PackageParser.Package pkg, PackageSetting setting) {
        synchronized (PackageCache.class) {
            pkg.mExtras = setting;
            sPackageCaches.put(pkg.packageName, pkg);
            VPackageManagerService.get().analyzePackageLocked(pkg);
        }
    }

    public static PackageParser.Package get(String packageName) {
        synchronized (PackageCache.class) {
            return sPackageCaches.get(packageName);
        }
    }

    public static PackageSetting getSetting(String packageName) {
        synchronized (PackageCache.class) {
            PackageParser.Package p = sPackageCaches.get(packageName);
            if (p != null) {
                return (PackageSetting) p.mExtras;
            }
            return null;
        }
    }

    public static PackageParser.Package remove(String packageName) {
        synchronized (PackageCache.class) {
            VPackageManagerService.get().deletePackageLocked(packageName);
            return sPackageCaches.remove(packageName);
        }
    }
}
