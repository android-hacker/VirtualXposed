package com.lody.virtual.service;

import android.app.ActivityManagerNative;
import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.pm.ComponentInfo;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.service.ProviderCaller;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.proto.VComponentInfo;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.XLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Lody
 *
 *         <p/>
 *         维护和管理所有的插件进程，支持如下特征： 1、在插件进程创建后第一时间与PMS连接，在插件进程死亡时能够立刻知晓并采取相应措施。
 *         2、在进程剩余不多时自动杀死优先级最低的进程。
 */
public class VProcessService extends IProcessManager.Stub {

	private static final String TAG = VProcessService.class.getSimpleName();

	private final char[] mProcessLock = new char[0];

	private final ProcessList mProcessList = new ProcessList();

	private final RunningAppList mRunningAppList = new RunningAppList();

	public static VProcessService getService() {
		return AppProcessManagerHolder.sMgr;
	}

	/**
	 * 根据插件进程的Pid查找运行在该进程的所有插件包名
	 *
	 * @param pid
	 *            插件Pid
	 */
	public String[] findRunningAppPkgByPid(int pid) {
		ProcessRecord record = mProcessList.getRecord(pid);
		if (record != null) {
			return record.runningAppPkgs.toArray(new String[record.runningAppPkgs.size()]);
		}
		return null;
	}

