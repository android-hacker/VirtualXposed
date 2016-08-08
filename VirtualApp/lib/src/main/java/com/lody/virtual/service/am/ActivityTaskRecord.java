package com.lody.virtual.service.am;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.utils.ComponentUtils;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

/**
 * @author Lody
 */

public class ActivityTaskRecord {
	final LinkedList<ActivityRecord> activityList = new LinkedList<ActivityRecord>();
	final Map<IBinder, ActivityRecord> activities = new HashMap<>();
	String rootAffinity;
	int taskId;
	ComponentName baseActivity;

	public AppTaskInfo toTaskInfo() {
		ActivityRecord top = activityList.isEmpty() ? null : activityList.getLast();
		ComponentName topActivity = null;
		if (top != null) {
			topActivity = new ComponentName(top.activityInfo.packageName, top.activityInfo.name);
		}
		return new AppTaskInfo(taskId, baseActivity, topActivity);
	}

	public ActivityRecord topActivity() {
		return activityList.getLast();
	}

	public IBinder topActivityToken() {
		ActivityRecord r = topActivity();
		if (r != null) {
			return r.token;
		}
		return null;
	}

	public boolean isOnTop(ActivityInfo activityInfo) {
		ActivityRecord top = topActivity();
		return top != null && ComponentUtils.isSameComponent(activityInfo, top.activityInfo);
	}

	public boolean isInTask(ActivityInfo activityInfo) {
		for (ActivityRecord r : activityList) {
			if (ComponentUtils.isSameComponent(r.activityInfo, activityInfo)) {
				return true;
			}
		}
		return false;
	}

	public int size() {
		return activityList.size();
	}
}
