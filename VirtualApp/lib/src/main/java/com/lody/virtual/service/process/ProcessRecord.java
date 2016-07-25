package com.lody.virtual.service.process;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IApplicationThread;
import android.os.RemoteException;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.service.am.StubInfo;
import com.lody.virtual.service.am.VActivityService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ProcessRecord {
	/**
	 * Real process name
	 */
	public String stubProcessName;
	/**
	 * Virtual process name
	 */
	public String appProcessName;
	/**
	 * Target Stub info
	 */
	public StubInfo stubInfo;
	/**
	 * ApplicationThread of target process
	 */
	public IApplicationThread appThread;
	/**
	 * target process's Client object
	 */
	IVClient client;
	/**
	 * target process's pid
	 */
	int pid;
	/**
	 * target process's uid
	 */
	int uid;
	/**
	 * Running applications on target process
	 */
	final Set<String> runningAppPkgs = new HashSet<String>();

	/*package*/ synchronized void updateStubProcess(int pid) {
		try {
			List<ActivityManager.RunningAppProcessInfo> runningInfos = ActivityManagerNative.getDefault()
					.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo info : runningInfos) {
				if (info.pid == pid) {
					this.stubProcessName = info.processName;
					break;
				}
			}
		} catch (RemoteException e) {
			// Ignore
		}
		stubInfo = VActivityService.getService().findStubInfo(this.stubProcessName);
	}


	/**
	 * 添加一个APK到该进程
	 *
	 * @param pkgName
	 *            apk的包名
	 */
	/*package*/ synchronized boolean addPkg(String pkgName) {
		return !runningAppPkgs.contains(pkgName) && runningAppPkgs.add(pkgName);

	}

	/**
	 * 该进程是否正在运行指定包名的插件
	 *
	 * @param pkgName
	 *            插件包名
	 */
	public boolean isRunning(String pkgName) {
		return runningAppPkgs.contains(pkgName);
	}
}
