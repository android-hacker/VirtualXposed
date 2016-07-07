package com.lody.virtual.service;

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
import com.lody.virtual.helper.utils.XLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lody
 *
 */
public class VActivityServiceImpl extends IActivityManager.Stub {

	private static final VActivityServiceImpl gService = new VActivityServiceImpl();
	private static final String TAG = VActivityServiceImpl.class.getSimpleName();
	private final List<ActivityInfo> stubActivityList = new ArrayList<ActivityInfo>();

	private final Map<String, StubInfo> stubInfoMap = new HashMap<String, StubInfo>();
	private final Set<String> stubProcessList = new HashSet<String>();
	private ActivityStack stack = new ActivityStack();
	private ActivityManager am = (ActivityManager) VirtualCore.getCore().getContext()
			.getSystemService(Context.ACTIVITY_SERVICE);

	public static VActivityServiceImpl getService() {
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

	public VActRedirectResult redirectTargetActivity(final VRedirectActRequest request) throws RemoteException {
		if (request == null || request.targetActInfo == null) {
			return null;
		}
		int requestFlags = request.targetFlags;
		int resultFlags = 0;
		ActivityInfo targetActInfo = request.targetActInfo;
		String targetProcessName = ComponentUtils.getProcessName(targetActInfo);
		if ((requestFlags & Intent.FLAG_ACTIVITY_MULTIPLE_TASK) != 0) {
			resultFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
			resultFlags |= Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
		}
		StubInfo selectStubInfo = fetchRunningStubInfo(targetProcessName);
		if (selectStubInfo == null) {
			selectStubInfo = VProcessServiceImpl.getService().fetchFreeStubInfo(stubInfoMap.values());
		}
		if (selectStubInfo == null) {
			return null;
		}
		ActivityInfo stubActInfo = selectStubInfo.fetchStubActivityInfo(targetActInfo);

		XLog.d(TAG, "Select StubAct(%s) -> TargetAct(%s).", stubActInfo.name, targetActInfo.name);
		return new VActRedirectResult(stubActInfo, resultFlags);

	}

	public ProviderInfo fetchServiceRuntime(ServiceInfo serviceInfo) {
		if (serviceInfo == null) {
			return null;
		}
		String plugProcName = ComponentUtils.getProcessName(serviceInfo);
		ProviderInfo runningEnv = fetchRunningServiceRuntime(plugProcName);
		if (runningEnv == null) {
			StubInfo stubInfo = VProcessServiceImpl.getService().fetchFreeStubInfo(stubInfoMap.values());
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
			String plugProcName = ComponentUtils.getProcessName(serviceInfo);
			return fetchRunningServiceRuntime(plugProcName);
		}
		return null;
	}

	public ProviderInfo fetchRunningServiceRuntime(String plugProcName) {
		StubInfo stubInfo = fetchRunningStubInfo(plugProcName);
		if (stubInfo != null) {
			return stubInfo.providerInfos.get(0);
		}
		return null;
	}

	public StubInfo fetchRunningStubInfo(String plugProcName) {
		return VProcessServiceImpl.getService().findStubInfo(plugProcName);
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

	public AppTaskInfo getTaskInfo(int taskId) {
		ActivityTaskRecord r = stack.findTask(taskId);
		if (r != null) {
			return r.toTaskInfo();
		}
		return null;
	}

	private int getTopTaskId() {
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

	public enum LaunchMode {
		SINGLE_TOP, SINGLE_TASK, SINGLE_INSTANCE, STANDARD {
			@Override
			boolean isSingle() {
				return false;
			}
		};

		boolean isSingle() {
			return true;
		}
	}

	static class StubInfo {
		String processName;
		List<ActivityInfo> standardActivityInfos = new ArrayList<ActivityInfo>(1);
		List<ProviderInfo> providerInfos = new ArrayList<ProviderInfo>(1);

		public void verify() {
			if (standardActivityInfos.isEmpty()) {
				throw new IllegalStateException("Unable to find any StubActivity in " + processName);
			}
			if (providerInfos.isEmpty()) {
				throw new IllegalStateException("Unable to find any StubProvider in " + processName);
			}
		}
		public ActivityInfo fetchStubActivityInfo(ActivityInfo targetActInfo) {
			return standardActivityInfos.get(0);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof StubInfo && TextUtils.equals(((StubInfo) o).processName, processName);
		}

	}


}
