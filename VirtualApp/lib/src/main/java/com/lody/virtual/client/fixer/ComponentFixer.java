package com.lody.virtual.client.fixer;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.ComponentUtils;

/**
 * @author Lody
 */

public class ComponentFixer {


	public static void fixApplicationInfo(AppInfo info, ApplicationInfo applicationInfo) {
		if (TextUtils.isEmpty(applicationInfo.processName)) {
			applicationInfo.processName = applicationInfo.packageName;
		}
		applicationInfo.name = fixComponentClassName(info.packageName, applicationInfo.name);
		applicationInfo.publicSourceDir = info.apkPath;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
			applicationInfo.flags &= -ApplicationInfo.FLAG_STOPPED;
		}
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
		applicationInfo.sourceDir = info.apkPath;
		applicationInfo.dataDir = info.dataDir;
		applicationInfo.enabled = true;
		applicationInfo.nativeLibraryDir = info.libDir;
		applicationInfo.uid = Process.myUid();
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

	public static void fixComponentInfo(AppInfo appInfo, ComponentInfo info) {
		if (info != null) {
			if (TextUtils.isEmpty(info.processName)) {
				info.processName = info.packageName;
			}
			fixApplicationInfo(appInfo, info.applicationInfo);
			info.name = fixComponentClassName(info.packageName, info.name);
		}
	}

	public static void fixUid(ApplicationInfo applicationInfo) {
		if (false && VClientImpl.getClient().isBound() && applicationInfo != null) {
			String packageName = applicationInfo.packageName;
			if (packageName.equals(VClientImpl.getClient().geCurrentPackage())
					|| ComponentUtils.isSharedPackage(packageName)) {
				applicationInfo.uid = Process.myUid();
			} else {
				applicationInfo.uid = 99999;
			}
		}
	}
}
