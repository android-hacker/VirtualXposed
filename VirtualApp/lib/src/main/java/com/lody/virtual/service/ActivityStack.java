package com.lody.virtual.service;

import android.os.IBinder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

	List<ActivityTaskRecord> tasks = Collections.synchronizedList(new LinkedList<ActivityTaskRecord>());

	public ActivityTaskRecord findTask(IBinder activityToken) {
		for (ActivityTaskRecord task : tasks) {
			ActivityRecord r = task.activities.get(activityToken);
            if (r != null) {
                return task;
            }
		}
		return null;
	}

	public ActivityTaskRecord findTask(int taskId) {
        for (ActivityTaskRecord task : tasks) {
            if (task.taskId == taskId) {
                return task;
            }
        }
        return null;
    }

}
