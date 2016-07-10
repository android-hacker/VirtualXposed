package com.lody.virtual.helper.compat;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.res.CompatibilityInfo;
import android.os.Build;

/**
 * @author Lody
 *
 */
public class ActivityThreadCompat {

	public static LoadedApk getPackageInfoNoCheck(ApplicationInfo appInfo) {

		ActivityThread mainThread = VirtualCore.mainThread();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			try {
				return getPackageInfoNoCheckV14(mainThread, appInfo);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			try {
				return getPackageInfoNoCheckV7(mainThread, appInfo);
			} catch (Throwable e) {
				e.printStackTrace();
				// Ignore
			}
		}

		throw new RuntimeException("Unable to getPackageInfo(%s)." + appInfo);
	}

	private static LoadedApk getPackageInfoNoCheckV7(ActivityThread mainThread, ApplicationInfo appInfo)
			throws Throwable {
		Method m_getPackageInfoNoCheck = ActivityThread.class.getDeclaredMethod("getPackageInfoNoCheck",
				ApplicationInfo.class);
		if (!m_getPackageInfoNoCheck.isAccessible()) {
			m_getPackageInfoNoCheck.setAccessible(true);
		}
		return (LoadedApk) m_getPackageInfoNoCheck.invoke(mainThread, appInfo);
	}

	private static LoadedApk getPackageInfoNoCheckV14(ActivityThread mainThread, ApplicationInfo appInfo)
			throws Throwable {
		return mainThread.getPackageInfoNoCheck(appInfo, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
	}

	public static void handleBindApplication(Object bindData) {
		ActivityThread mainThread = VirtualCore.mainThread();
		Reflect.on(mainThread).call("handleBindApplication", bindData);
	}

	public static void installContentProviders(Context context, List<ProviderInfo> providers) {
		ActivityThread mainThread = VirtualCore.mainThread();
		Reflect.on(mainThread).call("installContentProviders", context, providers);
	}

	public static Object getBoundApplication(ActivityThread mainThread) {
		return Reflect.on(mainThread).get("mBoundApplication");
	}
}
