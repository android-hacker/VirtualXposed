package com.lody.virtual.service.am;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

public class ProcessMap {

	final Map<String, Integer> mProcessByNames = new HashMap<>(10);
	final SparseArray<ProcessRecord> mProcessByPids = new SparseArray<>(10);

	public ProcessRecord get(int pid) {
		return mProcessByPids.get(pid);
	}

	public ProcessRecord get(String processName) {
		Integer pid = mProcessByNames.get(processName);
		if (pid != null) {
			return get(pid);
		}
		return null;
	}

	public ProcessRecord get(StubInfo stubInfo) {
		for (int N = 0; N < mProcessByPids.size(); N++) {
			ProcessRecord r = mProcessByPids.valueAt(N);
			if (r.stubInfo.equals(stubInfo)) {
				return r;
			}
		}
		return null;
	}

	public void put(ProcessRecord record) {
		int pid = record.pid;
		String processName = record.processName;
		mProcessByNames.put(processName, pid);
		mProcessByPids.put(pid, record);
	}

	public void foreach(Visitor visitor) {
		for (int N = 0; N < mProcessByPids.size(); N++) {
			ProcessRecord r = mProcessByPids.valueAt(N);
			if (!visitor.accept(r)) {
				break;
			}
		}
	}

	public ProcessRecord remove(int pid) {
		ProcessRecord record = mProcessByPids.get(pid);
		if (record != null) {
			mProcessByNames.remove(record.processName);
			mProcessByPids.remove(pid);
		}
		return record;
	}

	public interface Visitor {
		/**
		 *
		 * @return should break foreach?
		 */
		boolean accept(ProcessRecord record);
	}

}
