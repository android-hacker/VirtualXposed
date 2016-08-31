package com.lody.virtual.service.am;

import android.content.ComponentName;
import android.os.IBinder;

/**
 * @author Lody
 */

/* package */ class ActivityRecord {
	public TaskRecord task;
	public ComponentName component;
	public IBinder token;
	public int userId;
	public ProcessRecord process;
	public int launchMode;
	public int flags;
	public boolean marked;
	public String affinity;

	public ActivityRecord(TaskRecord task, ComponentName component, IBinder token, int userId, ProcessRecord process, int launchMode, int flags, String affinity) {
		this.task = task;
		this.component = component;
		this.token = token;
		this.userId = userId;
		this.process = process;
		this.launchMode = launchMode;
		this.flags = flags;
		this.affinity = affinity;
	}
}
