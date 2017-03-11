package com.lody.virtual.server.pm;

import android.content.pm.PackageParser;

import com.lody.virtual.helper.collection.ArrayMap;

/**
 * @author Lody
 */

public class PackageCache {

    static final ArrayMap<String, PackageParser.Package> PACKAGE_CACHE = new ArrayMap<>();

    public static int size() {
        synchronized (PACKAGE_CACHE) {
            return PACKAGE_CACHE.size();
        }
    }

    public static void put(PackageParser.Package pkg, PackageSetting setting) {
        synchronized (PackageCache.class) {
            pkg.mExtras = setting;
            PACKAGE_CACHE.put(pkg.packageName, pkg);
            VPackageManagerService.get().analyzePackageLocked(pkg);
        }
    }

    public static PackageParser.Package get(String packageName) {
        synchronized (PackageCache.class) {
            return PACKAGE_CACHE.get(packageName);
        }
    }

    public static PackageSetting getSetting(String packageName) {
        synchronized (PackageCache.class) {
            PackageParser.Package p = PACKAGE_CACHE.get(packageName);
            if (p != null) {
                return (PackageSetting) p.mExtras;
            }
            return null;
        }
    }

    public static PackageParser.Package remove(String packageName) {
        synchronized (PackageCache.class) {
            VPackageManagerService.get().deletePackageLocked(packageName);
            return PACKAGE_CACHE.remove(packageName);
        }
    }
}
