package com.lody.virtual;

import android.os.Binder;
import android.os.Build;
import android.os.Process;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;

/**
 * Created by Xfast on 2016/7/21.
 */
public class IOHook {

	private static final String TAG = IOHook.class.getSimpleName();

	private static boolean sLoaded;

	static {
		try {
			System.loadLibrary("iohook");
			sLoaded = true;
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

	public static void hookNative() {
		try {
			nativeHookNative();
		} catch (Throwable e) {
			VLog.e(TAG, VLog.getStackTraceString(e));
		}
	}

	/**
	 * this is called by JNI.
	 * 
	 * @param pid
	 *            killed pid
	 * @param signal
	 *            signal
	 */
	public static void onKillProcess(int pid, int signal) {
		VLog.e(TAG, "killProcess: pid = %d, signal = %d.", pid, signal);
		if (pid == android.os.Process.myPid()) {
			VLog.e(TAG, VLog.getStackTraceString(new Throwable()));
		}
	}

	public static int onGetCallingUid(int originUid) {
		int resultUid = originUid;
		int callingPid = Binder.getCallingPid();
		if (callingPid == Process.myPid()) {
			resultUid = originUid;
		} else {
			if (VClientImpl.getClient().isBound()) {
				String initialPackage = VActivityManager.getInstance().getInitialPackage(callingPid);
				if (!VClientImpl.getClient().geCurrentPackage().equals(initialPackage)
						&& !ComponentUtils.isSharedPackage(initialPackage)) {
//					resultUid = 99999;
				}
			}
		}
		VLog.d(TAG, "getCallingUid: orig = %d, after = %d.", originUid, resultUid);
		return resultUid;
	}

	// private static native void nativeRejectPath(String path);

	private static native String nativeRestoreRedirectedPath(String redirectedPath);

	private static native String nativeGetRedirectedPath(String orgPath);

	private static native void nativeRedirect(String orgPath, String newPath);

	private static native void nativeHookNative();

	private static native void nativeHook(int apiLevel);
}
