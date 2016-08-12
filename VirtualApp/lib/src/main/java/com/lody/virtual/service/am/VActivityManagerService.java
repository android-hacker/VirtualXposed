package com.lody.virtual.service.am;

import android.app.ActivityManager;
import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Pair;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.service.ProviderCaller;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.compat.IApplicationThreadCompat;
import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IActivityManager;
import com.lody.virtual.service.interfaces.IProcessObserver;
import com.lody.virtual.service.pm.VPackageManagerService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static android.app.ActivityThread.SERVICE_DONE_EXECUTING_STOP;
import static android.os.Process.killProcess;

/**
 * @author Lody
 *
 */
public class VActivityManagerService extends IActivityManager.Stub {

	private static final AtomicReference<VActivityManagerService> sService = new AtomicReference<>();
	private static final String TAG = VActivityManagerService.class.getSimpleName();

	private final Map<String, StubInfo> stubInfoMap = new ConcurrentHashMap<>();
	private final Set<String> stubProcessList = new HashSet<String>();
	private final ActivityStack mMainStack = new ActivityStack();
	private final List<ServiceRecord> mHistory = new ArrayList<ServiceRecord>();
	private final ProviderList mProviderList = new ProviderList();
	private final ProcessMap mProcessMap = new ProcessMap();
	private ActivityManager am = (ActivityManager) VirtualCore.getCore().getContext()
			.getSystemService(Context.ACTIVITY_SERVICE);
	private Map<String, ProcessRecord> mPendingProcesses = new HashMap<>();

	public static VActivityManagerService getService() {
		return sService.get();
	}

	public static void systemReady(Context context) {
		new VActivityManagerService().onCreate(context);
	}

