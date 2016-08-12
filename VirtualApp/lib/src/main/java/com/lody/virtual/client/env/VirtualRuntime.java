package com.lody.virtual.client.env;

import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.VMRuntimeCompat;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.ReflectException;
import com.lody.virtual.helper.utils.VLog;

/**
 * @author Lody
 *
 *         <p/>
 *         Runtime Environment for App.
 */
public class VirtualRuntime {

	private static final Handler sUIHandler = new Handler(Looper.getMainLooper());

	private static String sInitialPackageName;
	private static String sProcessName;

	public static Handler getUIHandler() {
		return sUIHandler;
	}

	public static String getProcessName() {
		return sProcessName;
	}

	public static String getInitialPackageName() {
		return sInitialPackageName;
	}

	public static void setupRuntime(String processName, ApplicationInfo appInfo) {
		if (sInitialPackageName == null) {
			sInitialPackageName = appInfo.packageName;
		}
		sProcessName = processName;
		Process.setArgV0(processName);
		try {
			Reflect.on("android.ddm.DdmHandleAppName").set("mAppName", processName);
		} catch (ReflectException e) {
			e.printStackTrace();
		}
		VMRuntimeCompat.registerAppInfo(appInfo.packageName, appInfo.dataDir, processName);
	}

	public static <T> T crash(RemoteException e) throws RuntimeException {
		e.printStackTrace();
		CrashReporter.report(getProcessName(), e);
		exit();
		throw new RuntimeException(e);
	}

	public static void exit() {
		VLog.d(VirtualRuntime.class.getSimpleName(), "Exit process : %s (%s).", getProcessName(),
				VirtualCore.getCore().getProcessName());
		Process.killProcess(Process.myPid());
	}

	public static boolean isArt() {
		return System.getProperty("java.vm.version").startsWith("2");
	}
}
