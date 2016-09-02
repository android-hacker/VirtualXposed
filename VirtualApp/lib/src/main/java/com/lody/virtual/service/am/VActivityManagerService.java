package com.lody.virtual.service.am;

import android.app.ActivityManager;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.Notification;
import android.content.BroadcastReceiver;
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
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.service.ProviderCall;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.compat.IApplicationThreadCompat;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.proto.PendingIntentData;
import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.helper.utils.collection.ArrayMap;
import com.lody.virtual.helper.utils.collection.SparseArray;
import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.service.IActivityManager;
import com.lody.virtual.service.interfaces.IProcessObserver;
import com.lody.virtual.service.pm.VAppManagerService;
import com.lody.virtual.service.pm.VPackageManagerService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import mirror.android.app.ApplicationThreadNative;
import mirror.android.app.IApplicationThreadICSMR1;
import mirror.android.app.IApplicationThreadJBMR1;
import mirror.android.app.IApplicationThreadKitkat;
import mirror.android.content.res.CompatibilityInfo;

import static android.os.Process.killProcess;

/**
 * @author Lody
 *
 */
public class VActivityManagerService extends IActivityManager.Stub {

	/** Type for IActivityManager.serviceDoneExecuting: anonymous operation */
	public static final int SERVICE_DONE_EXECUTING_ANON = 0;
	/** Type for IActivityManager.serviceDoneExecuting: done with an onStart call */
	public static final int SERVICE_DONE_EXECUTING_START = 1;
	/** Type for IActivityManager.serviceDoneExecuting: done stopping (destroying) service */
	public static final int SERVICE_DONE_EXECUTING_STOP = 2;

	private static final boolean BROADCAST_NOT_STARTED_PKG = false;

	private static final AtomicReference<VActivityManagerService> sService = new AtomicReference<>();
	private static final String TAG = VActivityManagerService.class.getSimpleName();
	private final SparseArray<ProcessRecord> mPidsSelfLocked = new SparseArray<ProcessRecord>();
	private final Map<String, StubInfo> stubInfoMap = new ConcurrentHashMap<String, StubInfo>();
	private final Set<String> stubProcessList = new HashSet<String>();
	private final ActivityStack mMainStack = new ActivityStack(this);
	private final List<ServiceRecord> mHistory = new ArrayList<ServiceRecord>();
	private final ProcessMap<ProcessRecord> mProcessNames = new ProcessMap<ProcessRecord>();
	private ActivityManager am = (ActivityManager) VirtualCore.get().getContext()
			.getSystemService(Context.ACTIVITY_SERVICE);
	private ProcessMap<ProcessRecord> mPendingProcesses = new ProcessMap<>();
	private final VPendingIntents mPendingIntents = new VPendingIntents();

	public static VActivityManagerService get() {
		return sService.get();
	}

	public static void systemReady(Context context) {
		new VActivityManagerService().onCreate(context);
	}

	private static ServiceInfo resolveServiceInfo(Intent service, int userId) {
		if (service != null) {
			ServiceInfo serviceInfo = VirtualCore.get().resolveServiceInfo(service, userId);
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
	public int startActivity(Intent intent, ActivityInfo info, IBinder resultTo, Bundle options, int userId) {
		synchronized (this) {
			return mMainStack.startActivityLocked(userId, intent, info, resultTo, options, userId);
		}
	}

	@Override
	public PendingIntentData getPendingIntent(IBinder binder) {
		return mPendingIntents.getPendingIntent(binder);
	}

	@Override
	public void addPendingIntent(IBinder binder, String creator) {
		mPendingIntents.addPendingIntent(binder, creator);
	}

	@Override
	public void removePendingIntent(IBinder binder) {
		mPendingIntents.removePendingIntent(binder);
	}

	@Override
	public int getSystemPid() {
		return VirtualCore.get().myUid();
	}

	@Override
	public void onActivityCreated(ComponentName component, ComponentName caller, IBinder token, Intent intent, String affinity, int taskId, int launchMode, int flags, int clearTargetOrder) {
		int pid = Binder.getCallingPid();
		ProcessRecord targetApp = findProcess(pid);
		if (targetApp != null) {
			mMainStack.onActivityCreated(targetApp, component, caller, token, intent, affinity, taskId, launchMode, flags, clearTargetOrder);
		}
	}

	@Override
	public void onActivityResumed(int userId, IBinder token) {
		mMainStack.onActivityResumed(userId, token);
	}

	@Override
	public boolean onActivityDestroyed(int userId, IBinder token) {
		return mMainStack.onActivityDestroyed(userId, token);
	}

	@Override
	public AppTaskInfo getTaskInfo(int taskId) {

		return null;
	}

	@Override
	public String getPackageForToken(int userId, IBinder token) {
		return mMainStack.getPackageForToken(userId, token);
	}

	private synchronized int getTopTaskId() {
		List<ActivityManager.RunningTaskInfo> taskInfos = am.getRunningTasks(1);
		if (taskInfos.size() > 0) {
			return taskInfos.get(0).id;
		}
		return -1;
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
			mMainStack.processDied(record);
		}
	}

	@Override
	public void ensureAppBound(String processName, String packageName, int userId) {
		int pid = getCallingPid();
		int appId = VAppManagerService.get().getAppId(packageName);
		int uid = VUserHandle.getUid(userId, appId);
		ProcessRecord app = findProcess(pid);
		if (app == null) {
			app = mPendingProcesses.get(processName, appId);
		}
		if (app == null && processName != null) {
			ApplicationInfo appInfo = VPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
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
				performStartProcessLocked(uid, stubInfo, appInfo, processName);
			}
		}
	}

