package com.lody.virtual.service;

import android.content.ComponentName;
import android.os.IBinder;

import com.lody.virtual.helper.proto.AppTaskInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Lody
 */

public class ActivityTaskRecord {
	String rootAffinity;
	int taskId;
	final LinkedList<ActivityRecord> activityList = new LinkedList<ActivityRecord>();
	final Map<IBinder, ActivityRecord> activities = new HashMap<>();
	ComponentName baseActivity;

	public AppTaskInfo toTaskInfo() {
		ActivityRecord top = activityList.getLast();
		ComponentName topActivity = null;
		if (top != null) {
			topActivity = new ComponentName(top.activityInfo.packageName, top.activityInfo.name);
		}
		return new AppTaskInfo(taskId, baseActivity, topActivity);
	}
}