	private static ServiceInfo resolveServiceInfo(Intent service) {
		if (service != null) {
			ServiceInfo serviceInfo = VirtualCore.getCore().resolveServiceInfo(service);
			if (serviceInfo != null) {
				return serviceInfo;
			}
		}
		return null;
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
			if (providerInfo.authority == null) {
				continue;
			}
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
	public VActRedirectResult redirectTargetActivity(final VRedirectActRequest request) {
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
					ProcessRecord processRecord = findProcess(r.pid);
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
					ProcessRecord processRecord = findProcess(r.pid);
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
						ProcessRecord processRecord = findProcess(top.pid);
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
							ProcessRecord processRecord = findProcess(current.pid);
							return new VActRedirectResult(current.token, processRecord.thread.asBinder());
						}
					}
				}
			}
		}
		ProcessRecord processRecord = startProcess(targetProcessName, targetActInfo.applicationInfo);
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

	public void processDead(ProcessRecord record) {
		synchronized (mHistory) {
			ListIterator<ServiceRecord> iterator = mHistory.listIterator();
			while (iterator.hasNext()) {
				ServiceRecord r = iterator.next();
				if (ComponentUtils.getProcessName(r.serviceInfo).equals(record.processName)) {
					iterator.remove();
				}
			}
		}
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
		for (ProviderInfo info : record.providers) {
			mProviderList.removeAuthority(info.name);
		}
	}

	@Override
	public void ensureAppBound(String processName, ApplicationInfo appInfo) {
		int pid = getCallingPid();
		ProcessRecord app = findProcess(pid);
		if (app == null) {
			app = mPendingProcesses.get(processName);
		}
		if (app == null && processName != null && appInfo != null) {
			appInfo.flags |= ApplicationInfo.FLAG_HAS_CODE;
			String stubProcessName = getProcessName(pid);
			StubInfo stubInfo = null;
			for (StubInfo info : getStubs()) {
				if (info.processName.equals(stubProcessName)) {
					stubInfo = info;
					break;
				}
			}
			if (stubInfo != null) {
				performStartProcessLocked(stubInfo, appInfo, processName);
			}
		}
	}

	private String getProcessName(int pid) {
		for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
			if (info.pid == pid) {
				return info.processName;
			}
		}
		return null;
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

	private void addRecord(ServiceRecord r) {
		mHistory.add(r);
	}

	private ServiceRecord findRecord(ServiceInfo serviceInfo) {
		synchronized (mHistory) {
			for (ServiceRecord r : mHistory) {
				if (ComponentUtils.isSameComponent(serviceInfo, r.serviceInfo)) {
					return r;
				}
			}
			return null;
		}
	}

	private ServiceRecord findRecord(IServiceConnection connection) {
		synchronized (mHistory) {
			for (ServiceRecord r : mHistory) {
				if (r.containConnection(connection)) {
					return r;
				}
			}
			return null;
		}
	}

	private ServiceRecord findRecord(IBinder token) {
		synchronized (mHistory) {
			for (ServiceRecord r : mHistory) {
				if (r.token == token) {
					return r;
				}
			}
			return null;
		}
	}

	@Override
	public ComponentName startService(IBinder caller, Intent service, String resolvedType) {
		synchronized (this) {
			return startServiceCommon(caller, service, resolvedType, true);
		}
	}

	private ComponentName startServiceCommon(IBinder caller, Intent service, String resolvedType,
			boolean scheduleServiceArgs) {
		ServiceInfo serviceInfo = resolveServiceInfo(service);
		if (serviceInfo == null) {
			return null;
		}
		ProcessRecord targetApp = startProcess(ComponentUtils.getProcessName(serviceInfo),
				serviceInfo.applicationInfo);

		if (targetApp == null) {
			VLog.e(TAG, "Unable to start new Process for : " + ComponentUtils.toComponentName(serviceInfo));
			return null;
		}
		if (targetApp.thread == null) {
			targetApp.attachLock.block();
		}
		IApplicationThread appThread = targetApp.thread;
		ServiceRecord r = findRecord(serviceInfo);
		if (r == null) {
			r = new ServiceRecord();
			r.pid = targetApp.pid;
			r.startId = 0;
			r.activeSince = SystemClock.elapsedRealtime();
			r.targetAppThread = appThread;
			r.token = new Binder();
			r.serviceInfo = serviceInfo;
			try {
				IApplicationThreadCompat.scheduleCreateService(appThread, r.token, r.serviceInfo, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			addRecord(r);
		}
		r.lastActivityTime = SystemClock.uptimeMillis();
		if (scheduleServiceArgs) {
			r.startId++;
			boolean taskRemoved = serviceInfo.applicationInfo != null
					&& serviceInfo.applicationInfo.targetSdkVersion < Build.VERSION_CODES.ECLAIR;
			try {
				IApplicationThreadCompat.scheduleServiceArgs(appThread, r.token, taskRemoved, r.startId, 0, service);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return new ComponentName(serviceInfo.packageName, serviceInfo.name);
	}

	@Override
	public int stopService(IBinder caller, Intent service, String resolvedType) {
		synchronized (this) {
			ServiceInfo serviceInfo = resolveServiceInfo(service);
			if (serviceInfo == null) {
				return 0;
			}
			ServiceRecord r = findRecord(serviceInfo);
			if (r == null) {
				return 0;
			}
			if (!r.hasSomeBound()) {
				try {
					IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					mHistory.remove(r);
				}
			}
			return 1;
		}
	}

	@Override
	public boolean stopServiceToken(ComponentName className, IBinder token, int startId) {
		synchronized (this) {
			ServiceRecord r = findRecord(token);
			if (r == null) {
				return false;
			}
			if (r.startId == startId) {
				try {
					IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					mHistory.remove(r);
				}
				return true;
			}
			return false;
		}
	}

	@Override
	public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification,
			boolean keepNotification) {

	}

	@Override
	public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType,
			IServiceConnection connection, int flags) {
		synchronized (this) {
			ServiceInfo serviceInfo = resolveServiceInfo(service);
			if (serviceInfo == null) {
				return 0;
			}
			ServiceRecord r = findRecord(serviceInfo);
			if (r == null) {
				if ((flags & Context.BIND_AUTO_CREATE) != 0) {
					startServiceCommon(caller, service, resolvedType, false);
					r = findRecord(serviceInfo);
				}
			}
			if (r == null) {
				return 0;
			}
			if (r.binder != null && r.binder.isBinderAlive()) {
				if (r.doRebind) {
					try {
						IApplicationThreadCompat.scheduleBindService(r.targetAppThread, r.token, service, true, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				ComponentName componentName = new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name);
				try {
					connection.connected(componentName, r.binder);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					IApplicationThreadCompat.scheduleBindService(r.targetAppThread, r.token, service, r.doRebind, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			r.lastActivityTime = SystemClock.uptimeMillis();
			r.addToBoundIntent(service, connection);

			return 1;
		}
	}

	@Override
	public boolean unbindService(IServiceConnection connection) {
		synchronized (this) {
			ServiceRecord r = findRecord(connection);
			if (r == null) {
				return false;
			}
			Intent intent = r.removedConnection(connection);
			try {
				IApplicationThreadCompat.scheduleUnbindService(r.targetAppThread, r.token, intent);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (r.startId <= 0 && r.getAllConnections().isEmpty()) {
				try {
					IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					mHistory.remove(r);
				}
			}
			return true;
		}
	}

	@Override
	public void unbindFinished(IBinder token, Intent service, boolean doRebind) {
		synchronized (this) {
			ServiceRecord r = findRecord(token);
			if (r != null) {
				r.doRebind = doRebind;
			}
		}
	}

	@Override
	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
		synchronized (this) {
			ServiceRecord r = findRecord(token);
			if (r == null) {
				return;
			}
			if (SERVICE_DONE_EXECUTING_STOP == type) {
				mHistory.remove(r);
			}
		}
	}

	@Override
	public IBinder peekService(Intent service, String resolvedType) {
		synchronized (this) {
			ServiceInfo serviceInfo = resolveServiceInfo(service);
			if (serviceInfo == null) {
				return null;
			}
			ServiceRecord r = findRecord(serviceInfo);
			if (r != null) {
				return r.token;
			}
			return null;
		}
	}

	@Override
	public void publishService(IBinder token, Intent intent, IBinder service) {
		synchronized (this) {
			ServiceRecord r = findRecord(token);
			if (r == null) {
				return;
			}
			List<IServiceConnection> allConnections = r.getAllConnections();
			if (allConnections.isEmpty()) {
				return;
			}
			for (IServiceConnection connection : allConnections) {
				if (connection.asBinder().isBinderAlive()) {
					ComponentName componentName = new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name);
					try {
						connection.connected(componentName, service);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					allConnections.remove(connection);
				}
			}
		}
	}

	@Override
	public VParceledListSlice<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) {
		synchronized (mHistory) {
			int myUid = Process.myUid();
			List<ActivityManager.RunningServiceInfo> services = new ArrayList<>(mHistory.size());
			for (ServiceRecord r : mHistory) {
				ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
				info.uid = myUid;
				info.pid = r.pid;
				ProcessRecord processRecord = findProcess(r.pid);
				if (processRecord != null) {
					info.process = processRecord.processName;
					info.clientPackage = processRecord.info.packageName;
				}
				info.activeSince = r.activeSince;
				info.lastActivityTime = r.lastActivityTime;
				info.clientCount = r.getClientCount();
				info.service = ComponentUtils.toComponentName(r.serviceInfo);
				info.started = r.startId > 0;
			}
			return new VParceledListSlice<>(services);
		}
	}

	@Override
	public android.app.IActivityManager.ContentProviderHolder getContentProvider(String name) {
		if (TextUtils.isEmpty(name)) {
			return null;
		}
		ProviderInfo providerInfo = VPackageManagerService.getService().resolveContentProvider(name, 0);
		if (providerInfo == null) {
			return null;
		}
		ProcessRecord targetApp = findProcess(providerInfo.processName);
		if (targetApp != null) {
			android.app.IActivityManager.ContentProviderHolder holder = mProviderList.getHolder(name);
			if (holder == null) {
				holder = new android.app.IActivityManager.ContentProviderHolder(providerInfo);
			}
			return holder;
		} else {
			targetApp = startProcess(ComponentUtils.getProcessName(providerInfo), providerInfo.applicationInfo);
			if (targetApp == null) {
				return null;
			}
		}
		if (!targetApp.doneExecuting) {
			targetApp.lock.block();
		}
		return mProviderList.getHolder(name);
	}

	@Override
	public void publishContentProviders(List<android.app.IActivityManager.ContentProviderHolder> holderList) {

		if (holderList == null || holderList.isEmpty()) {
			return;
		}
		for (android.app.IActivityManager.ContentProviderHolder holder : holderList) {
			ProviderInfo providerInfo = holder.info;

			if (holder.provider == null || providerInfo == null || providerInfo.authority == null) {
				continue;
			}
			final String authority = providerInfo.authority;
			IBinder pb = holder.provider.asBinder();
			if (!linkProviderToDeath(authority, pb)) {
				VLog.e(TAG, "Link Provider(%s) failed.", authority);
			}

			synchronized (mProviderList) {
				String auths[] = authority.split(";");
				for (String oneAuth : auths) {
					mProviderList.putHolder(oneAuth, holder);
				}
			}
		}
	}

	private boolean linkProviderToDeath(final String authority, final IBinder binder) {
		if (binder == null) {
			return false;
		}
		try {
			binder.linkToDeath(new DeathRecipient() {
				@Override
				public void binderDied() {
					mProviderList.removeAuthority(authority);
					binder.unlinkToDeath(this, 0);
				}
			}, 0);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void attachClient(final IBinder clientBinder) {
		synchronized (this) {
			int callingPid = Binder.getCallingPid();
			final IVClient client = IVClient.Stub.asInterface(clientBinder);
			if (client == null) {
				killProcess(callingPid);
				return;
			}
			IApplicationThread thread = null;
			try {
				thread = ApplicationThreadNative.asInterface(client.getAppThread());
			} catch (RemoteException e) {
				// client has dead
			}
			if (thread == null) {
				killProcess(callingPid);
				return;
			}
			ProcessRecord app = null;
			try {
				IBinder token = client.getToken();
				if (token instanceof ProcessRecord) {
					app = (ProcessRecord) token;
				}
			} catch (RemoteException e) {
				// client has dead
			}
			if (app == null) {
				killProcess(callingPid);
				return;
			}
			try {
				final ProcessRecord record = app;
				clientBinder.linkToDeath(new DeathRecipient() {
					@Override
					public void binderDied() {
						clientBinder.unlinkToDeath(this, 0);
						onProcessDead(record);
					}
				}, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			app.client = client;
			app.thread = thread;
			app.pid = callingPid;
			mPendingProcesses.remove(app.processName);
			mProcessMap.put(app);
			app.attachLock.open();
			try {
				client.bindApplication(app.processName, app.info, app.sharedPackages, app.providers);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void onProcessDead(ProcessRecord record) {
		VLog.d(TAG, "Process %s died.", record.processName);
		mProcessMap.remove(record.pid);
		processDead(record);
		record.lock.open();
	}

	public ProcessRecord startProcess(String processName, ApplicationInfo info) {
		synchronized (this) {
			VLog.d(TAG, "startProcess %s (%s).", processName, info.packageName);
			ProcessRecord app = mProcessMap.get(processName);
			if (app != null) {
				if (!app.pkgList.contains(info.packageName)) {
					app.pkgList.add(info.packageName);
				}
				return app;
			}
			app = mPendingProcesses.get(processName);
			if (app != null) {
				return app;
			}
			StubInfo stubInfo = queryFreeStubForProcess(processName);
			if (stubInfo == null) {
				return null;
			}
			app = performStartProcessLocked(stubInfo, info, processName);
			return app;
		}
	}

	private ProcessRecord performStartProcessLocked(StubInfo stubInfo, ApplicationInfo info, String processName) {
		List<String> sharedPackages = VPackageManagerService.getService().querySharedPackages(info.packageName);
		List<ProviderInfo> providers = VPackageManagerService.getService().queryContentProviders(processName, 0).getList();
		ProcessRecord app = new ProcessRecord(stubInfo, info, processName, providers, sharedPackages);
		mPendingProcesses.put(processName, app);
		Bundle extras = new Bundle();
		BundleCompat.putBinder(extras, ExtraConstants.EXTRA_BINDER, app);
		ProviderCaller.call(stubInfo, MethodConstants.INIT_PROCESS, null, extras);
		return app;
	}

	private StubInfo queryFreeStubForProcess(String processName) {
		for (StubInfo stubInfo : getStubs()) {
			if (mProcessMap.get(stubInfo) == null && !mPendingProcesses.containsKey(processName)) {
				return stubInfo;
			}
		}
		return null;
	}

	@Override
	public boolean isAppProcess(String processName) {
		if (!TextUtils.isEmpty(processName)) {
			Set<String> processList = getStubProcessList();
			return processList.contains(processName);
		}
		return false;
	}

	@Override
	public boolean isAppPid(int pid) {
		return findProcess(pid) != null;
	}

	@Override
	public String getAppProcessName(int pid) {
		synchronized (mProcessMap) {
			ProcessRecord r = mProcessMap.get(pid);
			if (r != null) {
				return r.processName;
			}
		}
		return null;
	}

	@Override
	public List<String> getProcessPkgList(int pid) {
		synchronized (mProcessMap) {
			ProcessRecord r = mProcessMap.get(pid);
			if (r != null) {
				return new ArrayList<String>(r.pkgList);
			}
		}
		return null;
	}

	@Override
	public void killAllApps() {
		synchronized (mProcessMap) {
			mProcessMap.foreach(new ProcessMap.Visitor() {
				@Override
				public boolean accept(ProcessRecord record) {
					killProcess(record.pid);
					return true;
				}
			});
		}
	}

	@Override
	public void killAppByPkg(final String pkg) {
		synchronized (mProcessMap) {
			mProcessMap.foreach(new ProcessMap.Visitor() {
				@Override
				public boolean accept(ProcessRecord record) {
					if (record.pkgList.contains(pkg)) {
						killProcess(record.pid);
					}
					return true;
				}
			});
		}
	}

	@Override
	public void killApplicationProcess(final String procName, int uid) {
		synchronized (mProcessMap) {
			mProcessMap.foreach(new ProcessMap.Visitor() {
				@Override
				public boolean accept(ProcessRecord record) {
					if (record.processName.equals(procName)) {
						killProcess(record.pid);
					}
					return true;
				}
			});
		}
	}

	@Override
	public void dump() {

	}

	@Override
	public void registerProcessObserver(IProcessObserver observer) {

	}

	@Override
	public void unregisterProcessObserver(IProcessObserver observer) {

	}

	@Override
	public String getInitialPackage(int pid) {
		synchronized (mProcessMap) {
			ProcessRecord r = mProcessMap.get(pid);
			if (r != null) {
				return r.info.packageName;
			}
			return null;
		}
	}

	@Override
	public void handleApplicationCrash() {
		synchronized (mProcessMap) {
			ProcessRecord r = mProcessMap.get(Binder.getCallingPid());
			if (r != null) {
				r.lock.open();
			}
		}
	}

	@Override
	public void appDoneExecuting() {
		synchronized (mProcessMap) {
			ProcessRecord r = mProcessMap.get(Binder.getCallingPid());
			if (r != null) {
				r.doneExecuting = true;
				r.lock.open();
			}
		}
	}

	public ProcessRecord findProcess(int pid) {
		return mProcessMap.get(pid);
	}

	public ProcessRecord findProcess(String processName) {
		return mProcessMap.get(processName);
	}
}
