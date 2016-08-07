package com.lody.virtual.service.am;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IActivityManager;
import com.lody.virtual.service.process.ProcessRecord;
import com.lody.virtual.service.process.VProcessService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 *
 */
public class VActivityService extends IActivityManager.Stub {

	private static final AtomicReference<VActivityService> sService = new AtomicReference<>();
	private static final String TAG = VActivityService.class.getSimpleName();
	private final List<ActivityInfo> stubActivityList = new ArrayList<ActivityInfo>();

	private final Map<String, StubInfo> stubInfoMap = new ConcurrentHashMap<>();
	private final Set<String> stubProcessList = new HashSet<String>();
	private final ActivityStack mMainStack = new ActivityStack();
	private ActivityManager am = (ActivityManager) VirtualCore.getCore().getContext()
			.getSystemService(Context.ACTIVITY_SERVICE);

	public static VActivityService getService() {
		return sService.get();
	}

	public static void systemReady(Context context) {
		new VActivityService().onCreate(context);
	}

	public void onCreate(Context context) {
		AttributeCache.init(context);
		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		if (packageInfo == null) {
			throw new RuntimeException("Unable to found PackageInfo : " + context.getPackageName());
		}

		ActivityInfo[] activityInfos = packageInfo.activities;
		for (ActivityInfo activityInfo : activityInfos) {
			if (isStubComponent(activityInfo)) {
				stubActivityList.add(activityInfo);
				String processName = activityInfo.processName;
				stubProcessList.add(processName);
				StubInfo stubInfo = stubInfoMap.get(processName);
				if (stubInfo == null) {
					stubInfo = new StubInfo();
					stubInfo.processName = processName;
					stubInfoMap.put(processName, stubInfo);
				}
				String name = activityInfo.name;
				if (name.endsWith("_")) {
					stubInfo.dialogActivityInfos.add(activityInfo);
				} else {
					stubInfo.standardActivityInfos.add(activityInfo);
				}
			}
		}
		ProviderInfo[] providerInfos = packageInfo.providers;
		for (ProviderInfo providerInfo : providerInfos) {
			if (isStubComponent(providerInfo)) {
				String processName = providerInfo.processName;
				stubProcessList.add(processName);
				StubInfo stubInfo = stubInfoMap.get(processName);
				if (stubInfo == null) {
					stubInfo = new StubInfo();
					stubInfo.processName = processName;
					stubInfoMap.put(processName, stubInfo);
				}
				if (stubInfo.providerInfo == null) {
					stubInfo.providerInfo = providerInfo;
				}
			}
		}
		sService.set(this);

	}

	private boolean isStubComponent(ComponentInfo componentInfo) {
		Bundle metaData = componentInfo.metaData;
		return metaData != null
				&& TextUtils.equals(metaData.getString(Constants.META_KEY_IDENTITY), Constants.META_VALUE_STUB);
	}

	public Collection<StubInfo> getStubs() {
		return stubInfoMap.values();
	}

	public Set<String> getStubProcessList() {
		return Collections.unmodifiableSet(stubProcessList);
	}

	@Override
	public VActRedirectResult redirectTargetActivity(final VRedirectActRequest request)
			throws RemoteException {
		synchronized (this) {
			return redirectTargetActivityLocked(request);
		}
	}

