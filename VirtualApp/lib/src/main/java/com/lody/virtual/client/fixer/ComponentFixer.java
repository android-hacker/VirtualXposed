package com.lody.virtual.client.fixer;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.utils.collection.ArrayMap;
import com.lody.virtual.os.VEnvironment;

import mirror.android.content.pm.ApplicationInfoL;
import mirror.android.content.pm.ApplicationInfoN;

/**
 * @author Lody
 */

public class ComponentFixer {

	private static final ArrayMap<String, String[]> sSharedLibCache = new ArrayMap<>();


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
			ApplicationInfoL.scanSourceDir.set(applicationInfo, applicationInfo.dataDir);
			ApplicationInfoL.scanPublicSourceDir.set(applicationInfo, applicationInfo.dataDir);
            String hostPrimaryCpuAbi = ApplicationInfoL.primaryCpuAbi.get(VirtualCore.get().getContext().getApplicationInfo());
			ApplicationInfoL.primaryCpuAbi.set(applicationInfo, hostPrimaryCpuAbi);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			ApplicationInfoN.deviceEncryptedDataDir.set(applicationInfo, applicationInfo.dataDir);
			ApplicationInfoN.deviceProtectedDataDir.set(applicationInfo, applicationInfo.dataDir);
			ApplicationInfoN.credentialEncryptedDataDir.set(applicationInfo, applicationInfo.dataDir);
			ApplicationInfoN.credentialProtectedDataDir.set(applicationInfo, applicationInfo.dataDir);
		}
		applicationInfo.enabled = true;
		applicationInfo.nativeLibraryDir = setting.libPath;
		applicationInfo.dataDir = VEnvironment.getDataUserPackageDirectory(userId, setting.packageName).getPath();
		applicationInfo.uid = setting.appId;
		if (setting.dependSystem) {
			String[] sharedLibraryFiles = sSharedLibCache.get(setting.packageName);
			if (sharedLibraryFiles == null) {
				PackageManager hostPM = VirtualCore.get().getUnHookPackageManager();
				try {
					ApplicationInfo hostInfo = hostPM.getApplicationInfo(setting.packageName, PackageManager.GET_SHARED_LIBRARY_FILES);
					sharedLibraryFiles = hostInfo.sharedLibraryFiles;
					if (sharedLibraryFiles == null) sharedLibraryFiles = new String[0];
					sSharedLibCache.put(setting.packageName, sharedLibraryFiles);
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
			}
			applicationInfo.sharedLibraryFiles = sharedLibraryFiles;
		}
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
