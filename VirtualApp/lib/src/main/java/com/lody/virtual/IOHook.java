package com.lody.virtual;

import android.os.Binder;
import android.os.Build;
import android.os.Process;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * VirtualApp Native Project
 */
public class IOHook {

	private static final String TAG = IOHook.class.getSimpleName();

	private static Map<String, AppSetting> sDexOverrideMap;

	public static void startDexOverride() {
		List<AppSetting> appSettings = VirtualCore.get().getAllApps();
		sDexOverrideMap = new HashMap<>(appSettings.size());
		for (AppSetting info : appSettings) {
			try {
				sDexOverrideMap.put(new File(info.apkPath).getCanonicalPath(), info);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static {
		try {
			System.loadLibrary("iohook");
		} catch (Throwable e) {
			VLog.e(TAG, VLog.getStackTraceString(e));
		}
	}

	public static String getRedirectedPath(String orgPath) {
		try {
			return nativeGetRedirectedPath(orgPath);
		} catch (Throwable e) {
			VLog.e(TAG, VLog.getStackTraceString(e));
		}
		return null;
	}

	public static String restoreRedirectedPath(String orgPath) {
		try {
			return nativeRestoreRedirectedPath(orgPath);
		} catch (Throwable e) {
			VLog.e(TAG, VLog.getStackTraceString(e));
		}
		return null;
	}

	public static void redirect(String orgPath, String newPath) {
		try {
			nativeRedirect(orgPath, newPath);
		} catch (Throwable e) {
			VLog.e(TAG, VLog.getStackTraceString(e));
		}
	}

	public static void hook() {
		try {
			nativeHook(Build.VERSION.SDK_INT);
		} catch (Throwable e) {
			VLog.e(TAG, VLog.getStackTraceString(e));
		}
	}

	private static Method openDexFileNative;
	static {
		String methodName =
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? "openDexFileNative" : "openDexFile";
		for (Method method : DexFile.class.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				openDexFileNative = method;
				break;
			}
		}
		if (openDexFileNative == null) {
			throw new RuntimeException("Unable to find method : " + methodName);
		}
		openDexFileNative.setAccessible(true);
	}

	public static void hookNative() {
		try {
			nativeHookNative(openDexFileNative, VirtualRuntime.isArt(), Build.VERSION.SDK_INT);
		} catch (Throwable e) {
			VLog.e(TAG, VLog.getStackTraceString(e));
		}
	}

	public static void onKillProcess(int pid, int signal) {
		VLog.e(TAG, "killProcess: pid = %d, signal = %d.", pid, signal);
		if (pid == android.os.Process.myPid()) {
			VLog.e(TAG, VLog.getStackTraceString(new Throwable()));
		}
	}

	public static int onGetCallingUid(int originUid) {
		int callingPid = Binder.getCallingPid();
		if (callingPid == Process.myPid()) {
			return VClientImpl.getClient().getBaseVUid();
		}
		if (callingPid == VirtualCore.get().getSystemPid()) {
			return Process.SYSTEM_UID;
		}
		int vuid = VActivityManager.get().getUidByPid(callingPid);
		if (vuid != -1) {
            return VUserHandle.getAppId(vuid);
        }
		VLog.d(TAG, "Ops, who are you ? " + callingPid);
		return VClientImpl.getClient().getBaseVUid();
	}

	public static void onOpenDexFileNative(String[] params) {
		String dexOrJarPath = params[0];
		String outputPath = params[1];
		VLog.d(TAG, "DexOrJarPath = %s, OutputPath = %s.", dexOrJarPath, outputPath);
		try {
			String canonical = new File(dexOrJarPath).getCanonicalPath();
			AppSetting info = sDexOverrideMap.get(canonical);
			if (info != null && !info.dependSystem) {
				outputPath = info.getOdexFile().getPath();
				params[1] = outputPath;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



    private static native void nativeHookNative(Object method, boolean isArt, int apiLevel);

	private static native void nativeMark();


	private static native String nativeRestoreRedirectedPath(String redirectedPath);

	private static native String nativeGetRedirectedPath(String orgPath);

	private static native void nativeRedirect(String orgPath, String newPath);

	private static native void nativeHook(int apiLevel);

	public static int onGetUid(int uid) {
		return VClientImpl.getClient().getBaseVUid();
	}
}
