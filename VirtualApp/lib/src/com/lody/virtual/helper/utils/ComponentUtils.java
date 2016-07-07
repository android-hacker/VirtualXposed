package com.lody.virtual.helper.utils;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;

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
		if (activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
			return "SINGLE_INSTANCE_" + activityInfo.packageName + "/" + activityInfo.name;
		}
		if (activityInfo.taskAffinity == null && activityInfo.applicationInfo.taskAffinity == null) {
			return activityInfo.packageName;
		}
		if (activityInfo.taskAffinity != null) {
			return activityInfo.taskAffinity;
		}
		return activityInfo.applicationInfo.taskAffinity;
	}

	public static boolean isSameComponent(ComponentInfo first, ComponentInfo second) {

		if (first != null && second != null) {
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
}
