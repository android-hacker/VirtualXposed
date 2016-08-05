package com.lody.virtual.helper.compat;

import java.lang.reflect.Method;
import java.util.List;

import com.android.internal.content.ReferrerIntent;

import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Lody
 */

public class IApplicationThreadCompat {

	public static void scheduleCreateService(IApplicationThread appThread, IBinder token, ServiceInfo info,
			int processState) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				appThread.scheduleCreateService(token, info, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO,
						processState);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleCreateService",
						IBinder.class, ServiceInfo.class, CompatibilityInfo.class);
				scheduleCreateService.setAccessible(true);
				scheduleCreateService.invoke(appThread, token, info, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleCreateService",
						IBinder.class, ServiceInfo.class);
				scheduleCreateService.setAccessible(true);
				scheduleCreateService.invoke(appThread, token, info);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	public static void scheduleBindService(IApplicationThread appThread, IBinder token, Intent intent, boolean rebind,
			int processState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				appThread.scheduleBindService(token, intent, rebind, processState);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleBindService",
						IBinder.class, Intent.class, boolean.class);
				scheduleCreateService.invoke(appThread, token, intent, rebind);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public static void scheduleUnbindService(IApplicationThread appThread, IBinder token, Intent intent) {
		try {
			appThread.scheduleUnbindService(token, intent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void scheduleServiceArgs(IApplicationThread appThread, IBinder token, boolean taskRemoved,
			int startId, int flags, Intent args) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			try {
				appThread.scheduleServiceArgs(token, taskRemoved, startId, flags, args);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Method scheduleCreateService = IApplicationThread.class.getDeclaredMethod("scheduleServiceArgs",
						IBinder.class, int.class, int.class, Intent.class);
				scheduleCreateService.invoke(appThread, token, startId, flags, args);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public static void scheduleStopService(IApplicationThread appThread, IBinder token) {
		try {
			appThread.scheduleStopService(token);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void scheduleNewIntent(IApplicationThread appThread, List intents, IBinder token) {
		try {
			// noinspection unchecked
			appThread.scheduleNewIntent((List<ReferrerIntent>) intents, token);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
