package com.lody.virtual.helper.compat;

import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.internal.content.ReferrerIntent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Lody
 */

public class IApplicationThreadCompat {

	public static void scheduleCreateService(IApplicationThread appThread, IBinder token, ServiceInfo info,
			int processState) throws RemoteException {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			appThread.scheduleCreateService(token, info, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO,
						processState);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleCreateService",
						IBinder.class, ServiceInfo.class, CompatibilityInfo.class);
				scheduleCreateService.setAccessible(true);
				scheduleCreateService.invoke(appThread, token, info, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
			} catch (Throwable e) {
				handleException(e);
			}
		} else {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleCreateService",
						IBinder.class, ServiceInfo.class);
				scheduleCreateService.setAccessible(true);
				scheduleCreateService.invoke(appThread, token, info);
			} catch (Throwable e) {
				handleException(e);
			}
		}

	}

	public static void scheduleBindService(IApplicationThread appThread, IBinder token, Intent intent, boolean rebind,
			int processState) throws RemoteException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			appThread.scheduleBindService(token, intent, rebind, processState);
		} else {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleBindService",
						IBinder.class, Intent.class, boolean.class);
				scheduleCreateService.invoke(appThread, token, intent, rebind);
			} catch (Throwable e) {
				handleException(e);
			}
		}
	}

	public static void scheduleUnbindService(IApplicationThread appThread, IBinder token, Intent intent) throws RemoteException {
		appThread.scheduleUnbindService(token, intent);
	}

	public static void scheduleServiceArgs(IApplicationThread appThread, IBinder token, boolean taskRemoved,
			int startId, int flags, Intent args) throws RemoteException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			appThread.scheduleServiceArgs(token, taskRemoved, startId, flags, args);
		} else {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleServiceArgs",
						IBinder.class, int.class, int.class, Intent.class);
				scheduleCreateService.invoke(appThread, token, startId, flags, args);
			} catch (Throwable e) {
				handleException(e);
			}
		}
	}

	private static void handleException(Throwable e) throws RemoteException {
		if (e instanceof InvocationTargetException) {
			Throwable cause = e.getCause();
			if (cause instanceof RemoteException) {
				throw (RemoteException) cause;
			} else {
				cause.printStackTrace();
				throw new RemoteException(cause.getMessage());
			}
		}
	}

	public static void scheduleStopService(IApplicationThread appThread, IBinder token) throws RemoteException {
		appThread.scheduleStopService(token);
	}

	public static void scheduleNewIntent(IApplicationThread appThread, List intents, IBinder token) throws RemoteException {
			// noinspection unchecked
		appThread.scheduleNewIntent((List<ReferrerIntent>) intents, token);
	}
}
