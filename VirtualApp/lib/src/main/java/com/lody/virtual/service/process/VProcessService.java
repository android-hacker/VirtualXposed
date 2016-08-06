package com.lody.virtual.service.process;

import android.annotation.NonNull;
import android.app.ActivityManagerNative;
import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.content.pm.ComponentInfo;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.service.ProviderCaller;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IProcessManager;
import com.lody.virtual.service.am.StubInfo;
import com.lody.virtual.service.am.VActivityService;
import com.lody.virtual.service.am.VServiceService;
import com.lody.virtual.service.interfaces.IProcessObserver;
import com.lody.virtual.service.pm.VAppService;

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
	private static final VProcessService sMgr = new VProcessService();
	private final ProcessList mProcessList = new ProcessList();
	private final RunningAppList mRunningAppList = new RunningAppList();
	private RemoteCallbackList<IProcessObserver> mObserverList = new RemoteCallbackList<>();

	public static VProcessService getService() {
		return sMgr;
	}

	/**
	 * 根据插件进程的Pid查找运行在该进程的所有插件包名
	 *
	 * @param pid
	 *            插件Pid
	 */
	public String[] findRunningAppPkgByPid(int pid) {
		synchronized (mProcessList) {
			ProcessRecord record = mProcessList.findProcess(pid);
			if (record != null) {
				return record.runningAppPkgs.toArray(new String[record.runningAppPkgs.size()]);
			}
		}
		return null;
	}

	@Override
	public void killApplicationProcess(String procName, int uid) {
		synchronized (mProcessList) {
			boolean killed = false;
			for (ProcessRecord r : mProcessList.values()) {
				if (TextUtils.equals(procName, r.processName)) {
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
	}

	@Override
	public boolean isAppPid(int pid) throws RemoteException {
		return mProcessList.containPid(pid);
	}

	@Override
	public String getAppProcessName(int pid) throws RemoteException {
		synchronized (mProcessList) {
			ProcessRecord r = mProcessList.findProcess(pid);
			if (r == null) {
				return null;
			}
			return r.processName;
		}
	}

	@Override
	public List<String> getProcessPkgList(int pid) throws RemoteException {
		synchronized (mProcessList) {
			ProcessRecord r = mProcessList.findProcess(pid);
			if (r == null) {
				return null;
			}
			return new ArrayList<String>(r.runningAppPkgs);
		}
	}

	/**
	 * 判断指定进程是否为插件进程
	 *
	 * @param processName
	 *            进程名
	 */
	@Override
	public synchronized boolean isAppProcess(String processName) {
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
		synchronized (mProcessList) {
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
	 * @param appProcessName
	 *            进程名
	 */
	public boolean isSameProcess(int pid, String appProcessName) {
		synchronized (mProcessList) {
			ProcessRecord r = mProcessList.findProcess(pid);
			return r != null && r.processName.equals(appProcessName);
		}
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
			synchronized (mProcessList) {
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
			synchronized (mProcessList) {
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

	private ProcessRecord removeProcessRecordLocked(int pid) {
		synchronized (mProcessList) {
			ProcessRecord r = mProcessList.findProcess(pid);
			if (r == null) {
				return null;
			}
			for (String pkg : r.runningAppPkgs) {
				RunningAppRecord app = mRunningAppList.getRecord(pkg);
				if (app == null) {
					continue;
				}
				int count = mObserverList.beginBroadcast();
				for (int N = 0; N < count; N++) {
					try {
						mObserverList.getBroadcastItem(N).onProcessDied(pkg, r.processName);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				mObserverList.finishBroadcast();
				if (app.isRunningOnPid(r.pid)) {
					app.removePid(r.pid);
				}
				if (app.runningPids.isEmpty()) {
					mRunningAppList.pluginStopped(app.pkgName);
				}
			}
			mProcessList.removeRecord(pid);
			return r;
		}
	}

	private void linkClientBinderDied(final int pid, final IBinder cb) {
		try {
			cb.linkToDeath(new DeathRecipient() {
				@Override
				public void binderDied() {
					synchronized (mProcessList) {
						ProcessRecord r = removeProcessRecordLocked(pid);
						if (r != null) {
							VActivityService.getService().processDied(r);
							VServiceService.getService().processDied(r);
						}
						cb.unlinkToDeath(this, 0);
					}
				}
			}, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAppProcessCreate(IBinder clientBinder, String pkg, String processName) {
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
		ProcessRecord r = mProcessList.findProcess(callingPid);
		if (r == null) {
            r = new ProcessRecord(callingPid, uid);
            r.updateStubProcess(callingPid);
            if (r.stubProcessName == null || r.stubInfo == null) {
                VLog.e(TAG, "Unable to find stubInfo from target AppProcess(%d).", callingPid);
                killProcess(r);
                return;
            }
            r.client = client;
            r.processName = processName;
            onEnterApp(pkg, r);
            r.appThread = appThread;
            r.stubInfo.verify();
            mProcessList.addProcess(callingPid, r);
        } else {
            VLog.w(TAG, "Pid %d has been bound to PMS, should not be bound again, ignored.", callingPid);
        }
	}


	public void onEnterApp(String pkgName, ProcessRecord r) {
		int count = mObserverList.beginBroadcast();
		for (int N = 0; N < count; N++) {
            try {
                mObserverList.getBroadcastItem(N).onProcessCreated(pkgName, r.processName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
		mObserverList.finishBroadcast();
		r.addPackage(pkgName);
		RunningAppRecord runningAppRecord = mRunningAppList.getRecord(pkgName);
		if (runningAppRecord == null) {
            runningAppRecord = new RunningAppRecord(pkgName);
            runningAppRecord.addPid(Binder.getCallingPid());
            mRunningAppList.appStarted(pkgName, runningAppRecord);
        }

	}

	public void registerProcessObserver(final IProcessObserver observer) {
		try {
			final IBinder binder = observer.asBinder();
			binder.linkToDeath(new DeathRecipient() {
				@Override
				public void binderDied() {
					mObserverList.unregister(observer);
					binder.unlinkToDeath(this, 0);
				}
			}, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		mObserverList.register(observer);
	}

	public void unregisterProcessObserver(IProcessObserver observer) {
		mObserverList.unregister(observer);
	}


	public StubInfo findStubInfo(String appProcessName) {
		synchronized (mProcessList) {
			ProcessRecord r = findProcess(appProcessName);
			if (r != null) {
				return r.stubInfo;
			}
			return null;
		}
	}

	public ProcessRecord findProcess(String appProcessName) {
		synchronized (mProcessList) {
			for (ProcessRecord r : mProcessList.values()) {
				if (TextUtils.equals(appProcessName, r.processName)) {
					return r;
				}
			}
		}
		return null;
	}

	public ProcessRecord startProcessLocked(@NonNull ComponentInfo componentInfo) {
		String pkg = componentInfo.packageName;
		String appProcessName = ComponentUtils.getProcessName(componentInfo);
		if (!VAppService.getService().isAppInstalled(pkg)) {
            return null;
        }
		FetchResult res = fetchStub(appProcessName);
		if (res.stubInfo == null) {
            VLog.e(TAG, "Unable to fetch free Stub for launch Process: %s(%s).", pkg, appProcessName);
            return null;
        }
		if (res.isNewStub) {
            performStartProcessLocked(componentInfo.packageName, appProcessName, res.stubInfo.providerInfo);
        }
		return findProcess(appProcessName);
	}

	public FetchResult fetchStub(String appProcessName) {
		boolean isNew = false;
		StubInfo stubInfo = findStubInfo(appProcessName);
		if (stubInfo == null) {
			// The target process is not running,
			// we need to find a free stub to hold the target process.
			isNew = true;
			stubInfo = fetchFreeStubInfo(VActivityService.getService().getStubs());
		}
		return new FetchResult(isNew, stubInfo);
	}

	public ProcessRecord getCallingProcessRecord() {
		return findProcess(Binder.getCallingPid());
	}

	public class FetchResult {
		public boolean isNewStub;
		public StubInfo stubInfo;

		public FetchResult(boolean isNewStub, StubInfo stubInfo) {
			this.isNewStub = isNewStub;
			this.stubInfo = stubInfo;
		}
	}

	public void performStartProcessLocked(String pkg, String processName, ProviderInfo providerInfo) {
		new ProviderCaller.Builder(VirtualCore.getCore().getContext(), providerInfo.authority)
                .methodName(MethodConstants.INIT_PROCESS)
                .addArg(ExtraConstants.EXTRA_PKG, pkg)
                .addArg(ExtraConstants.EXTRA_PROCESS_NAME, processName)
                .call();
	}

	public ProcessRecord findStubProcessRecord(String processName) {
		synchronized (mProcessList) {
			for (ProcessRecord r : mProcessList.values()) {
				if (TextUtils.equals(r.stubProcessName, processName)) {
					return r;
				}
			}
		}
		return null;
	}

	private boolean isStubProcessRunning(StubInfo stubInfo) {
		return findStubProcessRecord(stubInfo.processName) != null;
	}

	public StubInfo fetchFreeStubInfo(Collection<StubInfo> stubInfos) {
		for (StubInfo stubInfo : stubInfos) {
			if (!isStubProcessRunning(stubInfo)) {
				return stubInfo;
			}
		}
		return null;
	}

	public ProcessRecord findProcess(int pid) {
		return mProcessList.findProcess(pid);
	}

}
