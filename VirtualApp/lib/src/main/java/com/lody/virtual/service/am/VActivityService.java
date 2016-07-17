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
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.service.IActivityManager;
import com.lody.virtual.service.process.VProcessService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 *
 */
public class VActivityService extends IActivityManager.Stub {

	private static final VActivityService gService = new VActivityService();
	private static final String TAG = VActivityService.class.getSimpleName();
	private final List<ActivityInfo> stubActivityList = new ArrayList<ActivityInfo>();

	private final Map<String, StubInfo> stubInfoMap = new ConcurrentHashMap<>();
	private final Set<String> stubProcessList = new HashSet<String>();
	private final ActivityStack stack = new ActivityStack();
	private ActivityManager am = (ActivityManager) VirtualCore.getCore().getContext()
			.getSystemService(Context.ACTIVITY_SERVICE);

	public static VActivityService getService() {
		return gService;
	}


	public void onCreate(Context context) {
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
				stubInfo.standardActivityInfos.add(activityInfo);
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
				stubInfo.providerInfos.add(providerInfo);
			}
		}

	}

	private boolean isStubComponent(ComponentInfo componentInfo) {
		Bundle metaData = componentInfo.metaData;
		return metaData != null
				&& TextUtils.equals(metaData.getString(Constants.X_META_KEY_IDENTITY), Constants.X_META_VALUE_STUB);
	}

	public List<ActivityInfo> getStubActivityList() {
		return Collections.unmodifiableList(stubActivityList);
	}

	public Map<String, StubInfo> getStubInfoMap() {
		return stubInfoMap;
	}

	public Set<String> getStubProcessList() {
		return Collections.unmodifiableSet(stubProcessList);
	}

	@Override
	public synchronized VActRedirectResult redirectTargetActivity(final VRedirectActRequest request) throws RemoteException {
		if (request == null || request.targetActInfo == null) {
			return null;
		}
		int resultFlags = 0;
		ActivityInfo targetActInfo = request.targetActInfo;
		String targetProcessName = ComponentUtils.getProcessName(targetActInfo);
		if (request.fromHost) {
			resultFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
			resultFlags |= Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
			resultFlags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
		}

		StubInfo selectStubInfo = fetchRunningStubInfo(targetProcessName);
		if (selectStubInfo == null) {
			selectStubInfo = VProcessService.getService().fetchFreeStubInfo(stubInfoMap.values());
		}
		if (selectStubInfo == null) {
			return null;
		}
		ActivityInfo stubActInfo = selectStubInfo.fetchStubActivityInfo(targetActInfo);
		return new VActRedirectResult(stubActInfo, resultFlags);
	}

	public ProviderInfo fetchServiceRuntime(ServiceInfo serviceInfo) {
		if (serviceInfo == null) {
			return null;
		}
		String targetProcessName = ComponentUtils.getProcessName(serviceInfo);
		ProviderInfo runningEnv = fetchRunningServiceRuntime(targetProcessName);
		if (runningEnv == null) {
			StubInfo stubInfo = VProcessService.getService().fetchFreeStubInfo(stubInfoMap.values());
			if (stubInfo != null) {
				runningEnv = stubInfo.providerInfos.get(0);
			}
		}
		if (runningEnv != null) {
			return runningEnv;
		}
		return null;
	}

	public ProviderInfo fetchRunningServiceRuntime(ServiceInfo serviceInfo) {
		if (serviceInfo != null) {
			String appProcessName = ComponentUtils.getProcessName(serviceInfo);
			return fetchRunningServiceRuntime(appProcessName);
		}
		return null;
	}

	public ProviderInfo fetchRunningServiceRuntime(String appProcessName) {
		StubInfo stubInfo = fetchRunningStubInfo(appProcessName);
		if (stubInfo != null) {
			return stubInfo.providerInfos.get(0);
		}
		return null;
	}

	public StubInfo fetchRunningStubInfo(String appProcessName) {
		return VProcessService.getService().findStubInfo(appProcessName);
	}

	public StubInfo findStubInfo(String stubProcName) {
		return stubProcName == null ? null : stubInfoMap.get(stubProcName);
	}

	@Override
	public synchronized void onActivityCreated(IBinder token, ActivityInfo info, int taskId) {
		ActivityTaskRecord task = stack.findTask(taskId);
		if (task == null) {
			task = new ActivityTaskRecord();
			task.taskId = taskId;
			task.rootAffinity = ComponentUtils.getTaskAffinity(info);
			task.baseActivity = new ComponentName(info.packageName, info.name);
			stack.tasks.add(task);
		}
		ActivityRecord record = new ActivityRecord();
		record.activityInfo = info;
		record.token = token;
		record.pid = Binder.getCallingPid();
		task.activityList.add(record);
		task.activities.put(token, record);
	}

	@Override
	public synchronized void onActivityResumed(IBinder token) {
		ActivityTaskRecord r = stack.findTask(token);
		if (r != null) {
			ActivityRecord record = r.activities.get(token);
			r.activityList.remove(record);
			r.activityList.addLast(record);
		}
	}

	@Override
	public synchronized void onActivityDestroyed(IBinder token) {
		ActivityTaskRecord r = stack.findTask(token);
		if (r != null) {
			ActivityRecord record = r.activities.remove(token);
			r.activityList.remove(record);
			if (r.activityList.isEmpty()) {
				stack.tasks.remove(r);
			}
		}
	}

	@Override
	public AppTaskInfo getTaskInfo(int taskId) {
		synchronized (stack) {
			ActivityTaskRecord r = stack.findTask(taskId);
			if (r != null) {
				return r.toTaskInfo();
			}
		}
		return null;
	}

	private synchronized int getTopTaskId() {
		List<ActivityManager.RunningTaskInfo> taskInfos = am.getRunningTasks(1);
		if (taskInfos.size() > 0) {
			return taskInfos.get(0).id;
		}
		return -1;
	}

	public ActivityTaskRecord getTopTask() {
		int taskId = getTopTaskId();
		if (taskId == -1) {
			return null;
		}
		return stack.findTask(taskId);
	}

	public synchronized void processDied(int pid) {
		for (ActivityTaskRecord task : stack.tasks) {
			for (ActivityRecord r : task.activities.values()) {
				if (r.pid == pid) {
					task.activities.remove(r.token);
					task.activityList.remove(r);
				}
			}
		}
		stack.trimTasks();
	}



}
