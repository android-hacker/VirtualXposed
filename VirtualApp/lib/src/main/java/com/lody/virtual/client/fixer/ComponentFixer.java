package com.lody.virtual.client.fixer;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.os.VEnvironment;

import mirror.android.content.pm.ApplicationInfoL;

/**
 * @author Lody
 */

public class ComponentFixer {


	public static void fixApplicationInfo(AppSetting setting, ApplicationInfo applicationInfo, int userId) {
		applicationInfo.flags |= ApplicationInfo.FLAG_HAS_CODE;
		if (TextUtils.isEmpty(applicationInfo.processName)) {
			applicationInfo.processName = applicationInfo.packageName;
		}
		applicationInfo.name = fixComponentClassName(setting.packageName, applicationInfo.name);
		applicationInfo.publicSourceDir = setting.apkPath;
		applicationInfo.sourceDir = setting.apkPath;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			applicationInfo.splitSourceDirs = new String[]{setting.apkPath};
			applicationInfo.splitPublicSourceDirs = applicationInfo.splitSourceDirs;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ApplicationInfoL.scanSourceDir.set(applicationInfo, applicationInfo.dataDir);
			ApplicationInfoL.scanPublicSourceDir.set(applicationInfo, applicationInfo.dataDir);
		}
		applicationInfo.enabled = true;
		applicationInfo.nativeLibraryDir = setting.libPath;
		applicationInfo.dataDir = VEnvironment.getDataUserPackageDirectory(userId, setting.packageName).getPath();
		applicationInfo.uid = Process.myUid();
	}

	private static String fixComponentClassName(String pkgName, String className) {
		if (className != null) {
			if (className.charAt(0) == '.') {
				return pkgName + className;
			}
			return className;
		}
		return null;
	}

	public static void fixComponentInfo(AppSetting appSetting, ComponentInfo info, int userId) {
		if (info != null) {
			if (TextUtils.isEmpty(info.processName)) {
				info.processName = info.packageName;
			}
			fixApplicationInfo(appSetting, info.applicationInfo, userId);
			info.name = fixComponentClassName(info.packageName, info.name);
			if (info.processName == null) {
				info.processName = info.applicationInfo.processName;
			}
		}
	}

}
