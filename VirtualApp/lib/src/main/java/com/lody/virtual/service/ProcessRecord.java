package com.lody.virtual.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lody.virtual.client.IVClient;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;

final class ProcessRecord {
	/**
	 * 对应的进程名
	 */
	public String stubProcessName;
	/**
	 * 该进程正在运行的插件的进程名
	 */
	public String appProcessName;
	/**
	 * 该插件对应的Stub
	 */
	public VActivityServiceImpl.StubInfo stubInfo;
	/**
	 * 该插件的appThread
	 */
	public IApplicationThread appThread;
	/**
	 * Client对象
	 */
	IVClient client;
	/**
	 * 进程PID
	 */
	int pid;
	/**
	 * 进程UID
	 */
	int uid;
	/**
	 * 运行在该进程的所有APK的包名
	 */
	Set<String> runningAppPkgs = new HashSet<String>();

	Map<String, ServiceRecord> servicesMap = new HashMap<String, ServiceRecord>();

	public synchronized void updateStubProc(int pid) {
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
		stubInfo = VActivityServiceImpl.getService().findStubInfo(this.stubProcessName);
	}

	public ServiceRecord findServiceRecord(String name) {
		return servicesMap.get(name);
	}

	public ServiceRecord findServiceRecord(IBinder token) {
		for (ServiceRecord r : servicesMap.values()) {
			if (r.token == token) {
				return r;
			}
		}
		return null;
	}

	/**
	 * 添加一个APK到该进程
	 *
	 * @param pkgName
	 *            apk的包名
	 */
	public synchronized boolean addPkg(String pkgName) {
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

	public void addServiceRecord(ServiceRecord record) {
		servicesMap.put(record.serviceInfo.name, record);
	}

	public void removeServiceRecord(ServiceInfo serviceInfo) {
		servicesMap.remove(serviceInfo.name);
	}

	public ServiceRecord findServiceRecord(IServiceConnection connection) {
		for (ServiceRecord r : servicesMap.values()) {
			for (ServiceRecord.ServiceBoundRecord boundRecord : r.mBoundRecords) {
				if (boundRecord.containsConnection(connection)) {
					return r;
				}
			}
		}
		return null;
	}
}