package com.lody.virtual.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.IServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

/**
 * @author Lody
 *
 */
public class ProcessList {

	private final Map<Integer, ProcessRecord> runningRecords = new ConcurrentHashMap<Integer, ProcessRecord>();

	private final Map<String, String> stubProcesses = new ConcurrentHashMap<String, String>();

	public String getAppProcName(String stubProcName) {
		return stubProcesses.get(stubProcName);
	}

	public Map<Integer, ProcessRecord> getRunningRecords() {
		return Collections.unmodifiableMap(runningRecords);
	}

	public ProcessRecord getRecord(int pid) {
		return runningRecords.get(pid);
	}

	public void removeRecord(int pid) {
		runningRecords.remove(pid);
	}

	public Collection<ProcessRecord> values() {
		return runningRecords.values();
	}

	public boolean containPid(int pid) throws RemoteException {
		return runningRecords.containsKey(pid);
	}

	/**
	 * 判断指定进程名的进程是否正在运行
	 *
	 * @param stubProcessName
	 *            Stub进程名
	 */
	public boolean isProcessRunning(String stubProcessName) {
		for (ProcessRecord r : runningRecords.values()) {
			if (TextUtils.equals(stubProcessName, r.stubProcessName)) {
				return true;
			}
		}
		return false;
	}

	public void map(String stubProcessName, String pluginProcessName) {
		stubProcesses.put(stubProcessName, pluginProcessName);
	}

	public void addRecord(int callingPid, ProcessRecord r) {
		runningRecords.put(callingPid, r);
	}

	public ServiceRecord queryServiceRecord(IBinder token) {
		for (ProcessRecord processRecord : runningRecords.values()) {
			ServiceRecord record = processRecord.findServiceRecord(token);
			if (record != null) {
				return record;
			}
		}
		return null;
	}

	public ServiceRecord queryServiceRecord(IServiceConnection connection) {
		for (ProcessRecord processRecord : runningRecords.values()) {
			ServiceRecord serviceRecord = processRecord.findServiceRecord(connection);
			if (serviceRecord != null) {
				return serviceRecord;
			}
		}
		return null;
	}
}
