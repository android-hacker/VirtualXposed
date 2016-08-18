package com.lody.virtual.helper.utils;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;

import com.lody.virtual.client.VClientImpl;

/**
 * @author Lody
 *
 */
public class ComponentUtils {

	public static String getProcessName(ComponentInfo componentInfo) {
		String processName = componentInfo.processName;
		if (processName == null) {
			processName = componentInfo.packageName;
			componentInfo.processName = processName;
		}
		return processName;
	}

	public static String getTaskAffinity(ActivityInfo activityInfo) {
		int uid = activityInfo.applicationInfo.uid;
		if (activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
			return "SINGLE_INSTANCE_" + activityInfo.packageName + "/" + activityInfo.name + ":" + uid;
		}
		if (activityInfo.taskAffinity == null && activityInfo.applicationInfo.taskAffinity == null) {
			return activityInfo.packageName+ ":" + uid;
		}
		if (activityInfo.taskAffinity != null) {
			return activityInfo.taskAffinity+ ":" + uid;
		}
		return activityInfo.applicationInfo.taskAffinity+ ":" + uid;
	}

	public static boolean isSameComponent(ComponentInfo first, ComponentInfo second) {

		if (first != null && second != null && (first.applicationInfo.uid == second.applicationInfo.uid)) {
			String pkg1 = first.packageName + "";
			String pkg2 = second.packageName + "";
			String name1 = first.name + "";
			String name2 = second.name + "";
			return pkg1.equals(pkg2) && name1.equals(name2);
		}
		return false;
	}

	public static ComponentName toComponentName(ComponentInfo componentInfo) {
		return new ComponentName(componentInfo.packageName, componentInfo.name);
	}

	public static boolean isSystemApp(PackageInfo packageInfo) {
		return packageInfo != null && packageInfo.applicationInfo != null
				&& (ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0;
	}

	public static boolean isSystemApp(ApplicationInfo applicationInfo) {
		return applicationInfo != null && (ApplicationInfo.FLAG_SYSTEM & applicationInfo.flags) != 0;
	}


	public static boolean isSharedPackage(String packageName) {
		VClientImpl client = VClientImpl.getClient();
		if (packageName == null || !client.isBound()) {
			return false;
		}
		if (client.getCurrentPackage().equals(packageName)) {
			return true;
		}
		if (packageName.equals("com.android.vending")) {
			return true;
		}
		return client.getSharedPackages().contains(packageName);
	}
}
