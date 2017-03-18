package com.lody.virtual.server.pm;

import com.lody.virtual.helper.collection.ArrayMap;
import com.lody.virtual.server.pm.parser.PackageParserEx;
import com.lody.virtual.server.pm.parser.VPackage;

/**
 * @author Lody
 */

public class PackageCacheManager {

    static final ArrayMap<String, VPackage> PACKAGE_CACHE = new ArrayMap<>();

    public static int size() {
        synchronized (PACKAGE_CACHE) {
            return PACKAGE_CACHE.size();
        }
    }

    public static void put(VPackage pkg, PackageSetting ps) {
        synchronized (PackageCacheManager.class) {
            PackageParserEx.initApplicationInfoBase(ps, pkg);
            PACKAGE_CACHE.put(pkg.packageName, pkg);
            pkg.mExtras = ps;
            VPackageManagerService.get().analyzePackageLocked(pkg);
        }
    }

    public static VPackage get(String packageName) {
        synchronized (PackageCacheManager.class) {
            return PACKAGE_CACHE.get(packageName);
        }
    }

    public static PackageSetting getSetting(String packageName) {
        synchronized (PackageCacheManager.class) {
            VPackage p = PACKAGE_CACHE.get(packageName);
            if (p != null) {
                return (PackageSetting) p.mExtras;
            }
            return null;
        }
    }

    public static VPackage remove(String packageName) {
        synchronized (PackageCacheManager.class) {
            VPackageManagerService.get().deletePackageLocked(packageName);
            return PACKAGE_CACHE.remove(packageName);
        }
    }
}