	private VActRedirectResult redirectTargetActivityLocked(VRedirectActRequest request) {
		if (request == null || request.targetActInfo == null) {
			return null;
		}
		VLog.d(TAG, "Jump to " + request.targetActInfo.name);
		int launchFlags = request.targetFlags;
		int resultFlags = 0;
		ActivityInfo targetActInfo = request.targetActInfo;
		String targetProcessName = ComponentUtils.getProcessName(targetActInfo);
		IBinder replaceToken = null;
		if (request.fromHost) {
			resultFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
			resultFlags |= Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
			resultFlags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
		} else {
			String taskAffinity = ComponentUtils.getTaskAffinity(targetActInfo);
			ActivityRecord sourceRecord = mMainStack.findRecord(request.resultTo);

			if ((launchFlags & Intent.FLAG_ACTIVITY_CLEAR_TASK) != 0) {
				ActivityTaskRecord task = mMainStack.findTask(taskAffinity);
				if (task != null) {
					for (ActivityRecord r : task.activityList) {
						ActivityManagerCompat.finishActivity(r.token, -1, null);
					}
				}
			}

			if (targetActInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
				ActivityTaskRecord inTask = mMainStack.findTask(taskAffinity);
				if (inTask != null) {
					am.moveTaskToFront(inTask.taskId, 0);
					ActivityRecord r = inTask.topActivity();
					ProcessRecord processRecord = VProcessService.getService().findProcess(r.pid);
					// Only one Activity in the SingleInstance task
					return new VActRedirectResult(r.token, processRecord.thread.asBinder());
				} else {
					resultFlags |= Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
					resultFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
				}
			}

			if (targetActInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {
				ActivityTaskRecord topTask = getTopTask();
				if (topTask != null && topTask.isOnTop(targetActInfo)) {
					ActivityRecord r = topTask.topActivity();
					ProcessRecord processRecord = VProcessService.getService().findProcess(r.pid);
					// The top Activity is the target Activity
					return new VActRedirectResult(r.token, processRecord.thread.asBinder());
				}
			}

			if (targetActInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {
				resultFlags |= Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
				ActivityTaskRecord topTask = getTopTask();
				if (topTask != null && topTask.isInTask(targetActInfo)) {
					int size = topTask.size();
					ActivityRecord top = null;
					ListIterator<ActivityRecord> iterator = topTask.activityList.listIterator(size);
					while (iterator.hasPrevious()) {
						top = iterator.previous();
						if (ComponentUtils.isSameComponent(top.activityInfo, targetActInfo)) {
							break;
						}
						ActivityManagerCompat.finishActivity(top.token, -1, null);
					}
					if (top != null) {
						ProcessRecord processRecord = VProcessService.getService().findProcess(top.pid);
						// The top Activity is the target Activity
						return new VActRedirectResult(top.token, processRecord.thread.asBinder());
					}
				}
			}

			if (sourceRecord != null && sourceRecord.caller != null) {
				if (sourceRecord.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
					String comebackTaskAffinity = ComponentUtils.getTaskAffinity(sourceRecord.caller);
					synchronized (mMainStack) {
						ActivityTaskRecord comebackTask = mMainStack.findTask(comebackTaskAffinity);
						if (comebackTask != null) {
							am.moveTaskToFront(comebackTask.taskId, 0);
							replaceToken = comebackTask.topActivityToken();
						}
					}
				}
			}
			if ((launchFlags & Intent.FLAG_ACTIVITY_NO_USER_ACTION) != 0) {
				resultFlags |= Intent.FLAG_ACTIVITY_NO_USER_ACTION;
			}
			if ((launchFlags & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0) {
				ActivityTaskRecord task = mMainStack.findTask(taskAffinity);
				if (task != null && task.isInTask(targetActInfo)) {
					if (task.isOnTop(targetActInfo)) {
						ActivityManagerCompat.finishActivity(task.topActivityToken(), -1, null);
						return new VActRedirectResult();
					}
					List<ActivityRecord> activityList = task.activityList;
					ListIterator<ActivityRecord> iterator = activityList.listIterator();
					while (iterator.hasNext()) {
						ActivityRecord current = iterator.next();
						if (ComponentUtils.isSameComponent(current.activityInfo, targetActInfo)) {
							while (iterator.hasNext()) {
								ActivityRecord afterCurrent = iterator.next();
								ActivityManagerCompat.finishActivity(afterCurrent.token, -1, null);
							}
							ProcessRecord processRecord = VProcessService.getService().findProcess(current.pid);
							return new VActRedirectResult(current.token, processRecord.thread.asBinder());
						}
					}
				}
			}
		}
		ProcessRecord processRecord = VProcessService.getService().startProcess(targetProcessName,
				targetActInfo.applicationInfo);
		if (processRecord == null) {
			return null;
		}
		StubInfo selectedStub = processRecord.stubInfo;
		ActivityInfo stubActInfo = selectedStub.fetchStubActivityInfo(targetActInfo);
		if (stubActInfo == null) {
			return null;
		}
		VActRedirectResult result = new VActRedirectResult(stubActInfo, resultFlags);
		// Workaround: issue #33 START
		if (request.resultTo == null && replaceToken == null) {
			ActivityTaskRecord r = getTopTask();
			if (r != null) {
				replaceToken = r.topActivity().token;
			}
		}
		result.replaceToken = replaceToken;
		// Workaround: issue #33 END
		return result;
	}

	@Override
	public void onActivityCreated(IBinder token, ActivityInfo targetActInfo, ActivityInfo callerActInfo, int taskId) {
		synchronized (mMainStack) {
			ActivityTaskRecord task = mMainStack.findTask(taskId);
			if (task == null) {
				task = new ActivityTaskRecord();
				task.taskId = taskId;
				task.rootAffinity = ComponentUtils.getTaskAffinity(targetActInfo);
				task.baseActivity = new ComponentName(targetActInfo.packageName, targetActInfo.name);
				mMainStack.tasks.add(task);
			}
			ActivityRecord record = new ActivityRecord();
			record.activityInfo = targetActInfo;
			record.token = token;
			record.caller = callerActInfo;
			record.pid = Binder.getCallingPid();
			task.activityList.add(record);
			task.activities.put(token, record);
		}
	}

	@Override
	public void onActivityResumed(IBinder token) {
		synchronized (mMainStack) {
			ActivityTaskRecord r = mMainStack.findTask(token);
			if (r != null) {
				ActivityRecord record = r.activities.get(token);
				if (r.activityList.peekLast() != record) {
					r.activityList.remove(record);
					r.activityList.addLast(record);
				}
			}
		}
	}

	@Override
	public void onActivityDestroyed(IBinder token) {
		synchronized (mMainStack) {
			ActivityTaskRecord r = mMainStack.findTask(token);
			if (r != null) {
				ActivityRecord record = r.activities.remove(token);
				r.activityList.remove(record);
				if (r.activityList.isEmpty()) {
					mMainStack.tasks.remove(r);
				}
			}
		}
	}

	@Override
	public AppTaskInfo getTaskInfo(int taskId) {
		synchronized (mMainStack) {
			ActivityTaskRecord r = mMainStack.findTask(taskId);
			if (r != null) {
				return r.toTaskInfo();
			}
		}
		return null;
	}

	@Override
	public String getPackageForToken(IBinder token) {
		synchronized (mMainStack) {
			ActivityRecord r = mMainStack.findRecord(token);
			if (r != null) {
				return r.caller != null ? r.caller.packageName : null;
			}
			return null;
		}
	}

	private synchronized int getTopTaskId() {
		List<ActivityManager.RunningTaskInfo> taskInfos = am.getRunningTasks(1);
		if (taskInfos.size() > 0) {
			return taskInfos.get(0).id;
		}
		return -1;
	}

	public ActivityTaskRecord getTopTask() {
		synchronized (mMainStack) {
			int taskId = getTopTaskId();
			if (taskId == -1) {
				return null;
			}
			return mMainStack.findTask(taskId);
		}
	}

	public void processDied(ProcessRecord record) {
		synchronized (mMainStack) {
			int pid = record.pid;
			List<Pair<ActivityTaskRecord, ActivityRecord>> removeRecordList = new LinkedList<>();
			for (ActivityTaskRecord task : mMainStack.tasks) {
				for (ActivityRecord r : task.activities.values()) {
					if (r.pid == pid) {
						removeRecordList.add(Pair.create(task, r));
					}
				}
			}
			for (Pair<ActivityTaskRecord, ActivityRecord> pair : removeRecordList) {
				ActivityTaskRecord taskRecord = pair.first;
				ActivityRecord r = pair.second;
				taskRecord.activities.remove(r.token);
				taskRecord.activityList.remove(r);
			}
			mMainStack.trimTasks();
		}
	}

	public ActivityInfo getCallingActivity(IBinder token) {
		synchronized (mMainStack) {
			ActivityRecord r = mMainStack.findRecord(token);
			if (r != null) {
				return r.caller;
			}
			return null;
		}
	}

	@Override
	public ActivityInfo getActivityInfo(IBinder token) {
		synchronized (mMainStack) {
			if (token == null) {
				return null;
			}
			ActivityRecord r = mMainStack.findRecord(token);
			if (r != null) {
				return r.activityInfo;
			}
			return null;
		}
	}

	@Override
	public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
		try {
			return super.onTransact(code, data, reply, flags);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
}
