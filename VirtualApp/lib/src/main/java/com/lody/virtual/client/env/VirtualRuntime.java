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
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//			try {
//				Field EXTERNAL_STORAGE_DIRECTORY = Environment.class.getDeclaredField("EXTERNAL_STORAGE_DIRECTORY");
//				EXTERNAL_STORAGE_DIRECTORY.setAccessible(true);
//				File file = (File) EXTERNAL_STORAGE_DIRECTORY.get(null);
//				File newFile = VEnvironment.redirectSDCard(VUserHandle.myUserId(), file.getPath());
//				EXTERNAL_STORAGE_DIRECTORY.set(null, newFile);
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
//		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
//			try {
//				Reflect sCurrentUser = Reflect.on(Environment.class).field("sCurrentUser");
//				File file = sCurrentUser.get("mExternalStorage");
//				File newFile = VEnvironment.redirectSDCard(VUserHandle.myUserId(), file.getPath());
//				sCurrentUser.set("mExternalStorage", newFile);
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
//		}
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
