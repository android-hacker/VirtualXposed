package com.lody.virtual.helper.compat;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.os.Build;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
public class ActivityThreadCompat {

	private static Method installProvider;

	static {
		try {
			if (Build.VERSION.SDK_INT <= 15) {
				installProvider = ActivityThread.class.getDeclaredMethod("installProvider", Context.class,
						IActivityManager.ContentProviderHolder.class, ProviderInfo.class, boolean.class, boolean.class);
			} else {
				installProvider = ActivityThread.class.getDeclaredMethod("installProvider", Context.class,
						IActivityManager.ContentProviderHolder.class, ProviderInfo.class, boolean.class, boolean.class,
						boolean.class);
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public static void handleBindApplication(Object bindData) {
		ActivityThread mainThread = VirtualCore.mainThread();
		Reflect.on(mainThread).call("handleBindApplication", bindData);
	}

	public static IActivityManager.ContentProviderHolder installProvider(Context context, ProviderInfo providerInfo) {
		if (!installProvider.isAccessible()) {
			installProvider.setAccessible(true);
		}
		ActivityThread activityThread = VirtualCore.mainThread();
		try {
			if (Build.VERSION.SDK_INT <= 15) {
				return (IActivityManager.ContentProviderHolder) installProvider.invoke(activityThread, context, null,
						providerInfo, false, true);
			} else {
				return (IActivityManager.ContentProviderHolder) installProvider.invoke(activityThread, context, null,
						providerInfo, false, true, true);
			}
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object getBoundApplication(ActivityThread mainThread) {
		return Reflect.on(mainThread).get("mBoundApplication");
	}

	public static Application getCurrentApplication() {
		Application application = null;
		try {
			application = ActivityThread.currentApplication();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (application == null) {
			application = Reflect.on(VirtualCore.mainThread()).get("mInitialApplication");
		}
		return application;
	}
}
