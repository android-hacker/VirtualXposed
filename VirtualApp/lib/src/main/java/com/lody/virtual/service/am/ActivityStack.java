package com.lody.virtual.service.am;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.SparseArray;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.ComponentUtils;

import java.util.Iterator;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TASK;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TOP;

/**
 * @author Lody
 */

/* package */ class ActivityStack {

	final ActivityManager mAM;
	private final VActivityManagerService mService;

	/**
	 * [Key] = TaskId
	 * [Value] = TaskRecord
	 */
	private final SparseArray<TaskRecord> mHistory = new SparseArray<>();

	private Handler mTaskHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int N = mHistory.size();
			while (N-- > 0) {
				TaskRecord task = mHistory.valueAt(N);
				for (ActivityRecord r : task.activities) {
					if (r.marked) {
						try {
							r.process.client.finishActivity(r.token);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	};

	public ActivityStack(VActivityManagerService mService) {
		this.mService = mService;
		mAM = (ActivityManager) VirtualCore.get().getContext().getSystemService(Context.ACTIVITY_SERVICE);
	}


	private static void removeFlags(Intent intent, int flags) {
		intent.setFlags(intent.getFlags() & ~flags);
	}

	private static boolean containFlags(Intent intent, int flags) {
		return (intent.getFlags() & flags) != 0;
	}

	private static ActivityRecord topActivityInTask(TaskRecord task) {
		for (int size = task.activities.size() - 1; size >= 0; size--) {
			ActivityRecord r = task.activities.get(size);
			if (!r.marked) {
				return r;
			}
		}
		return null;
	}

	private void deliverNewIntentLocked(ActivityRecord sourceRecord, ActivityRecord targetRecord, Intent intent) {
		String creator = sourceRecord != null ? sourceRecord.component.getPackageName() : "android";
		try {
			targetRecord.process.client.scheduleNewIntent(creator, targetRecord.token, intent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	private TaskRecord findTaskByAffinityLocked(int userId, String affinity) {
		for (int i = 0; i < this.mHistory.size(); i++) {
			TaskRecord r = this.mHistory.valueAt(i);
			if (userId == r.userId && affinity.equals(r.affinity)) {
				return r;
			}
		}
		return null;
	}

	private TaskRecord findTaskByIntentLocked(int userId, Intent intent) {
		for (int i = 0; i < this.mHistory.size(); i++) {
			TaskRecord r = this.mHistory.valueAt(i);
			if (userId == r.userId && r.taskRoot != null && intent.getComponent().equals(r.taskRoot.getComponent())) {
				return r;
			}
		}
		return null;
	}

	private ActivityRecord findActivityByToken(int userId, IBinder token) {
		ActivityRecord target = null;
		if (token != null) {
			for (int i = 0; i < this.mHistory.size(); i++) {
				TaskRecord task = this.mHistory.valueAt(i);
				if (task.userId != userId) {
					continue;
				}
				for (ActivityRecord r : task.activities) {
					if (r.token == token) {
						target = r;
					}
				}
			}
		}
		return target;
	}

	private boolean markTaskByClearTarget(TaskRecord task, ClearTarget clearTarget, ComponentName component) {
		boolean marked = false;
		switch (clearTarget) {
			case TASK: {
				for (ActivityRecord r : task.activities) {
					r.marked = true;
					marked = true;
				}
			} break;
			case ACTIVITY: {
				for (ActivityRecord r : task.activities) {
					if (r.component.equals(component)) {
						r.marked = true;
						marked = true;
					}
				}
			} break;
			case TOP: {
				int N = task.activities.size();
				while (N-- > 0) {
					ActivityRecord r = task.activities.get(N);
					if (r.component.equals(component)) {
						marked = true;
						break;
					}
				}
				if (marked) {
					while (N++ < task.activities.size() - 1) {
						task.activities.get(N).marked = true;
					}
				}

			} break;
		}
		return marked;
	}

	@SuppressWarnings("deprecation")
	public int startActivityLocked(int userId, Intent intent, ActivityInfo info, IBinder resultTo, Bundle options,
			int requestCode) {
		Intent destIntent;
		ActivityRecord sourceRecord = findActivityByToken(userId, resultTo);
		TaskRecord sourceTask = sourceRecord != null ? sourceRecord.task : null;

		ReuseTarget reuseTarget = ReuseTarget.CURRENT;
		ClearTarget clearTarget = ClearTarget.NOTHING;

		if (intent.getComponent() == null) {
			intent.setComponent(new ComponentName(info.packageName, info.name));
		}
		if (sourceRecord != null && sourceRecord.launchMode == LAUNCH_SINGLE_INSTANCE) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		if (containFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TOP)) {
			removeFlags(intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			clearTarget = ClearTarget.TOP;
		}
		if (containFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TASK)) {
			if (containFlags(intent, Intent.FLAG_ACTIVITY_NEW_TASK)) {
				clearTarget = ClearTarget.TASK;
			} else {
				removeFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			switch (info.documentLaunchMode) {
				case ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING :
					clearTarget = ClearTarget.TASK;
					reuseTarget = ReuseTarget.DOCUMENT;
					break;
				case ActivityInfo.DOCUMENT_LAUNCH_ALWAYS :
					reuseTarget = ReuseTarget.MULTIPLE;
					break;
			}
		}

		switch (info.launchMode) {
			case LAUNCH_SINGLE_TOP : {
				clearTarget = ClearTarget.TOP;
				if (containFlags(intent, Intent.FLAG_ACTIVITY_NEW_TASK)) {
					reuseTarget = containFlags(intent, Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
							? ReuseTarget.MULTIPLE
							: ReuseTarget.AFFINITY;
				}
			} break;
			case LAUNCH_SINGLE_TASK : {
				clearTarget = ClearTarget.TOP;
				reuseTarget = containFlags(intent, Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
						? ReuseTarget.MULTIPLE
						: ReuseTarget.AFFINITY;
			} break;
			case LAUNCH_SINGLE_INSTANCE : {
				clearTarget = ClearTarget.TOP;
				reuseTarget = ReuseTarget.AFFINITY;
			} break;
			default : {
				if (containFlags(intent, Intent.FLAG_ACTIVITY_SINGLE_TOP)) {
					clearTarget = ClearTarget.TOP;
				}
			} break;
		}
		if (clearTarget == ClearTarget.NOTHING) {
			if (containFlags(intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)) {
				clearTarget = ClearTarget.ACTIVITY;
			}
		}
		if (sourceTask == null && reuseTarget == ReuseTarget.CURRENT) {
			reuseTarget = ReuseTarget.AFFINITY;
		}
		String affinity = ComponentUtils.getTaskAffinity(info);
		TaskRecord reuseTask = null;
		switch (reuseTarget) {
			case AFFINITY :
				reuseTask = findTaskByAffinityLocked(userId, affinity);
				break;
			case DOCUMENT :
				reuseTask = findTaskByIntentLocked(userId, intent);
				break;
			case CURRENT :
				reuseTask = sourceTask;
				break;
			default :
				break;
		}

		boolean taskMarked = false;
		if (reuseTask == null) {
			destIntent = startActivityProcess(userId, null, intent, info
			);
			if (destIntent != null) {
				destIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				destIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				destIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					destIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				else
					destIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					VirtualCore.get().getContext().startActivity(destIntent, options);
				} else {
					VirtualCore.get().getContext().startActivity(destIntent);
				}
			}
		} else if (clearTarget != ClearTarget.TOP && ComponentUtils.isSameIntent(intent, reuseTask.taskRoot)) {
			mAM.moveTaskToFront(reuseTask.taskId, 0);
			// In this case, we only need to move the task to front.

		} else {
			boolean delivered = false;
			mAM.moveTaskToFront(reuseTask.taskId, 0);
			if (clearTarget == ClearTarget.TOP) {
                taskMarked = markTaskByClearTarget(reuseTask, clearTarget, intent.getComponent());
                ActivityRecord topRecord = topActivityInTask(reuseTask);
				// Target activity is on top
                if (topRecord != null && topRecord.component.equals(intent.getComponent())) {
                    deliverNewIntentLocked(sourceRecord, topRecord, intent);
                    delivered = true;
                }
            }
			if (!delivered) {
                destIntent = startActivityProcess(userId, sourceRecord, intent, info
				);
                if (destIntent != null) {
                    startActivityFromSourceTask(reuseTask, destIntent, info, options);
                }
            }
		}
		if (taskMarked) {
			scheduleFinishMarkedActivity();
		}

		return 0;
	}

	private void scheduleFinishMarkedActivity() {
		mTaskHandler.removeMessages(0);
		mTaskHandler.sendEmptyMessage(0);
	}

	private boolean startActivityFromSourceTask(TaskRecord task, Intent intent, ActivityInfo info, Bundle options) {
		ActivityRecord top = topActivityInTask(task);
		if (top != null) {
			if (startActivityProcess(task.userId, top, intent, info) != null) {
				try {
					return top.process.client.startActivityFromToken(top.token, intent, options);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private Intent startActivityProcess(int userId, ActivityRecord sourceRecord, Intent intent, ActivityInfo info) {
		ProcessRecord targetApp = mService.startProcessIfNeedLocked(info.processName, userId, info.packageName);
		if (targetApp == null) {
			return null;
		}
		ActivityInfo stubActivityInfo = targetApp.stubInfo.fetchStubActivityInfo(info);
		Intent targetIntent = new Intent();
		targetIntent.setClassName(stubActivityInfo.packageName, stubActivityInfo.name);
		ComponentName component = intent.getComponent();
		if (component == null) {
			component = ComponentUtils.toComponentName(info);
		}
		targetIntent.setType(component.flattenToString());
		targetIntent.putExtra("intent", new Intent(intent));
		targetIntent.putExtra("info", info);
		if (sourceRecord != null) {
			targetIntent.putExtra("caller_package", sourceRecord.component.getPackageName());
		}
		targetIntent.putExtra("user_id", userId);
		return targetIntent;
	}


	public void onActivityCreated(ProcessRecord targetApp, ComponentName component, IBinder token, Intent taskRoot, String affinity, int taskId, int launchMode, int flags, int clearTargetOrder) {
		synchronized (mHistory) {
			TaskRecord task = mHistory.get(taskId);
			if (task == null) {
				task = new TaskRecord(taskId, targetApp.userId, affinity, taskRoot);
				mHistory.put(taskId, task);
			}
			ActivityRecord record = new ActivityRecord(task, component, token, targetApp.userId, targetApp, launchMode, flags, affinity);
			task.activities.add(record);
		}
	}

	public void onActivityResumed(int userId, IBinder token) {
		synchronized (mHistory) {
			ActivityRecord r = findActivityByToken(userId, token);
			if (r != null) {
				r.task.activities.remove(r);
				r.task.activities.add(r);
			}
		}
	}

	public boolean onActivityDestroyed(int userId, IBinder token) {
		synchronized (mHistory) {
			ActivityRecord r = findActivityByToken(userId, token);
			if (r != null) {
				r.task.activities.remove(r);
				if (r.task.activities.isEmpty()) {
					mHistory.remove(r.task.taskId);
					return true;
				}
			}
			return false;
		}
	}

	public void processDied(ProcessRecord record) {
		synchronized (mHistory) {
			int N = mHistory.size();
			while (N-- > 0) {
				TaskRecord task = mHistory.valueAt(N);
				Iterator<ActivityRecord> iterator = task.activities.iterator();
				while (iterator.hasNext()) {
					ActivityRecord r = iterator.next();
					if (r.process.pid == record.pid) {
						iterator.remove();
						if (task.activities.isEmpty()) {
							mHistory.remove(task.taskId);
						}
					}
				}
			}

		}
	}

	enum ClearTarget {
		NOTHING, TASK, ACTIVITY, TOP
	}

	enum ReuseTarget {
		CURRENT, AFFINITY, DOCUMENT, MULTIPLE
	}
}
