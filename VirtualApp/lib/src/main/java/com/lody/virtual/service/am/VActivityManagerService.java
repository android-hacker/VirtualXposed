package com.lody.virtual.service.am;

import android.annotation.NonNull;
import android.app.ActivityManager;
import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Slog;

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static android.os.Process.killProcess;

/**
 * @author Lody
 *
 */
public class VActivityManagerService extends IActivityManager.Stub {

	private static final AtomicReference<VActivityManagerService> sService = new AtomicReference<>();
	private static final String TAG = VActivityManagerService.class.getSimpleName();

	static final int SERVICE_TIMEOUT_MSG = 12;

	// How long we wait for a service to finish executing.
	static final int SERVICE_TIMEOUT = 20 * 1000;

	// The minimum amount of time between restarting services that we allow.
	// That is, when multiple services are restarting, we won't allow each
	// to restart less than this amount of time from the last one.
	static final int SERVICE_MIN_RESTART_TIME_BETWEEN = 10 * 1000;

	// How long a service needs to be running until it will start back at
	// SERVICE_RESTART_DURATION after being killed.
	static final int SERVICE_RESET_RUN_DURATION = 60 * 1000;

	// Multiplying factor to increase restart duration time by, for each time
	// a service is killed before it has run for SERVICE_RESET_RUN_DURATION.
	static final int SERVICE_RESTART_DURATION_FACTOR = 4;

	// How long a service needs to be running until restarting its process
	// is no longer considered to be a relaunch of the service.
	static final int SERVICE_RESTART_DURATION = 5 * 1000;

	/**
	 * All currently running services.
	 */
	final HashMap<ComponentName, ServiceRecord> mServices = new HashMap<ComponentName, ServiceRecord>();
	/**
	 * All currently running services indexed by the Intent used to start them.
	 */
	final HashMap<Intent.FilterComparison, ServiceRecord> mServicesByIntent = new HashMap<Intent.FilterComparison, ServiceRecord>();
	/**
	 * All currently bound service connections. Keys are the IBinder of the
	 * client's IServiceConnection.
	 */
	final HashMap<IBinder, ArrayList<ConnectionRecord>> mServiceConnections = new HashMap<IBinder, ArrayList<ConnectionRecord>>();
	/**
	 * List of services that we have been asked to start, but haven't yet been
	 * able to. It is used to hold start requests while waiting for their
	 * corresponding application thread to get going.
	 */
	final ArrayList<ServiceRecord> mPendingServices = new ArrayList<ServiceRecord>();
	/**
	 * List of services that are scheduled to restart following a crash.
	 */
	final ArrayList<ServiceRecord> mRestartingServices = new ArrayList<ServiceRecord>();
	/**
	 * List of services that are in the process of being stopped.
	 */
	final ArrayList<ServiceRecord> mStoppingServices = new ArrayList<ServiceRecord>();

	private final Map<String, StubInfo> stubInfoMap = new ConcurrentHashMap<>();
	private final Set<String> stubProcessList = new HashSet<String>();
	private final ActivityStack mMainStack = new ActivityStack();
	private final ProviderList mProviderList = new ProviderList();
	private final ProcessMap mProcessMap = new ProcessMap();
	private ActivityManager am = (ActivityManager) VirtualCore.getCore().getContext()
			.getSystemService(Context.ACTIVITY_SERVICE);
	private Map<String, ProcessRecord> mPendingProcesses = new HashMap<>();

	private final ProcessRecord mSystemProcessRecord;

