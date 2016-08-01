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

	private final Map<Integer, ProcessRecord> runningProcesses = new ConcurrentHashMap<Integer, ProcessRecord>();

	public ProcessRecord findProcess(int pid) {
		return runningProcesses.get(pid);
	}

	public void removeRecord(int pid) {
		runningProcesses.remove(pid);
	}

	public Collection<ProcessRecord> values() {
		return runningProcesses.values();
	}

	public boolean containPid(int pid) throws RemoteException {
		return runningProcesses.containsKey(pid);
	}

	/**
	 * Is the target stub process is running?
	 *
	 * @param stubProcessName
	 *            Stub process name
	 */
	public boolean isProcessRunning(String stubProcessName) {
		synchronized (runningProcesses) {
			for (ProcessRecord r : runningProcesses.values()) {
				if (TextUtils.equals(stubProcessName, r.stubProcessName)) {
					return true;
				}
			}
		}
		return false;
	}


	public void addProcess(int callingPid, ProcessRecord r) {
		runningProcesses.put(callingPid, r);
	}

}
