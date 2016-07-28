package com.lody.virtual.service.am;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.utils.ComponentUtils;

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

}
