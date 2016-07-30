package com.lody.virtual.service.pm;

import com.lody.virtual.helper.proto.AppInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 */

public class PackageCache {
	public static final Map<String, PackageParser.Package> sPackageCaches = new ConcurrentHashMap<>();
	public static final Map<String, AppInfo> sAppInfos = new ConcurrentHashMap<>();

	public static void put(PackageParser.Package pkg, AppInfo appInfo) {
		synchronized (PackageCache.class) {
			sPackageCaches.put(pkg.packageName, pkg);
			sAppInfos.put(pkg.packageName, appInfo);
			VPackageService.getService().analyzePackageLocked(appInfo, pkg);
		}
	}

	public static PackageParser.Package get(String packageName) {
		return sPackageCaches.get(packageName);
	}

	public static void remove(String packageName) {
		synchronized (PackageCache.class) {
			sPackageCaches.remove(packageName);
			sAppInfos.remove(packageName);
			VPackageService.getService().deletePackageLocked(packageName);
		}
	}
}
