package com.lody.virtual.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 *
 */
public class RunningAppList {

	private final Map<String, RunningAppRecord> runningApps = new ConcurrentHashMap<String, RunningAppRecord>();

	public void pluginStopped(String pkgName) {
		runningApps.remove(pkgName);
	}

	public void pluginStarted(String pkgName, RunningAppRecord runningAppRecord) {
		runningApps.put(pkgName, runningAppRecord);
	}

	public RunningAppRecord getRecord(String pkg) {
		return runningApps.get(pkg);
	}

}