	@Override
	public IBinder acquireProviderClient(int userId, ProviderInfo info) {
		String processName = info.processName;
		ProcessRecord r = startProcessIfNeedLocked(processName, userId, info.packageName);
		if (r != null && r.client != null) {
			try {
				return r.client.acquireProviderClient(info);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String getProcessName(int pid) {
		for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
			if (info.pid == pid) {
				return info.processName;
			}
		}
		return null;
	}

	@Override
	public ComponentName getCallingActivity(int userId, IBinder token) {
		return mMainStack.getCallingActivity(userId, token);
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
		int userId = VUserHandle.getCallingUserId();
		ServiceInfo serviceInfo = resolveServiceInfo(service, userId);
		if (serviceInfo == null) {
			return null;
		}
		ProcessRecord targetApp = startProcessIfNeedLocked(ComponentUtils.getProcessName(serviceInfo),
				userId,
				serviceInfo.packageName);

		if (targetApp == null) {
			VLog.e(TAG, "Unable to start new Process for : " + ComponentUtils.toComponentName(serviceInfo));
			return null;
		}
		if (targetApp.thread == null) {
			targetApp.attachLock.block();
		}
		IInterface appThread = targetApp.thread;
		ServiceRecord r = findRecord(serviceInfo);
		if (r == null) {
			r = new ServiceRecord();
			r.pid = targetApp.pid;
			r.uid = targetApp.uid;
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
			ServiceInfo serviceInfo = resolveServiceInfo(service, VUserHandle.getCallingUserId());
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
		return bindServiceAsUser(caller, token, service, resolvedType, connection, flags,
				VUserHandle.getCallingUserId());
	}

	public int bindServiceAsUser(IBinder caller, IBinder token, Intent service, String resolvedType,
			IServiceConnection connection, int flags, int userId) {
		synchronized (this) {
			ServiceInfo serviceInfo = resolveServiceInfo(service, userId);
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
			ServiceInfo serviceInfo = resolveServiceInfo(service, VUserHandle.getCallingUserId());
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
			if (r != null) {
				r.binder = service;
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
	}

	@Override
	public VParceledListSlice<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) {
		synchronized (mHistory) {
			int userId = VUserHandle.getCallingUserId();
			List<ActivityManager.RunningServiceInfo> services = new ArrayList<>(mHistory.size());
			for (ServiceRecord r : mHistory) {
				if (!(VUserHandle.getUserId(r.uid) == userId)) {
					continue;
				}
				ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
				info.uid = r.uid;
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
	public void attachClient(final IBinder clientBinder) {
		synchronized (this) {
			int callingPid = VBinder.getCallingPid();
			final IVClient client = IVClient.Stub.asInterface(clientBinder);
			if (client == null) {
				killProcess(callingPid);
				return;
			}
			IInterface thread = null;
			try {
				thread = ApplicationThreadNative.asInterface.call(client.getAppThread());
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
			mPendingProcesses.remove(app.processName, app.userId);
			synchronized (mProcessNames) {
				mProcessNames.put(app.processName, app.uid, app);
				mPidsSelfLocked.put(app.pid, app);
			}
			app.attachLock.open();
			try {
				client.bindApplication(app.processName, app.info, app.sharedPackages, app.providers, app.usesLibraries, app.uid);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void onProcessDead(ProcessRecord record) {
		VLog.d(TAG, "Process %s died.", record.processName);
		mProcessNames.remove(record.processName, record.uid);
		mPidsSelfLocked.remove(record.pid);
		processDead(record);
		record.lock.open();
	}

	@Override
	public int getFreeStubCount() {
		return stubInfoMap.size() - mPidsSelfLocked.size();
	}

	public ProcessRecord startProcessIfNeedLocked(String processName, int userId, String packageName) {
		if (VActivityManagerService.get().getFreeStubCount() < 3) {
			// run GC
			VActivityManagerService.get().killAllApps();
		}
		ApplicationInfo info = VPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
		synchronized (this) {
			AppSetting setting = VAppManagerService.get().findAppInfo(info.packageName);
			int uid = VUserHandle.getUid(userId, setting.appId);
			ProcessRecord app = mProcessNames.get(processName, uid);
			if (app != null) {
				if (!app.pkgList.contains(info.packageName)) {
					app.pkgList.add(info.packageName);
				}
				return app;
			}
			app = mPendingProcesses.get(processName, userId);
			if (app != null) {
				return app;
			}
			StubInfo stubInfo = queryFreeStubForProcess(processName, userId);
			if (stubInfo == null) {
				return null;
			}
			app = performStartProcessLocked(uid, stubInfo, info, processName);
			return app;
		}
	}

	private void checkApplicationInfo(ApplicationInfo info) {
		if (info.uid == 0) {
			VLog.e(TAG, "CheckApplicationInfo failed: uid = 0.");
		}
	}

	@Override
	public int getUidByPid(int pid) {
		synchronized (this) {
			ProcessRecord r = findProcess(pid);
			if (r != null) {
				return r.uid;
			}
		}
		return Process.myUid();
	}

	private ProcessRecord performStartProcessLocked(int uid, StubInfo stubInfo, ApplicationInfo info, String processName) {
		VPackageManagerService pm = VPackageManagerService.get();
		List<String> sharedPackages = pm.querySharedPackages(info.packageName);
		List<ProviderInfo> providers = pm.queryContentProviders(processName, 0, 0).getList();
		List<String> usesLibraries = pm.getSharedLibraries(info.packageName);
		ProcessRecord app = new ProcessRecord(stubInfo, info, processName, providers, sharedPackages, usesLibraries, uid);
		mPendingProcesses.put(processName, app.userId, app);
		Bundle extras = new Bundle();
		BundleCompat.putBinder(extras, "_VA_|_binder_", app);
		ProviderCall.call(stubInfo, "_VA_|_init_process_", null, extras);
		return app;
	}

	private StubInfo queryFreeStubForProcess(String processName, int userId) {
		for (StubInfo stubInfo : getStubs()) {
			int N = mPidsSelfLocked.size();
			boolean using = false;
			while (N-- > 0) {
				ProcessRecord r = mPidsSelfLocked.valueAt(N);
				if (r.stubInfo == stubInfo) {
					using = true;
					break;
				}
			}
			if (using || mPendingProcesses.get(processName, userId) != null) {
				continue;
			}
			return stubInfo;
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
		synchronized (mPidsSelfLocked) {
			ProcessRecord r = mPidsSelfLocked.get(pid);
			if (r != null) {
				return r.processName;
			}
		}
		return null;
	}

	@Override
	public List<String> getProcessPkgList(int pid) {
		synchronized (mPidsSelfLocked) {
			ProcessRecord r = mPidsSelfLocked.get(pid);
			if (r != null) {
				return new ArrayList<String>(r.pkgList);
			}
		}
		return null;
	}

	@Override
	public void killAllApps() {
		synchronized (mPidsSelfLocked) {
			for (int i = 0; i < mPidsSelfLocked.size(); i++) {
				ProcessRecord r = mPidsSelfLocked.valueAt(i);
				killProcess(r.pid);
			}
		}
	}

	@Override
	public void killAppByPkg(final String pkg, int userId) {
		synchronized (mProcessNames) {
			ArrayMap<String, SparseArray<ProcessRecord>> map = mProcessNames.getMap();
			int N = map.size();
			while (N-- > 0) {
				SparseArray<ProcessRecord> uids = map.valueAt(N);
				for (int i = 0; i < uids.size(); i++) {
					ProcessRecord r = uids.valueAt(i);
					if (userId != VUserHandle.USER_ALL) {
						if (!(VUserHandle.getUserId(userId) == userId)) {
							continue;
						}
					}
					if (r.pkgList.contains(pkg)) {
						killProcess(r.pid);
					}
				}
			}
		}
	}

	@Override
	public void killApplicationProcess(final String processName, int uid) {
		synchronized (mProcessNames) {
			ProcessRecord r = mProcessNames.get(processName, uid);
			if (r != null) {
				killProcess(r.pid);
			}
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
		synchronized (mPidsSelfLocked) {
			ProcessRecord r = mPidsSelfLocked.get(pid);
			if (r != null) {
				return r.info.packageName;
			}
			return null;
		}
	}

	@Override
	public void handleApplicationCrash() {
		// Nothing
	}

	@Override
	public void appDoneExecuting() {
		synchronized (mPidsSelfLocked) {
			ProcessRecord r = mPidsSelfLocked.get(VBinder.getCallingPid());
			if (r != null) {
				r.doneExecuting = true;
				r.lock.open();
			}
		}
	}

	public ProcessRecord findProcess(int pid) {
		return mPidsSelfLocked.get(pid);
	}

	public ProcessRecord findProcess(String processName, int uid) {
		return mProcessNames.get(processName, uid);
	}

	public int stopUser(int userHandle, IStopUserCallback.Stub stub) {
		synchronized (mPidsSelfLocked) {
			int N = mPidsSelfLocked.size();
			while (N-- > 0) {
				ProcessRecord r = mPidsSelfLocked.valueAt(N);
				if (r.uid == userHandle) {
					killProcess(r.pid);
				}
			}
		}
		try {
			stub.userStopped(userHandle);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void sendOrderedBroadcastAsUser(Intent intent, VUserHandle user, String receiverPermission,
			BroadcastReceiver resultReceiver, Handler scheduler, int initialCode,
			String initialData, Bundle initialExtras) {
		Context context = VirtualCore.get().getContext();
		intent.putExtra("_VA_|_user_id_", user.getIdentifier());
		// TODO: checkPermission
		context.sendOrderedBroadcast(intent, null/* permission */, resultReceiver, scheduler, initialCode, initialData,
				initialExtras);
	}

	public void sendBroadcastAsUser(Intent intent, VUserHandle user) {
		Context context = VirtualCore.get().getContext();
		intent.putExtra("_VA_|_user_id_", user.getIdentifier());
		context.sendBroadcast(intent);
	}

	public void sendBroadcastAsUser(Intent intent, VUserHandle user, String permission) {
		Context context = VirtualCore.get().getContext();
		intent.putExtra("_VA_|_user_id_", user.getIdentifier());
		// TODO: checkPermission
		context.sendBroadcast(intent);
	}

	public boolean handleStaticBroadcast(int appId, ActivityInfo info, Intent intent, BroadcastReceiver receiver,
			BroadcastReceiver.PendingResult result) {
		// Maybe send from System
		int userId = intent.getIntExtra("_VA_|_user_id_", VUserHandle.USER_ALL);
		ComponentName component = intent.getParcelableExtra("_VA_|_component_");
		Intent realIntent = intent.getParcelableExtra("_VA_|_intent_");
		if (component != null) {
			if (!ComponentUtils.toComponentName(info).equals(component)) {
				return false;
			}
		}
		if (realIntent == null) {
			realIntent = intent;
		}
		String originAction = SpecialComponentList.restoreAction(realIntent.getAction());
		if (originAction != null) {
			realIntent.setAction(originAction);
		}
		if (userId >= 0) {
			int uid = VUserHandle.getUid(userId, appId);
			handleStaticBroadcastAsUser(uid, info, realIntent, receiver, result);
		} else if (userId == VUserHandle.USER_ALL) {
			List<UserInfo> userList = VUserManager.get().getUsers(false);
			for (UserInfo userInfo : userList) {
				int uid = VUserHandle.getUid(userInfo.id, appId);
				handleStaticBroadcastAsUser(uid, info, realIntent, receiver, result);
			}
		} else {
			VLog.w(TAG, "Unknown User for receive the broadcast : #%d.", userId);
			return false;
		}
		return true;
	}


	public void handleStaticBroadcastAsUser(int uid, ActivityInfo info, Intent intent, BroadcastReceiver receiver,
			BroadcastReceiver.PendingResult result) {
		synchronized (this) {
			ProcessRecord r = findProcess(info.processName, uid);
			if (BROADCAST_NOT_STARTED_PKG && r == null) {
				r = startProcessIfNeedLocked(info.processName, VUserHandle.getUserId(uid), info.packageName);
			}
			if (r != null && r.thread != null) {
				handleBroadcastIntent(r.thread, VUserHandle.getUserId(uid), info, intent, receiver.isOrderedBroadcast(),
						result);
			}
		}
	}

	private void handleBroadcastIntent(IInterface thread, int sendingUser, ActivityInfo info, Intent intent,
									   boolean sync, BroadcastReceiver.PendingResult result) {
		ComponentName componentName = ComponentUtils.toComponentName(info);
		if (intent.getComponent() != null && !componentName.equals(intent.getComponent())) {
			return;
		}
		if (intent.getComponent() == null) {
			intent.setComponent(componentName);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			IApplicationThreadKitkat.scheduleReceiver.call(thread, intent, info,
					CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO.get(), result.getResultCode(), result.getResultData(),
					result.getResultExtras(false), sync, sendingUser, 0);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			IApplicationThreadJBMR1.scheduleReceiver.call(thread, intent, info,
					CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO.get(), result.getResultCode(), result.getResultData(),
					result.getResultExtras(false), sync, sendingUser);
		} else {
			IApplicationThreadICSMR1.scheduleReceiver.call(thread, intent, info,
					CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO.get(), result.getResultCode(), result.getResultData(),
					result.getResultExtras(false), sync);
		}
	}
}
