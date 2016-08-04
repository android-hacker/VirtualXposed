package com.lody.virtual.service.am;

import android.os.IBinder;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Lody
 */

/*package*/ class ActivityStack {

	final LinkedList<ActivityTaskRecord> tasks = new LinkedList<ActivityTaskRecord>();

    public ActivityTaskRecord findTask(String affinity) {
        synchronized (tasks) {
            for (ActivityTaskRecord task : tasks) {
                if (affinity.equals(task.rootAffinity)) {
                    return task;
                }
            }
        }
        return null;
    }
	public ActivityTaskRecord findTask(IBinder activityToken) {
		synchronized (tasks) {
            for (ActivityTaskRecord task : tasks) {
                ActivityRecord r = task.activities.get(activityToken);
                if (r != null) {
                    return task;
                }
            }
        }
		return null;
	}

    public ActivityRecord findRecord(IBinder activityToken) {
        synchronized (tasks) {
            for (ActivityTaskRecord task : tasks) {
                ActivityRecord r = task.activities.get(activityToken);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

	public ActivityTaskRecord findTask(int taskId) {
        synchronized (tasks) {
            for (ActivityTaskRecord task : tasks) {
                if (task.taskId == taskId) {
                    return task;
                }
            }
        }
        return null;
    }

    public synchronized void trimTasks() {
        ListIterator<ActivityTaskRecord> iterator = tasks.listIterator();
        while (iterator.hasNext()) {
            ActivityTaskRecord task = iterator.next();
            if (task.activities.isEmpty()) {
                iterator.remove();
            }
        }
    }
}
