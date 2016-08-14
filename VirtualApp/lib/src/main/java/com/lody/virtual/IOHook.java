package com.lody.virtual;

import android.os.Binder;
import android.os.Build;
import android.os.Process;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.proto.AppSettings;
import com.lody.virtual.helper.utils.VLog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * Created by Xfast on 2016/7/21.
 */
public class IOHook {

	private static final String TAG = IOHook.class.getSimpleName();

	private static Map<String, AppSettings> sDexOverrideMap;

	public static void startDexOverride() {
		List<AppSettings> appSettings = VirtualCore.getCore().getAllApps();
		sDexOverrideMap = new HashMap<>(appSettings.size());
		for (AppSettings info : appSettings) {
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
		try {
			String methodName =
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? "openDexFileNative" : "openDexFile";
			openDexFileNative = DexFile.class.getDeclaredMethod(methodName, String.class, String.class, Integer.TYPE);
			openDexFileNative.setAccessible(true);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void hookNative() {
		try {
			nativeHookNative(openDexFileNative, VirtualRuntime.isArt());
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
		int resultUid = originUid;
		int callingPid = Binder.getCallingPid();
		if (callingPid == 0) {
			resultUid = VClientImpl.getClient().getVUid();
		} else if (callingPid == VirtualCore.getCore().getSystemPid()) {
			return Process.SYSTEM_UID;
		} else {
			int uid = VActivityManager.getInstance().getUidByPid(callingPid);
			if (uid != -1) {
				resultUid = uid;
			} else {
				VLog.e(TAG, "Unable to get uid from pid : %d.", callingPid);
			}
		}
		VLog.e(TAG, "GetCallingUid return %d.", resultUid);
		return resultUid;
	}

	public static void onOpenDexFileNative(String[] params) {
		String dexOrJarPath = params[0];
		String outputPath = params[1];
		VLog.d(TAG, "DexOrJarPath = %s, OutputPath = %s.", dexOrJarPath, outputPath);
		AppSettings info = sDexOverrideMap.get(dexOrJarPath);
		if (info != null && !info.dependSystem) {
			outputPath = info.getOdexFile().getPath();
			params[1] = outputPath;
		}
	}



    private static native void nativeHookNative(Object method, boolean isArt);

	private static native void nativeMark();



	private static native String nativeRestoreRedirectedPath(String redirectedPath);

	private static native String nativeGetRedirectedPath(String orgPath);

	private static native void nativeRedirect(String orgPath, String newPath);

	private static native void nativeHook(int apiLevel);

	public static int onGetUid(int uid) {
		int vuid = VClientImpl.getClient().getVUid();
		if (vuid == -1) {
			if (VClientImpl.getClient().isBound()) {
				VLog.printStackTrace(TAG);
			}
			vuid = uid;
		}
		VLog.e(TAG, "Process.myPid() return %d.", vuid);
		return vuid;
	}
}
