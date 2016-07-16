package com.lody.virtual.service.process;

import java.util.ArrayList;
import java.util.List;

final class RunningAppRecord {
	/**
	 * 插件包名
	 */
	String pkgName;
	/**
	 * 该插件运行在哪些进程Pid中
	 */
	List<Integer> runningPids = new ArrayList<Integer>();

	public RunningAppRecord(String pkgName) {
		this.pkgName = pkgName;
	}

	/**
	 * 添加一个运行该插件的Pid
	 *
	 * @param pid
	 *            Pid
	 */
	public void addPid(int pid) {
		runningPids.add(pid);
	}

	/**
	 * 移除一个运行该插件的Pid
	 *
	 * @param pid
	 *            Pid
	 */
	public void removePid(int pid) {
		int index = -1;
		for (int N = 0; N < runningPids.size(); N++) {
			if (runningPids.get(N) == pid) {
				index = N;
			}
		}
		if (index > 0) {
			runningPids.remove(index);
		}
	}

	/**
	 * 该插件是否运行在指定Pid
	 *
	 * @param pid
	 *            Pid
	 */
	public boolean isRunningOnPid(int pid) {
		return runningPids.contains(pid);
	}

}