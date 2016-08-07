package com.lody.virtual.service.process;

import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.client.service.ProviderCaller;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IProcessManager;
import com.lody.virtual.service.am.StubInfo;
import com.lody.virtual.service.am.VActivityService;
import com.lody.virtual.service.am.VServiceService;
import com.lody.virtual.service.interfaces.IProcessObserver;
import com.lody.virtual.service.pm.VPackageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.os.Process.killProcess;

/**
 * @author Lody
 */

public class VProcessService extends IProcessManager.Stub {

	private static final VProcessService sService = new VProcessService();
	private static final String TAG = VProcessService.class.getSimpleName();
	private final ProcessMap mProcessMap = new ProcessMap();
	private Map<String, ProcessRecord> mPendingProcesses = new HashMap<>();

	public static VProcessService getService() {
		return sService;
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
				// client has died
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
				// client has died
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
						onProcessDied(record);
					}
				}, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			app.client = client;
			app.thread = thread;
			app.pid = callingPid;
			try {
				client.bindApplication(app.processName, app.info, app.sharedPackages, app.providers);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mProcessMap.put(app);
			mPendingProcesses.remove(app.processName);
		}
	}

	private void onProcessDied(ProcessRecord record) {
		VLog.d(TAG, "Process %s has died.", record.processName);
		mProcessMap.remove(record.pid);
		VActivityService.getService().processDied(record);
		VServiceService.getService().processDied(record);
		record.lock.open();
	}

	public ProcessRecord startProcess(String processName, ApplicationInfo info) {
		synchronized (this) {
			ProcessRecord app = mProcessMap.get(processName);
			if (app != null) {
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
			List<String> sharedPackages = VPackageService.getService().querySharedPackages(info.packageName);
			List<ProviderInfo> providers = VPackageService.getService().queryContentProviders(processName, 0).getList();
			app = new ProcessRecord(stubInfo, info, processName, providers, sharedPackages);
			app.pendingPackages.add(info.packageName);
			mPendingProcesses.put(processName, app);
			Bundle extras = new Bundle();
			BundleCompat.putBinder(extras, ExtraConstants.EXTRA_BINDER, app);
			ProviderCaller.call(stubInfo, MethodConstants.INIT_PROCESS, null, extras);
			return app;
		}
	}

	private StubInfo queryFreeStubForProcess(String processName) {
		for (StubInfo stubInfo : VActivityService.getService().getStubs()) {
			if (mProcessMap.get(stubInfo) == null || mPendingProcesses.containsKey(processName)) {
				return stubInfo;
			}
		}
		return null;
	}

	@Override
	public boolean isAppProcess(String processName) {
		if (!TextUtils.isEmpty(processName)) {
			Set<String> processList = VActivityService.getService().getStubProcessList();
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
				return new ArrayList<>(r.pkgList);
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
	public void appDoneExecuting(String packageName) {
		synchronized (mProcessMap) {
			ProcessRecord r = mProcessMap.get(Binder.getCallingPid());
			if (r != null) {
				r.pendingPackages.remove(packageName);
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
