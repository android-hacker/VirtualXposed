package com.lody.virtual.client.fixer;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.text.TextUtils;

import com.lody.virtual.helper.proto.AppSettings;

/**
 * @author Lody
 */

public class ComponentFixer {


	public static void fixApplicationInfo(AppSettings info, ApplicationInfo applicationInfo) {
		applicationInfo.flags |= ApplicationInfo.FLAG_HAS_CODE;
		if (TextUtils.isEmpty(applicationInfo.processName)) {
			applicationInfo.processName = applicationInfo.packageName;
		}
		applicationInfo.name = fixComponentClassName(info.packageName, applicationInfo.name);
		applicationInfo.publicSourceDir = info.apkPath;
		applicationInfo.sourceDir = info.apkPath;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				applicationInfo.splitSourceDirs = new String[]{info.apkPath};
				applicationInfo.splitPublicSourceDirs = applicationInfo.splitSourceDirs;
			}
		} catch (Throwable e) {
			// ignore
		}
		try {
			applicationInfo.scanSourceDir = applicationInfo.dataDir;
			applicationInfo.scanPublicSourceDir = applicationInfo.dataDir;
		} catch (Throwable e) {
			// Ignore
		}
		applicationInfo.dataDir = info.dataDir;
		applicationInfo.enabled = true;
		applicationInfo.nativeLibraryDir = info.libDir;
		applicationInfo.uid = info.uid;
	}

	public static String fixComponentClassName(String pkgName, String className) {
		if (className != null) {
			if (className.charAt(0) == '.') {
				return pkgName + className;
			}
			return className;
		}
		return null;
	}

	public static void fixComponentInfo(AppSettings appSettings, ComponentInfo info) {
		if (info != null) {
			if (TextUtils.isEmpty(info.processName)) {
				info.processName = info.packageName;
			}
			fixApplicationInfo(appSettings, info.applicationInfo);
			info.name = fixComponentClassName(info.packageName, info.name);
			if (info.processName == null) {
				info.processName = info.applicationInfo.processName;
			}
		}
	}

}
