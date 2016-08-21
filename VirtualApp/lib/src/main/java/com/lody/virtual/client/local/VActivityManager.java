package com.lody.virtual.client.local;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.ContentProviderNative;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.proto.PendingIntentData;
import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.service.IActivityManager;
import com.lody.virtual.service.interfaces.IProcessObserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 */
public class VActivityManager {

	private static final VActivityManager sAM = new VActivityManager();

	private IActivityManager service;

	private Map<IBinder, LocalActivityRecord> mActivities = new HashMap<IBinder, LocalActivityRecord>(6);

	public static VActivityManager get() {
		return sAM;
	}

	public IActivityManager getService() {
		if (service == null) {
			service = IActivityManager.Stub
					.asInterface(ServiceManagerNative.getService(ServiceManagerNative.ACTIVITY_MANAGER));
		}
		return service;
	}

	public Intent startActivity(Intent intent, ActivityInfo info, IBinder resultTo, Bundle options, int userId) {
		try {
			return getService().startActivity(intent, info, resultTo, options, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public LocalActivityRecord onActivityCreate(Activity activity) {
		Intent intent = activity.getIntent();
		if (intent == null) {
			return null;
		}
		ActivityInfo targetActInfo = intent.getParcelableExtra("_VA_|_target_activity_");
		ActivityInfo callerActInfo = intent.getParcelableExtra("_VA_|_caller_activity_");

		// NOTE:
		// 此处在使用LocalActivityManager启动Activity的时候是空的,因为走不到replaceIntent里,
		// 比如掌阅会崩溃,暂时从Activity里取,没调研兼容性=_=,先用着。
		if (targetActInfo == null) {
			try {
				targetActInfo = mirror.android.app.Activity.mActivityInfo.get(activity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		IBinder token = activity.getActivityToken();
		LocalActivityRecord r = new LocalActivityRecord();
		r.activityInfo = targetActInfo;
		r.activity = activity;
		r.targetIntent = intent;
		mActivities.put(token, r);
		try {
			getService().onActivityCreated(activity.getActivityToken(), targetActInfo, callerActInfo,
					activity.getTaskId());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return r;
	}

	public LocalActivityRecord getActivityRecord(IBinder token) {
		return token == null ? null : mActivities.get(token);
	}

	public void onActivityResumed(Activity activity) {
		IBinder token = activity.getActivityToken();
		try {
			getService().onActivityResumed(token);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onActivityDestroy(Activity activity) {
		IBinder token = activity.getActivityToken();
		mActivities.remove(token);
		try {
			getService().onActivityDestroyed(token);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public AppTaskInfo getTaskInfo(int taskId) {
		try {
			return getService().getTaskInfo(taskId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ActivityInfo getCallingActivity(IBinder token) {
		try {
			return getService().getCallingActivity(token);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public String getPackageForToken(IBinder token) {
		try {
			return getService().getPackageForToken(token);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ActivityInfo getActivityInfo(IBinder token) {
		try {
			return getService().getActivityInfo(token);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ComponentName startService(IBinder caller, Intent service, String resolvedType) {
		try {
			return getService().startService(caller, service, resolvedType);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public int stopService(IBinder caller, Intent service, String resolvedType) {
		try {
			return getService().stopService(caller, service, resolvedType);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean stopServiceToken(ComponentName className, IBinder token, int startId) {
		try {
			return getService().stopServiceToken(className, token, startId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, boolean keepNotification) {
		try {
			getService().setServiceForeground(className, token, id, notification, keepNotification);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags) {
		try {
			return getService().bindService(caller, token, service, resolvedType, connection, flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean unbindService(IServiceConnection connection) {
		try {
			return getService().unbindService(connection);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void unbindFinished(IBinder token, Intent service, boolean doRebind) {
		try {
			getService().unbindFinished(token, service, doRebind);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
		try {
			getService().serviceDoneExecuting(token, type, startId, res);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public IBinder peekService(Intent service, String resolvedType) {
		try {
			return getService().peekService(service, resolvedType);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void publishService(IBinder token, Intent intent, IBinder service) {
		try {
			getService().publishService(token, intent, service);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public VParceledListSlice getServices(int maxNum, int flags) {
		try {
			return getService().getServices(maxNum, flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}



	public void attachClient(IBinder client) {
		try {
			getService().attachClient(client);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public String getAppProcessName(int pid) {
		try {
			return getService().getAppProcessName(pid);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public String getInitialPackage(int pid) {
		try {
			return getService().getInitialPackage(pid);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean isAppProcess(String processName) {
		try {
			return getService().isAppProcess(processName);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void handleApplicationCrash() {
		try {
			getService().handleApplicationCrash();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void killAllApps() {
		try {
			getService().killAllApps();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void dump() {
		try {
			getService().dump();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void killApplicationProcess(String procName, int uid) {
		try {
			getService().killApplicationProcess(procName, uid);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void registerProcessObserver(IProcessObserver observer) {
		try {
			getService().registerProcessObserver(observer);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void killAppByPkg(String pkg, int userId) {
		try {
			getService().killAppByPkg(pkg, userId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void unregisterProcessObserver(IProcessObserver observer) {
		try {
			getService().unregisterProcessObserver(observer);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void appDoneExecuting() {
		try {
			getService().appDoneExecuting();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public List<String> getProcessPkgList(int pid) {
		try {
			return getService().getProcessPkgList(pid);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean isAppPid(int pid) {
		try {
			return getService().isAppPid(pid);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void ensureAppBound(String processName, String packageName, int userId) {
		try {
			getService().ensureAppBound(processName, packageName, userId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public int getUidByPid(int pid) {
		try {
			return getService().getUidByPid(pid);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public int getSystemPid() {
		try {
			return getService().getSystemPid();
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void sendActivityResult(IBinder resultTo, String resultWho, int requestCode) {
		LocalActivityRecord r = mActivities.get(resultTo);
		if (r != null && r.activity != null) {
			ActivityThread mainThread = VirtualCore.mainThread();
			mainThread.sendActivityResult(resultTo, resultWho, requestCode, 0, null);
		}
	}

	public IContentProvider acquireProviderClient(int userId, ProviderInfo info) throws RemoteException {
		return ContentProviderNative.asInterface(getService().acquireProviderClient(userId, info));
	}

	public PendingIntentData getPendingIntent(IBinder binder) throws RemoteException {
		return getService().getPendingIntent(binder);
	}

	public void addPendingIntent(IBinder binder, String creator) throws RemoteException {
		getService().addPendingIntent(binder, creator);
	}

	public void removePendingIntent(IBinder binder) throws RemoteException {
		getService().removePendingIntent(binder);
	}
}