	public VActivityManagerService() {
		mSystemProcessRecord = new ProcessRecord(null, VirtualCore.getCore().getHostPkgInfo().applicationInfo, VirtualCore.getCore().getProcessName(), null, null);
		mSystemProcessRecord.thread = VirtualCore.mainThread().getApplicationThread();
		mSystemProcessRecord.pid = Process.myPid();
		mSystemProcessRecord.pkgList.add(VirtualCore.getCore().getHostPkg());
		mProcessMap.put(mSystemProcessRecord);
	}


	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SERVICE_TIMEOUT_MSG: {
					serviceTimeout((ProcessRecord)msg.obj);
				} break;
			}
		}
	};

	public static VActivityManagerService getService() {
		return sService.get();
	}

	public static void systemReady(Context context) {
		new VActivityManagerService().onCreate(context);
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
	public VActRedirectResult redirectTargetActivity(final VRedirectActRequest request) throws RemoteException {
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
				ActivityTaskRecord task = mMainStack.findTask(taskAffinity);
				if (task != null && task.isOnTop(targetActInfo)) {
					ActivityRecord r = task.topActivity();
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
		ProcessRecord processRecord = startProcessLocked(targetProcessName, targetActInfo.applicationInfo);
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

	void serviceTimeout(ProcessRecord proc) {
		String anrMessage = null;

		synchronized(this) {
			if (proc.executingServices.size() == 0 || proc.thread == null) {
				return;
			}
			long maxTime = SystemClock.uptimeMillis() - SERVICE_TIMEOUT;
			Iterator<ServiceRecord> it = proc.executingServices.iterator();
			ServiceRecord timeout = null;
			long nextTime = 0;
			while (it.hasNext()) {
				ServiceRecord sr = it.next();
				if (sr.executingStart < maxTime) {
					timeout = sr;
					break;
				}
				if (sr.executingStart > nextTime) {
					nextTime = sr.executingStart;
				}
			}
			if (timeout != null && mProcessMap.isExist(proc)) {
				Slog.w(TAG, "Timeout executing service: " + timeout);
				anrMessage = "Executing service " + timeout.shortName;
			} else {
				Message msg = mHandler.obtainMessage(SERVICE_TIMEOUT_MSG);
				msg.obj = proc;
				mHandler.sendMessageAtTime(msg, nextTime+SERVICE_TIMEOUT);
			}
		}

		if (anrMessage != null) {
			VLog.w(TAG, anrMessage);
		}
	}

	ActivityManager.RunningServiceInfo makeRunningServiceInfoLocked(ServiceRecord r) {
		ActivityManager.RunningServiceInfo info =
				new ActivityManager.RunningServiceInfo();
		info.service = r.name;
		if (r.app != null) {
			info.pid = r.app.pid;
		}
		info.uid = r.appInfo.uid;
		info.process = r.processName;
		info.foreground = r.isForeground;
		info.activeSince = r.createTime;
		info.started = r.startRequested;
		info.clientCount = r.connections.size();
		info.crashCount = r.crashCount;
		info.lastActivityTime = r.lastActivity;
		if (r.isForeground) {
			info.flags |= ActivityManager.RunningServiceInfo.FLAG_FOREGROUND;
		}
		if (r.startRequested) {
			info.flags |= ActivityManager.RunningServiceInfo.FLAG_STARTED;
		}
		if (r.app != null && r.app.persistent) {
			info.flags |= ActivityManager.RunningServiceInfo.FLAG_PERSISTENT_PROCESS;
		}

		for (ArrayList<ConnectionRecord> connl : r.connections.values()) {
			for (int i=0; i<connl.size(); i++) {
				ConnectionRecord conn = connl.get(i);
				if (conn.clientLabel != 0) {
					info.clientPackage = conn.binding.client.info.packageName;
					info.clientLabel = conn.clientLabel;
					return info;
				}
			}
		}
		return info;
	}

	public List<ActivityManager.RunningServiceInfo> getServicesLocked(int maxNum) {
		ArrayList<ActivityManager.RunningServiceInfo> res
                = new ArrayList<ActivityManager.RunningServiceInfo>();

		if (mServices.size() > 0) {
            Iterator<ServiceRecord> it = mServices.values().iterator();
            while (it.hasNext() && res.size() < maxNum) {
                res.add(makeRunningServiceInfoLocked(it.next()));
            }
        }

		for (int i=0; i<mRestartingServices.size() && res.size() < maxNum; i++) {
            ServiceRecord r = mRestartingServices.get(i);
            ActivityManager.RunningServiceInfo info =
                    makeRunningServiceInfoLocked(r);
            info.restarting = r.nextRestartTime;
            res.add(info);
        }

		return res;
	}

	public PendingIntent getRunningServiceControlPanel(ComponentName name) {
		synchronized (this) {
			ServiceRecord r = mServices.get(name);
			if (r != null) {
				for (ArrayList<ConnectionRecord> conn : r.connections.values()) {
					for (int i=0; i<conn.size(); i++) {
						if (conn.get(i).clientIntent != null) {
							return conn.get(i).clientIntent;
						}
					}
				}
			}
		}
		return null;
	}

	private ServiceRecord findServiceLocked(ComponentName name,
											IBinder token) {
		ServiceRecord r = mServices.get(name);
		return r == token ? r : null;
	}

		private ServiceLookupResult findServiceLocked(Intent service, String resolvedType) {
		ServiceRecord r = null;
		if (service.getComponent() != null) {
			r = mServices.get(service.getComponent());
		}
		if (r == null) {
			Intent.FilterComparison filter = new Intent.FilterComparison(service);
			r = mServicesByIntent.get(filter);
		}

		if (r == null) {
			ResolveInfo rInfo = VPackageManagerService.getService().resolveService(service, resolvedType, 0);
			ServiceInfo sInfo = rInfo != null ? rInfo.serviceInfo : null;
			if (sInfo == null) {
				return null;
			}

			ComponentName name = new ComponentName(sInfo.applicationInfo.packageName, sInfo.name);
			r = mServices.get(name);
		}
		if (r != null) {
			return new ServiceLookupResult(r, null);
		}
		return null;
	}

	private class ServiceRestarter implements Runnable {
		private ServiceRecord mService;

		void setService(ServiceRecord service) {
			mService = service;
		}

		public void run() {
			synchronized(VActivityManagerService.this) {
				performServiceRestartLocked(mService);
			}
		}
	}


	final void performServiceRestartLocked(ServiceRecord r) {
		if (!mRestartingServices.contains(r)) {
			return;
		}
		bringUpServiceLocked(r, r.intent.getIntent().getFlags(), true);
	}

	private void bumpServiceExecutingLocked(ServiceRecord r, String why) {
		VLog.v(TAG, ">>> EXECUTING "
				+ why + " of " + r + " in app " + r.app);
		long now = SystemClock.uptimeMillis();
		if (r.executeNesting == 0 && r.app != null) {
			if (r.app.executingServices.size() == 0) {
				Message msg = mHandler.obtainMessage(SERVICE_TIMEOUT_MSG);
				msg.obj = r.app;
				mHandler.sendMessageAtTime(msg, now + SERVICE_TIMEOUT);
			}
			r.app.executingServices.add(r);
		}
		r.executeNesting++;
		r.executingStart = now;
	}

	private void sendServiceArgsLocked(ServiceRecord r) {
		final int N = r.pendingStarts.size();
		if (N == 0) {
			return;
		}

		while (r.pendingStarts.size() > 0) {
			try {
				ServiceRecord.StartItem si = r.pendingStarts.remove(0);
				VLog.v(TAG, "Sending arguments to: "
						+ r + " " + r.intent + " args=" + si.intent);
				if (si.intent == null && N > 1) {
					// If somehow we got a dummy null intent in the middle,
					// then skip it.  DO NOT skip a null intent when it is
					// the only one in the list -- this is to support the
					// onStartCommand(null) case.
					continue;
				}
				si.deliveredTime = SystemClock.uptimeMillis();
				r.deliveredStarts.add(si);
				si.deliveryCount++;
				bumpServiceExecutingLocked(r, "start");
				int flags = 0;
				if (si.deliveryCount > 0) {
					flags |= Service.START_FLAG_RETRY;
				}
				if (si.doneExecutingCount > 0) {
					flags |= Service.START_FLAG_REDELIVERY;
				}
				IApplicationThreadCompat.scheduleServiceArgs(r.app.thread, r, si.taskRemoved, si.id, flags, si.intent);
			} catch (RemoteException e) {
				// Remote process gone...  we'll let the normal cleanup take
				// care of this.
				VLog.v(TAG, "Crashed while scheduling start: " + r);
				break;
			} catch (Exception e) {
				VLog.w(TAG, "Unexpected exception", e);
				break;
			}
		}
	}

	private boolean bringUpServiceLocked(ServiceRecord r,
										 int intentFlags, boolean whileRestarting) {
		//VLog.i(TAG, "Bring up service:");
		//r.dump("  ");

		if (r.app != null && r.app.thread != null) {
			sendServiceArgsLocked(r);
			return true;
		}

		if (!whileRestarting && r.restartDelay > 0) {
			// If waiting for a restart, then do nothing.
			return true;
		}

		VLog.v(TAG, "Bringing up " + r + " " + r.intent);

		// We are now bringing the service up, so no longer in the
		// restarting state.
		mRestartingServices.remove(r);

		final String appName = r.processName;
		ProcessRecord app = findProcess(appName);
		if (app != null && app.thread != null) {
			try {
				realStartServiceLocked(r, app);
				return true;
			} catch (RemoteException e) {
				VLog.w(TAG, "Exception when starting service " + r.shortName, e);
			}

			// If a dead object exception was thrown -- fall through to
			// restart the application.
		}

		// Not running -- get it started, and enqueue this service record
		// to be executed when the app comes up.
		if (!mPendingServices.contains(r)) {
			mPendingServices.add(r);
		}
		if (startProcessLocked(ComponentUtils.getProcessName(r.serviceInfo), r.appInfo) == null) {
			VLog.w(TAG, "Unable to launch app "
					+ r.appInfo.packageName + "/"
					+ r.appInfo.uid + " for service "
					+ r.intent.getIntent() + ": process is bad");
			bringDownServiceLocked(r, true);
			mPendingServices.remove(r);
			return false;
		}

		return true;
	}

	private void bringDownServiceLocked(ServiceRecord r, boolean force) {
		//VLog.i(TAG, "Bring down service:");
		//r.dump("  ");

		// Does it still need to run?
		if (!force && r.startRequested) {
			return;
		}
		if (r.connections.size() > 0) {
			if (!force) {
				// XXX should probably keep a count of the number of auto-create
				// connections directly in the service.
				Iterator<ArrayList<ConnectionRecord>> it = r.connections.values().iterator();
				while (it.hasNext()) {
					ArrayList<ConnectionRecord> cr = it.next();
					for (int i=0; i<cr.size(); i++) {
						if ((cr.get(i).flags&Context.BIND_AUTO_CREATE) != 0) {
							return;
						}
					}
				}
			}

			// Report to all of the connections that the service is no longer
			// available.
			Iterator<ArrayList<ConnectionRecord>> it = r.connections.values().iterator();
			while (it.hasNext()) {
				ArrayList<ConnectionRecord> c = it.next();
				for (int i=0; i<c.size(); i++) {
					ConnectionRecord cr = c.get(i);
					// There is still a connection to the service that is
					// being brought down.  Mark it as dead.
					cr.serviceDead = true;
					try {
						cr.conn.connected(r.name, null);
					} catch (Exception e) {
						VLog.w(TAG, "Failure disconnecting service " + r.name +
								" to connection " + c.get(i).conn.asBinder() +
								" (in " + c.get(i).binding.client.processName + ")", e);
					}
				}
			}
		}

		// Tell the service that it has been unbound.
		if (r.bindings.size() > 0 && r.app != null && r.app.thread != null) {
			Iterator<IntentBindRecord> it = r.bindings.values().iterator();
			while (it.hasNext()) {
				IntentBindRecord ibr = it.next();
				VLog.v(TAG, "Bringing down binding " + ibr
						+ ": hasBound=" + ibr.hasBound);
				if (r.app != null && r.app.thread != null && ibr.hasBound) {
					try {
						bumpServiceExecutingLocked(r, "bring down unbind");
						ibr.hasBound = false;
						IApplicationThreadCompat.scheduleUnbindService(r.app.thread, r,
								ibr.intent.getIntent());
					} catch (Exception e) {
						VLog.w(TAG, "Exception when unbinding service "
								+ r.shortName, e);
						serviceDoneExecutingLocked(r, true);
					}
				}
			}
		}

		VLog.v(TAG, "Bringing down " + r + " " + r.intent);

		mServices.remove(r.name);
		mServicesByIntent.remove(r.intent);
		r.totalRestartCount = 0;
		unscheduleServiceRestartLocked(r);

		// Also make sure it is not on the pending list.
		int N = mPendingServices.size();
		for (int i=0; i<N; i++) {
			if (mPendingServices.get(i) == r) {
				mPendingServices.remove(i);
				VLog.v(TAG, "Removed pending: " + r);
				i--;
				N--;
			}
		}

		r.cancelNotification();
		r.isForeground = false;
		r.foregroundId = 0;
		r.foregroundNoti = null;

		// Clear start entries.
		r.clearDeliveredStartsLocked();
		r.pendingStarts.clear();

		if (r.app != null) {
			r.app.services.remove(r);
			if (r.app.thread != null) {
				try {
					bumpServiceExecutingLocked(r, "stop");
					mStoppingServices.add(r);
					IApplicationThreadCompat.scheduleStopService(r.app.thread, r);
				} catch (Exception e) {
					VLog.w(TAG, "Exception when stopping service "
							+ r.shortName, e);
					serviceDoneExecutingLocked(r, true);
				}
				updateServiceForegroundLocked(r.app);
			} else {
				VLog.v(
						TAG, "Removed service that has no process: " + r);
			}
		} else {
			VLog.v(
					TAG, "Removed service that is not running: " + r);
		}

		if (r.bindings.size() > 0) {
			r.bindings.clear();
		}

		if (r.restarter instanceof ServiceRestarter) {
			((ServiceRestarter)r.restarter).setService(null);
		}
	}

	public void updateServiceForegroundLocked(ProcessRecord proc) {
		boolean anyForeground = false;
		for (ServiceRecord sr : proc.services) {
			if (sr.isForeground) {
				anyForeground = true;
				break;
			}
		}
		if (anyForeground != proc.foregroundServices) {
			proc.foregroundServices = anyForeground;
		}
	}

	public void serviceDoneExecutingLocked(ServiceRecord r, boolean inStopping) {
		VLog.v(TAG, "<<< DONE EXECUTING " + r
				+ ": nesting=" + r.executeNesting
				+ ", inStopping=" + inStopping + ", app=" + r.app);
		r.executeNesting--;
		if (r.executeNesting <= 0 && r.app != null) {
			VLog.v(TAG,
					"Nesting at 0 of " + r.shortName);
			r.app.executingServices.remove(r);
			if (r.app.executingServices.size() == 0) {
				VLog.v(TAG,
						"No more executingServices of " + r.shortName);
				mHandler.removeMessages(SERVICE_TIMEOUT_MSG, r.app);
			}
			if (inStopping) {
				VLog.v(TAG,
						"doneExecuting remove stopping " + r);
				mStoppingServices.remove(r);
				r.bindings.clear();
			}
		}
	}
	
	private boolean unscheduleServiceRestartLocked(ServiceRecord r) {
		if (r.restartDelay == 0) {
			return false;
		}
		r.resetRestartCounter();
		mRestartingServices.remove(r);
		mHandler.removeCallbacks(r.restarter);
		return true;
	}
	
	private void realStartServiceLocked(ServiceRecord r,
										ProcessRecord app) throws RemoteException {
		if (app.thread == null) {
			throw new RemoteException();
		}

		r.app = app;
		r.restartTime = r.lastActivity = SystemClock.uptimeMillis();

		app.services.add(r);
		bumpServiceExecutingLocked(r, "create");

		boolean created = false;
		try {
			IApplicationThreadCompat.scheduleCreateService(app.thread, r, r.serviceInfo, 0);
			r.postNotification();
			created = true;
		} finally {
			if (!created) {
				app.services.remove(r);
				scheduleServiceRestartLocked(r, false);
			}
		}
		requestServiceBindingsLocked(r);

		// If the service is in the started state, and there are no
		// pending arguments, then fake up one so its onStartCommand() will
		// be called.
		if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
			r.pendingStarts.add(new ServiceRecord.StartItem(r, false, r.makeNextStartId(),
					null));
		}

		sendServiceArgsLocked(r);
	}

	private void requestServiceBindingsLocked(ServiceRecord r) {
		Iterator<IntentBindRecord> bindings = r.bindings.values().iterator();
		while (bindings.hasNext()) {
			IntentBindRecord i = bindings.next();
			if (!requestServiceBindingLocked(r, i, false)) {
				break;
			}
		}
	}

	private boolean requestServiceBindingLocked(ServiceRecord r,
												IntentBindRecord i, boolean rebind) {
		if (r.app == null || r.app.thread == null) {
			// If service is not currently running, can't yet bind.
			return false;
		}
		if ((!i.requested || rebind) && i.apps.size() > 0) {
			try {
				bumpServiceExecutingLocked(r, "bind");
				IApplicationThreadCompat.scheduleBindService(r.app.thread, r, i.intent.getIntent(), rebind, 0);
				if (!rebind) {
					i.requested = true;
				}
				i.hasBound = true;
				i.doRebind = false;
			} catch (RemoteException e) {
				VLog.v(TAG, "Crashed while binding " + r);
				return false;
			}
		}
		return true;
	}


	private boolean scheduleServiceRestartLocked(ServiceRecord r,
												 boolean allowCancel) {
		boolean canceled = false;

		final long now = SystemClock.uptimeMillis();
		long minDuration = SERVICE_RESTART_DURATION;
		long resetTime = SERVICE_RESET_RUN_DURATION;

		if ((r.serviceInfo.applicationInfo.flags
				&ApplicationInfo.FLAG_PERSISTENT) != 0) {
			minDuration /= 4;
		}

		// Any delivered but not yet finished starts should be put back
		// on the pending list.
		final int N = r.deliveredStarts.size();
		if (N > 0) {
			for (int i=N-1; i>=0; i--) {
				ServiceRecord.StartItem si = r.deliveredStarts.get(i);
				if (si.intent == null) {
					// We'll generate this again if needed.
				} else if (!allowCancel || (si.deliveryCount < ServiceRecord.MAX_DELIVERY_COUNT
						&& si.doneExecutingCount < ServiceRecord.MAX_DONE_EXECUTING_COUNT)) {
					r.pendingStarts.add(0, si);
					long dur = SystemClock.uptimeMillis() - si.deliveredTime;
					dur *= 2;
					if (minDuration < dur) minDuration = dur;
					if (resetTime < dur) resetTime = dur;
				} else {
					VLog.w(TAG, "Canceling start item " + si.intent + " in service "
							+ r.name);
					canceled = true;
				}
			}
			r.deliveredStarts.clear();
		}

		r.totalRestartCount++;
		if (r.restartDelay == 0) {
			r.restartCount++;
			r.restartDelay = minDuration;
		} else {
			// If it has been a "reasonably long time" since the service
			// was started, then reset our restart duration back to
			// the beginning, so we don't infinitely increase the duration
			// on a service that just occasionally gets killed (which is
			// a normal case, due to process being killed to reclaim memory).
			if (now > (r.restartTime+resetTime)) {
				r.restartCount = 1;
				r.restartDelay = minDuration;
			} else {
				if ((r.serviceInfo.applicationInfo.flags
						&ApplicationInfo.FLAG_PERSISTENT) != 0) {
					// Services in peristent processes will restart much more
					// quickly, since they are pretty important.  (Think SystemUI).
					r.restartDelay += minDuration/2;
				} else {
					r.restartDelay *= SERVICE_RESTART_DURATION_FACTOR;
					if (r.restartDelay < minDuration) {
						r.restartDelay = minDuration;
					}
				}
			}
		}

		r.nextRestartTime = now + r.restartDelay;

		// Make sure that we don't end up restarting a bunch of services
		// all at the same time.
		boolean repeat;
		do {
			repeat = false;
			for (int i=mRestartingServices.size()-1; i>=0; i--) {
				ServiceRecord r2 = mRestartingServices.get(i);
				if (r2 != r && r.nextRestartTime
						>= (r2.nextRestartTime-SERVICE_MIN_RESTART_TIME_BETWEEN)
						&& r.nextRestartTime
						< (r2.nextRestartTime+SERVICE_MIN_RESTART_TIME_BETWEEN)) {
					r.nextRestartTime = r2.nextRestartTime + SERVICE_MIN_RESTART_TIME_BETWEEN;
					r.restartDelay = r.nextRestartTime - now;
					repeat = true;
					break;
				}
			}
		} while (repeat);

		if (!mRestartingServices.contains(r)) {
			mRestartingServices.add(r);
		}

		r.cancelNotification();

		mHandler.removeCallbacks(r.restarter);
		mHandler.postAtTime(r.restarter, r.nextRestartTime);
		r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
		VLog.w(TAG, "Scheduling restart of crashed service "
				+ r.shortName + " in " + r.restartDelay + "ms");

		return canceled;
	}

	private void killServicesLocked(ProcessRecord app,
									boolean allowRestart) {
		// Clean up any connections this application has to other services.
		if (app.connections.size() > 0) {
			for (ConnectionRecord r : app.connections) {
				removeConnectionLocked(r, app, null);
			}
		}
		app.connections.clear();

		if (app.services.size() != 0) {
			// Any services running in the application need to be placed
			// back in the pending list.
			for (ServiceRecord sr : app.services) {
				sr.app = null;
				sr.executeNesting = 0;
				if (mStoppingServices.remove(sr)) {
					VLog.v(TAG, "killServices remove stopping " + sr);
				}

				boolean hasClients = sr.bindings.size() > 0;
				if (hasClients) {
					for (IntentBindRecord b : sr.bindings.values()) {
						VLog.v(TAG, "Killing binding " + b
								+ ": shouldUnbind=" + b.hasBound);
						b.binder = null;
						b.requested = b.received = b.hasBound = false;
					}
				}

				if (sr.crashCount >= 2 && (sr.serviceInfo.applicationInfo.flags
						& ApplicationInfo.FLAG_PERSISTENT) == 0) {
					VLog.w(TAG, "Service crashed " + sr.crashCount
							+ " times, stopping: " + sr);
					bringDownServiceLocked(sr, true);
				} else if (!allowRestart) {
					bringDownServiceLocked(sr, true);
				} else {
					boolean canceled = scheduleServiceRestartLocked(sr, true);

					// Should the service remain running?  Note that in the
					// extreme case of so many attempts to deliver a command
					// that it failed we also will stop it here.
					if (sr.startRequested && (sr.stopIfKilled || canceled)) {
						if (sr.pendingStarts.size() == 0) {
							sr.startRequested = false;
							if (!hasClients) {
								// Whoops, no reason to restart!
								bringDownServiceLocked(sr, true);
							}
						}
					}
				}
			}
			if (!allowRestart) {
				app.services.clear();
			}
		}

		// Make sure we have no more records on the stopping list.
		int i = mStoppingServices.size();
		while (i > 0) {
			i--;
			ServiceRecord sr = mStoppingServices.get(i);
			if (sr.app == app) {
				mStoppingServices.remove(i);
				Slog.v(TAG, "killServices remove stopping " + sr);
			}
		}
		app.executingServices.clear();
	}

	private ServiceLookupResult retrieveServiceLocked(Intent service,
													  String resolvedType) {
		ServiceRecord r;
		Intent.FilterComparison filter = new Intent.FilterComparison(service);
		r = mServicesByIntent.get(filter);
		if (r == null) {
				ResolveInfo rInfo =
						VPackageManagerService.getService().resolveService(
								service, resolvedType, 0);
				ServiceInfo sInfo =
						rInfo != null ? rInfo.serviceInfo : null;
				if (sInfo == null) {
					VLog.w(TAG, "Unable to start service " + service +
							": not found");
					return null;
				}

				ComponentName name = new ComponentName(
						sInfo.applicationInfo.packageName, sInfo.name);
				r = mServices.get(name);
				if (r == null) {
					filter = new Intent.FilterComparison(service.cloneFilter());
					ServiceRestarter res = new ServiceRestarter();
					r = new ServiceRecord(this, name, filter, sInfo, res);
					res.setService(r);
					mServices.put(name, r);
					mServicesByIntent.put(filter, r);

					// Make sure this component isn't in the pending list.
					int N = mPendingServices.size();
					for (int i=0; i<N; i++) {
						ServiceRecord pr = mPendingServices.get(i);
						if (pr.name.equals(name)) {
							mPendingServices.remove(i);
							i--;
							N--;
						}
					}
				}
		}
		return new ServiceLookupResult(r, null);
	}

	@Override
	public ComponentName startService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		// Refuse possible leaked file descriptors
		if (service != null && service.hasFileDescriptors()) {
			throw new IllegalArgumentException("File descriptors passed in Intent");
		}
		synchronized (this) {
			final long origId = Binder.clearCallingIdentity();
			ComponentName res = startServiceLocked(ApplicationThreadNative.asInterface(caller), service, resolvedType);
			Binder.restoreCallingIdentity(origId);
			return res;
		}
	}

	ComponentName startServiceLocked(IApplicationThread caller,
									 Intent service, String resolvedType) {
		synchronized(this) {
			VLog.v(TAG, "startService: " + service
					+ " type=" + resolvedType + " args=" + service.getExtras());

			ServiceLookupResult res =
					retrieveServiceLocked(service, resolvedType
					);
			if (res == null) {
				return null;
			}
			if (res.record == null) {
				return new ComponentName("!", res.permission != null
						? res.permission : "private to package");
			}
			ServiceRecord r = res.record;
			if (unscheduleServiceRestartLocked(r)) {
				VLog.v(TAG, "START SERVICE WHILE RESTART PENDING: " + r);
			}
			r.startRequested = true;
			r.callStart = false;
			r.pendingStarts.add(new ServiceRecord.StartItem(r, false, r.makeNextStartId(),
					service));
			r.lastActivity = SystemClock.uptimeMillis();
			if (!bringUpServiceLocked(r, service.getFlags(), false)) {
				return new ComponentName("!", "Service process is bad");
			}
			return r.name;
		}
	}

	@Override
	public int stopService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		// Refuse possible leaked file descriptors
		if (service != null && service.hasFileDescriptors()) {
			throw new IllegalArgumentException("File descriptors passed in Intent");
		}

		synchronized(this) {
			VLog.v(TAG, "stopService: " + service
					+ " type=" + resolvedType);

			// If this service is active, make sure it is stopped.
			ServiceLookupResult r = findServiceLocked(service, resolvedType);
			if (r != null) {
				if (r.record != null) {
					final long origId = Binder.clearCallingIdentity();
					try {
						stopServiceLocked(r.record);
					} finally {
						Binder.restoreCallingIdentity(origId);
					}
					return 1;
				}
				return -1;
			}
		}

		return 0;
	}

	private void stopServiceLocked(ServiceRecord service) {
		service.startRequested = false;
		service.callStart = false;
		bringDownServiceLocked(service, false);
	}

	@Override
	public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
		synchronized(this) {
			VLog.v(TAG, "stopServiceToken: " + className
					+ " " + token + " startId=" + startId);
			ServiceRecord r = findServiceLocked(className, token);
			if (r != null) {
				if (startId >= 0) {
					// Asked to only stop if done with all work.  Note that
					// to avoid leaks, we will take this as dropping all
					// start items up to and including this one.
					ServiceRecord.StartItem si = r.findDeliveredStart(startId, false);
					if (si != null) {
						while (r.deliveredStarts.size() > 0) {
							ServiceRecord.StartItem cur = r.deliveredStarts.remove(0);
							if (cur == si) {
								break;
							}
						}
					}

					if (r.getLastStartId() != startId) {
						return false;
					}

					if (r.deliveredStarts.size() > 0) {
						VLog.w(TAG, "stopServiceToken startId " + startId
								+ " is last, but have " + r.deliveredStarts.size()
								+ " remaining args");
					}
				}

				r.startRequested = false;
				r.callStart = false;
				final long origId = Binder.clearCallingIdentity();
				bringDownServiceLocked(r, false);
				Binder.restoreCallingIdentity(origId);
				return true;
			}
		}
		return false;
	}

	@Override
	public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification,
			boolean keepNotification) throws RemoteException {

	}

	@Override
	public int bindService(IBinder caller, IBinder token, @NonNull Intent service, String resolvedType,
						   IServiceConnection connection, int flags) throws RemoteException {
		// Refuse possible leaked file descriptors
		if (service != null && service.hasFileDescriptors()) {
			throw new IllegalArgumentException("File descriptors passed in Intent");
		}

		synchronized(this) {
			VLog.v(TAG, "bindService: " + service
					+ " type=" + resolvedType + " conn=" + connection.asBinder()
					+ " flags=0x" + Integer.toHexString(flags));
			final ProcessRecord callerApp = getRecordForAppLocked(caller);
			if (callerApp == null) {
				throw new SecurityException(
						"Unable to find app for caller " + caller
								+ " (pid=" + Binder.getCallingPid()
								+ ") when binding service " + service);
			}

			int clientLabel = 0;

			ServiceLookupResult res =
					retrieveServiceLocked(service, resolvedType);
			if (res == null) {
				return 0;
			}
			if (res.record == null) {
				return -1;
			}
			ServiceRecord s = res.record;

			final long origId = Binder.clearCallingIdentity();

			if (unscheduleServiceRestartLocked(s)) {
				VLog.v(TAG, "BIND SERVICE WHILE RESTART PENDING: "
						+ s);
			}

			AppBindRecord b = s.retrieveAppBindingLocked(service, callerApp);
			ConnectionRecord c = new ConnectionRecord(b,
					connection, flags, clientLabel, null);

			IBinder binder = connection.asBinder();
			ArrayList<ConnectionRecord> clist = s.connections.get(binder);
			if (clist == null) {
				clist = new ArrayList<ConnectionRecord>();
				s.connections.put(binder, clist);
			}
			clist.add(c);
			b.connections.add(c);
			b.client.connections.add(c);
			clist = mServiceConnections.get(binder);
			if (clist == null) {
				clist = new ArrayList<ConnectionRecord>();
				mServiceConnections.put(binder, clist);
			}
			clist.add(c);

			if ((flags&Context.BIND_AUTO_CREATE) != 0) {
				s.lastActivity = SystemClock.uptimeMillis();
				if (!bringUpServiceLocked(s, service.getFlags(), false)) {
					return 0;
				}
			}

			VLog.v(TAG, "Bind " + s + " with " + b
					+ ": received=" + b.intent.received
					+ " apps=" + b.intent.apps.size()
					+ " doRebind=" + b.intent.doRebind);

			if (s.app != null && b.intent.received) {
				// Service is already running, so we can immediately
				// publish the connection.
				try {
					c.conn.connected(s.name, b.intent.binder);
				} catch (Exception e) {
					Slog.w(TAG, "Failure sending service " + s.shortName
							+ " to connection " + c.conn.asBinder()
							+ " (in " + c.binding.client.processName + ")", e);
				}

				// If this is the first app connected back to this binding,
				// and the service had previously asked to be told when
				// rebound, then do so.
				if (b.intent.apps.size() == 1 && b.intent.doRebind) {
					requestServiceBindingLocked(s, b.intent, true);
				}
			} else if (!b.intent.requested) {
				requestServiceBindingLocked(s, b.intent, false);
			}

			Binder.restoreCallingIdentity(origId);
		}

		return 1;
	}

	@Override
	public boolean unbindService(IServiceConnection connection) throws RemoteException {
		synchronized (this) {
			IBinder binder = connection.asBinder();
			VLog.v(TAG, "unbindService: conn=" + binder);
			ArrayList<ConnectionRecord> clist = mServiceConnections.get(binder);
			if (clist == null) {
				VLog.w(TAG, "Unbind failed: could not find connection for "
						+ connection.asBinder());
				return false;
			}

			final long origId = Binder.clearCallingIdentity();

			while (clist.size() > 0) {
				ConnectionRecord r = clist.get(0);
				removeConnectionLocked(r, null, null);
			}

			Binder.restoreCallingIdentity(origId);
		}

		return true;
	}

	void removeConnectionLocked(
			ConnectionRecord c, ProcessRecord skipApp, ActivityRecord skipAct) {
		IBinder binder = c.conn.asBinder();
		AppBindRecord b = c.binding;
		ServiceRecord s = b.service;
		ArrayList<ConnectionRecord> clist = s.connections.get(binder);
		if (clist != null) {
			clist.remove(c);
			if (clist.size() == 0) {
				s.connections.remove(binder);
			}
		}
		b.connections.remove(c);
		if (b.client != skipApp) {
			b.client.connections.remove(c);
		}
		clist = mServiceConnections.get(binder);
		if (clist != null) {
			clist.remove(c);
			if (clist.size() == 0) {
				mServiceConnections.remove(binder);
			}
		}

		if (b.connections.size() == 0) {
			b.intent.apps.remove(b.client);
		}

		if (!c.serviceDead) {
			VLog.v(TAG, "Disconnecting binding " + b.intent
					+ ": shouldUnbind=" + b.intent.hasBound);
			if (s.app != null && s.app.thread != null && b.intent.apps.size() == 0
					&& b.intent.hasBound) {
				try {
					bumpServiceExecutingLocked(s, "unbind");
					b.intent.hasBound = false;
					// Assume the client doesn't want to know about a rebind;
					// we will deal with that later if it asks for one.
					b.intent.doRebind = false;
					s.app.thread.scheduleUnbindService(s, b.intent.intent.getIntent());
				} catch (Exception e) {
					Slog.w(TAG, "Exception when unbinding service " + s.shortName, e);
					serviceDoneExecutingLocked(s, true);
				}
			}

			if ((c.flags&Context.BIND_AUTO_CREATE) != 0) {
				bringDownServiceLocked(s, false);
			}
		}
	}

	@Override
	public void unbindFinished(IBinder token, Intent intent, boolean doRebind) throws RemoteException {
		// Refuse possible leaked file descriptors
		if (intent != null && intent.hasFileDescriptors()) {
			throw new IllegalArgumentException("File descriptors passed in Intent");
		}

		synchronized(this) {
			if (!(token instanceof ServiceRecord)) {
				throw new IllegalArgumentException("Invalid service token");
			}
			ServiceRecord r = (ServiceRecord)token;

			final long origId = Binder.clearCallingIdentity();

			Intent.FilterComparison filter
                    = new Intent.FilterComparison(intent);
			IntentBindRecord b = r.bindings.get(filter);
			VLog.v(TAG, "unbindFinished in " + r
                    + " at " + b + ": apps="
                    + (b != null ? b.apps.size() : 0));

			boolean inStopping = mStoppingServices.contains(r);
			if (b != null) {
                if (b.apps.size() > 0 && !inStopping) {
                    // Applications have already bound since the last
                    // unbind, so just rebind right here.
                    requestServiceBindingLocked(r, b, true);
                } else {
                    // Note to tell the service the next time there is
                    // a new client.
                    b.doRebind = true;
                }
            }

			serviceDoneExecutingLocked(r, inStopping);

			Binder.restoreCallingIdentity(origId);
		}
	}

	@Override
	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException  {
		synchronized(this) {
			if (!(token instanceof ServiceRecord)) {
				throw new IllegalArgumentException("Invalid service token");
			}
			ServiceRecord r = (ServiceRecord)token;
			boolean inStopping = mStoppingServices.contains(token);
			if (r != token) {
                Slog.w(TAG, "Done executing service " + r.name
                        + " with incorrect token: given " + token
                        + ", expected " + r);
                return;
            }

			if (type == 1) {
                // This is a call from a service start...  take care of
                // book-keeping.
                r.callStart = true;
                switch (res) {
                    case Service.START_STICKY_COMPATIBILITY:
                    case Service.START_STICKY: {
                        // We are done with the associated start arguments.
                        r.findDeliveredStart(startId, true);
                        // Don't stop if killed.
                        r.stopIfKilled = false;
                        break;
                    }
                    case Service.START_NOT_STICKY: {
                        // We are done with the associated start arguments.
                        r.findDeliveredStart(startId, true);
                        if (r.getLastStartId() == startId) {
                            // There is no more work, and this service
                            // doesn't want to hang around if killed.
                            r.stopIfKilled = true;
                        }
                        break;
                    }
                    case Service.START_REDELIVER_INTENT: {
                        // We'll keep this item until they explicitly
                        // call stop for it, but keep track of the fact
                        // that it was delivered.
                        ServiceRecord.StartItem si = r.findDeliveredStart(startId, false);
                        if (si != null) {
                            si.deliveryCount = 0;
                            si.doneExecutingCount++;
                            // Don't stop if killed.
                            r.stopIfKilled = true;
                        }
                        break;
                    }
                    case Service.START_TASK_REMOVED_COMPLETE: {
                        // Special processing for onTaskRemoved().  Don't
                        // impact normal onStartCommand() processing.
                        r.findDeliveredStart(startId, true);
                        break;
                    }
                    default:
                        throw new IllegalArgumentException(
                                "Unknown service start result: " + res);
                }
                if (res == Service.START_STICKY_COMPATIBILITY) {
                    r.callStart = false;
                }
            }

			final long origId = Binder.clearCallingIdentity();
			serviceDoneExecutingLocked(r, inStopping);
			Binder.restoreCallingIdentity(origId);
		}
	}

	@Override
	public IBinder peekService(Intent service, String resolvedType) throws RemoteException {
		// Refuse possible leaked file descriptors
		if (service != null && service.hasFileDescriptors()) {
			throw new IllegalArgumentException("File descriptors passed in Intent");
		}

		IBinder ret = null;

		synchronized(this) {
			ServiceLookupResult r = findServiceLocked(service, resolvedType);

			if (r != null) {
				// r.record is null if findServiceLocked() failed the caller permission check
				if (r.record == null) {
					throw new SecurityException(
							"Permission Denial: Accessing service " + r.record.name
									+ " from pid=" + Binder.getCallingPid()
									+ ", uid=" + Binder.getCallingUid()
									+ " requires " + r.permission);
				}
				IntentBindRecord ib = r.record.bindings.get(r.record.intent);
				if (ib != null) {
					ret = ib.binder;
				}
			}
		}

		return ret;
	}

	@Override
	public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
		// Refuse possible leaked file descriptors
		if (intent != null && intent.hasFileDescriptors()) {
			throw new IllegalArgumentException("File descriptors passed in Intent");
		}

		synchronized(this) {
			if (!(token instanceof ServiceRecord)) {
				throw new IllegalArgumentException("Invalid service token");
			}
			ServiceRecord r = (ServiceRecord)token;

			final long origId = Binder.clearCallingIdentity();

			VLog.v(TAG, "PUBLISHING " + r
					+ " " + intent + ": " + service);
			Intent.FilterComparison filter
                    = new Intent.FilterComparison(intent);
			IntentBindRecord b = r.bindings.get(filter);
			if (b != null && !b.received) {
                b.binder = service;
                b.requested = true;
                b.received = true;
                if (r.connections.size() > 0) {
                    Iterator<ArrayList<ConnectionRecord>> it
                            = r.connections.values().iterator();
                    while (it.hasNext()) {
                        ArrayList<ConnectionRecord> clist = it.next();
                        for (int i=0; i<clist.size(); i++) {
                            ConnectionRecord c = clist.get(i);
                            if (!filter.equals(c.binding.intent.intent)) {
                                VLog.v(
                                        TAG, "Not publishing to: " + c);
                                VLog.v(
                                        TAG, "Bound intent: " + c.binding.intent.intent);
                                VLog.v(
                                        TAG, "Published intent: " + intent);
                                continue;
                            }
                            VLog.v(TAG, "Publishing to: " + c);
                            try {
                                c.conn.connected(r.name, service);
                            } catch (Exception e) {
                                VLog.w(TAG, "Failure sending service " + r.name +
                                        " to connection " + c.conn.asBinder() +
                                        " (in " + c.binding.client.processName + ")", e);
                            }
                        }
                    }
                }
            }

			serviceDoneExecutingLocked(r, mStoppingServices.contains(r));

			Binder.restoreCallingIdentity(origId);
		}
	}
	@Override
	public VParceledListSlice<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) {
		synchronized (this) {
			return new VParceledListSlice<>(getServicesLocked(maxNum));
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
			targetApp = startProcessLocked(ComponentUtils.getProcessName(providerInfo), providerInfo.applicationInfo);
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
			binder.linkToDeath(new IBinder.DeathRecipient() {
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
			VLog.d(TAG, "Attach Client (%s) with %d PendingServices.", app.processName, mPendingServices.size());
			if (mPendingServices.size() > 0) {
				ServiceRecord sr = null;
				try {
					for (int i=0; i<mPendingServices.size(); i++) {
						sr = mPendingServices.get(i);
						if (!app.processName.equals(sr.processName)) {
							continue;
						}
						mPendingServices.remove(i);
						i--;
						realStartServiceLocked(sr, app);
					}
				} catch (Exception e) {
					Slog.w(TAG, "Exception in new application when starting service "
							+ sr.shortName, e);
				}
			}
		}
	}

	private void onProcessDead(ProcessRecord record) {
		VLog.d(TAG, "Process %s died.", record.processName);
		mProcessMap.remove(record.pid);
		processDead(record);
		killServicesLocked(record, false);
		record.lock.open();
	}

	public ProcessRecord startProcessLocked(String processName, ApplicationInfo info) {
		synchronized (this) {
			VLog.d(TAG, "startProcessLocked %s (%s).", processName, info.packageName);
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
		List<ProviderInfo> providers = VPackageManagerService.getService().queryContentProviders(processName, 0)
				.getList();
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
					if (record != mSystemProcessRecord) {
						killProcess(record.pid);
					}
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
		synchronized (mProcessMap) {
			return mProcessMap.get(pid);
		}
	}

	public ProcessRecord getRecordForAppLocked(IBinder app) {
		synchronized (mProcessMap) {
			ProcessRecord r = mProcessMap.get(app);
			if (r != null) {
				return r;
			}
			return mSystemProcessRecord;
		}
	}

	public ProcessRecord getRecordForAppLocked(IApplicationThread app) {
		synchronized (mProcessMap) {
			return mProcessMap.get(app.asBinder());
		}
	}

	public ProcessRecord findProcess(String processName) {
		return mProcessMap.get(processName);
	}

private final class ServiceLookupResult {
		final ServiceRecord record;
		final String permission;

		ServiceLookupResult(ServiceRecord _record, String _permission) {
			record = _record;
			permission = _permission;
		}
	}
}
