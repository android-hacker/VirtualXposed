package com.lody.virtual.service.am;

import android.os.IBinder;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Lody
 *
 *         实验发现: 1、SingleInstance 永远独占一个Task。
 *
 *         2、添加 Intent.FLAG_ACTIVITY_MULTIPLE_TASK 标志后, 启动的Activity永远在新创建的Task中。
 *
 *
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
