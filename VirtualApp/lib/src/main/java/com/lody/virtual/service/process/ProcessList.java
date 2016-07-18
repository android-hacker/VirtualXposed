package com.lody.virtual.service.process;

import android.os.RemoteException;
import android.text.TextUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 *
 */
public class ProcessList {

	private final Map<Integer, ProcessRecord> runningRecords = new ConcurrentHashMap<Integer, ProcessRecord>();

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
		synchronized (runningRecords) {
			for (ProcessRecord r : runningRecords.values()) {
				if (TextUtils.equals(stubProcessName, r.stubProcessName)) {
					return true;
				}
			}
		}
		return false;
	}


	public synchronized void addRecord(int callingPid, ProcessRecord r) {
		runningRecords.put(callingPid, r);
	}

}
