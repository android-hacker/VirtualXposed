package com.lody.virtual.client.env;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.AppBindDataCompat;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.XLog;

/**
 * @author Lody
 *
 *         <p/>
 *         Runtime Environment for App.
 */
public class RuntimeEnv {

	/* package */ static Handler sUIHandler = null;

	private static String sCurrentProcessName;

	public static void init() {
		sUIHandler = new Handler(Looper.getMainLooper());
	}

	public static String getCurrentProcessName() {
		return sCurrentProcessName;
	}

	public static void setCurrentProcessName(String processName, AppInfo appInfo) {
		if (processName == null) {
			return;
		}
		sCurrentProcessName = processName;
		Process.setArgV0(processName);
		try {
			// NOTE: 部分App,例如:支付宝, 会反射获取DdmHandleAppName.mAppName来直接拿到进程名
			Reflect.on("android.ddm.DdmHandleAppName").set("mAppName", processName);
		} catch (Throwable e) {
			// Ignore
		}
		VirtualCore.getCore().notifyOnEnterAppProcessName(sCurrentProcessName);
		AppBindDataCompat dataMirror = new AppBindDataCompat(VirtualCore.getHostBindData());
		dataMirror.setAppInfo(appInfo.applicationInfo);
		dataMirror.setInfo(appInfo.getLoadedApk());
		dataMirror.setProcessName(processName);
	}

	/**
	 * @return UI-Handler
	 */
	public static Handler getUIHandler() {
		return sUIHandler;
	}

	public static <T> T crash(RemoteException e) throws RuntimeException {
		e.printStackTrace();
		// 服务端挂了, 客户端活着也没用了...
		 Process.killProcess(Process.myPid());
		throw new RuntimeException(e);
	}

	public static void exit() {
		XLog.d(RuntimeEnv.class.getSimpleName(), "Exit Process : " + VirtualCore.getCore().getProcessName());
		Process.killProcess(Process.myPid());
		System.exit(0);
	}
}