	@Override
	public void killApplicationProcess(String procName, int uid) {
		boolean killed = false;
		for (ProcessRecord r : mProcessList.values()) {
			if (TextUtils.equals(procName, r.appProcessName)) {
				killProcess(r);
				killed = true;
				break;
			}
		}
		if (!killed) {
			try {
				ActivityManagerNative.getDefault().killApplicationProcess(procName, Process.myUid());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 映射Stub进程名与插件进程名
	 *
	 * @param stubProcessName
	 *            Stub进程名
	 * @param pluginProcessName
	 *            插件进程名
	 */
	@Override
	public void mapProcessName(String stubProcessName, String pluginProcessName) {
		mProcessList.map(stubProcessName, pluginProcessName);
	}

	/**
	 * 根据Stub进程名取得插件进程名
	 *
	 * @param stubProcessName
	 *            Stub进程名
	 */
	@Override
	public String getMapAppProcessName(String stubProcessName) {
		return mProcessList.getAppProcName(stubProcessName);
	}

	@Override
	public boolean isAppPID(int pid) throws RemoteException {
		return mProcessList.containPid(pid);
	}

	@Override
	public String getAppProcessName(int pid) throws RemoteException {
		ProcessRecord r = mProcessList.getRecord(pid);
		if (r == null) {
			return null;
		}
		return r.appProcessName;
	}

	@Override
	public List<String> getProcessPkgList(int pid) throws RemoteException {
		ProcessRecord r = mProcessList.getRecord(pid);
		if (r == null) {
			return null;
		}
		return new ArrayList<String>(r.runningAppPkgs);
	}

	/**
	 * 判断指定进程是否为插件进程
	 *
	 * @param processName
	 *            进程名
	 */
	@Override
	public boolean isAppProcess(String processName) {
		if (!TextUtils.isEmpty(processName)) {
			Set<String> processList = VActivityService.getService().getStubProcessList();
			return processList.contains(processName);
		}
		return false;
	}

	/**
	 * DUMP PROC
	 */
	@Override
	public void dump() {

	}

	/**
	 * 杀掉所有的插件进程
	 */
	@Override
	public void killAllApps() {
		synchronized (mProcessLock) {
			for (ProcessRecord r : mProcessList.values()) {
				killProcess(r);
			}
		}
	}

	/**
	 * 判断指定Pid是否与指定进程名为同一个进程
	 *
	 * @param pid
	 *            Pid
	 * @param pluginProcessName
	 *            进程名
	 */
	public boolean isSameProcess(int pid, String pluginProcessName) {
		ProcessRecord r = mProcessList.getRecord(pid);
		return r != null && r.appProcessName.equals(pluginProcessName);
	}

	/**
	 * 杀掉所有正在运行指定包名apk的进程
	 *
	 * @param pkgName
	 *            包名
	 */
	@Override
	public void killAppByPkg(String pkgName) {
		if (!TextUtils.isEmpty(pkgName)) {
			synchronized (mProcessLock) {
				for (ProcessRecord r : mProcessList.values()) {
					if (r.isRunning(pkgName)) {
						killProcess(r);
					}
				}
				mRunningAppList.pluginStopped(pkgName);
			}
		}
	}

	/**
	 * 杀掉指定插件进程
	 *
	 * @param r
	 *            record
	 */
	private void killProcess(ProcessRecord r) {
		if (r != null) {
			synchronized (mProcessLock) {
				if (r.pid != 0) {
					tryKillProcess(r.pid);
				}
			}
		}
	}

	private void tryKillProcess(int pid) {
		try {
			Process.killProcess(pid);
		} catch (Throwable e) {
			// Maybe produce exception
		}
	}

	/**
	 * 移除指定的插件进程
	 *
	 * @param pid
	 *            插件Pid
	 */
	private void removeProcessRecordLocked(int pid) {
		ProcessRecord r = mProcessList.getRecord(pid);
		if (r.runningAppPkgs != null) {
			for (String pkg : r.runningAppPkgs) {
				RunningAppRecord plugin = mRunningAppList.getRecord(pkg);
				if (plugin == null) {
					continue;
				}
				if (plugin.isRunningOnPid(r.pid)) {
					plugin.removePid(r.pid);
				}
				if (plugin.runningPids.isEmpty()) {
					mRunningAppList.pluginStopped(plugin.pkgName);
				}
			}
		}
		mProcessList.removeRecord(pid);
	}

	private void linkClientBinderDied(final int pid, final IBinder cb) {
		try {
			cb.linkToDeath(new DeathRecipient() {
				@Override
				public void binderDied() {
					synchronized (mProcessLock) {
						VActivityService.getService().processDied(pid);
						VServiceService.getService().processDied(pid);
						removeProcessRecordLocked(pid);
						cb.unlinkToDeath(this, 0);
					}
				}
			}, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAppProcessCreate(IBinder clientBinder) {
		final int callingPid = Binder.getCallingPid();
		int uid = Binder.getCallingUid();
		IVClient client = IVClient.Stub.asInterface(clientBinder);
		if (client == null) {
			Process.killProcess(callingPid);
			return;
		}
		IApplicationThread appThread = null;
		try {
			appThread = ApplicationThreadNative.asInterface(client.getAppThread());
		} catch (RemoteException e) {
			// Ignore
		}
		if (appThread == null) {
			Process.killProcess(callingPid);
			return;
		}
		linkClientBinderDied(callingPid, clientBinder);
		synchronized (mProcessLock) {
			ProcessRecord record = mProcessList.getRecord(callingPid);
			if (record == null) {
				ProcessRecord r = newProcessRecordLocked(callingPid, uid);
				r.updateStubProc(callingPid);
				if (r.stubProcessName == null || r.stubInfo == null) {
					XLog.e(TAG, "Unable to find stubInfo from target AppProcess(%d).", callingPid);
					killProcess(r);
					return;
				}
				r.client = client;
				r.appThread = appThread;
				r.stubInfo.verify();
				r.appProcessName = mProcessList.getAppProcName(r.stubProcessName);
				mProcessList.addRecord(callingPid, r);
			} else {
				XLog.w(TAG, "Pid %d have been bound to PMS, should not be bound again, ignore.", callingPid);
			}
		}
	}

	@Override
	public void onEnterApp(String pkgName) {
		int pid = Binder.getCallingPid();
		if (!TextUtils.isEmpty(pkgName)) {
			ProcessRecord r = mProcessList.getRecord(pid);
			if (r == null) {
				XLog.w(TAG, "Enter plugin(%d/%s) but not found in record.", pid, pkgName);
				return;
			}
			r.addPkg(pkgName);
			RunningAppRecord runningAppRecord = mRunningAppList.getRecord(pkgName);
			if (runningAppRecord == null) {
				runningAppRecord = new RunningAppRecord(pkgName);
				runningAppRecord.addPid(pid);
				mRunningAppList.pluginStarted(pkgName, runningAppRecord);
			}

		}
	}

	public void onEnterAppProcessName(String pluginProcessName) {
		int pid = Binder.getCallingPid();
		ProcessRecord r = mProcessList.getRecord(pid);
		if (r != null) {
			r.appProcessName = pluginProcessName;
		}
	}

	private ProcessRecord newProcessRecordLocked(int pid, int uid) {
		ProcessRecord processRecord = new ProcessRecord();
		processRecord.pid = pid;
		processRecord.uid = uid;
		return processRecord;
	}

	public VActivityService.StubInfo findStubInfo(String appProcessName) {
		ProcessRecord r = findRecord(appProcessName);
		if (r != null) {
			return r.stubInfo;
		}
		return null;
	}

	public ProcessRecord findRecord(String appProcessName) {
		for (ProcessRecord r : mProcessList.values()) {
			if (TextUtils.equals(appProcessName, r.appProcessName)) {
				return r;
			}
		}
		return null;
	}

	public void launchComponentProcess(ComponentInfo componentInfo, VActivityService.StubInfo stubInfo) {
		if (componentInfo != null && stubInfo != null) {
			VProcessService.getService().mapProcessName(stubInfo.processName, componentInfo.processName);
			ProviderInfo env = stubInfo.providerInfos.get(0);
			new ProviderCaller.Builder(VirtualCore.getCore().getContext(), env.authority)
					.methodName(MethodConstants.INIT_PROCESS)
					.addArg(ExtraConstants.EXTRA_PKG, componentInfo.packageName)
					.call();
		}
	}

	public void installComponent(VComponentInfo componentInfo) {
		String pkg = componentInfo.packageName;
		String plugProcName = ComponentUtils.getProcessName(componentInfo);

		if (VAppService.getService().isAppInstalled(pkg)) {
			VActivityService.StubInfo stubInfo = findStubInfo(plugProcName);
			if (stubInfo == null) {
				stubInfo = fetchFreeStubInfo(VActivityService.getService().getStubInfoMap().values());
				if (stubInfo != null) {
					launchComponentProcess(componentInfo, stubInfo);
				} else {
					XLog.e(TAG, "Unable to fetch free Stub to launch Process(%s/%s).", pkg, plugProcName);
				}
			}
		} else {
			XLog.e(TAG, "Install Component failed, plugin %s not installed?", pkg);
		}
	}

	public ProcessRecord findStubProcessRecord(VActivityService.StubInfo stubInfo) {
		if (stubInfo != null) {
			String processName = stubInfo.processName;
			for (ProcessRecord r : mProcessList.values()) {
				if (TextUtils.equals(r.stubProcessName, processName)) {
					return r;
				}
			}
		}
		return null;
	}

	public boolean isStubProcessRunning(VActivityService.StubInfo stubInfo) {
		return findStubProcessRecord(stubInfo) != null;
	}

	public VActivityService.StubInfo fetchFreeStubInfo(Collection<VActivityService.StubInfo> stubInfos) {
		for (VActivityService.StubInfo stubInfo : stubInfos) {
			if (!isStubProcessRunning(stubInfo)) {
				return stubInfo;
			}
		}
		return null;
	}

	public ServiceRecord queryServiceRecord(IBinder token) {
		return mProcessList.queryServiceRecord(token);
	}

	public ServiceRecord findServiceRecord(IServiceConnection connection) {
		return mProcessList.queryServiceRecord(connection);
	}

	public ProcessRecord findRecord(int pid) {
		return mProcessList.getRecord(pid);
	}

	private static final class AppProcessManagerHolder {
		private static final VProcessService sMgr = new VProcessService();
	}

}
