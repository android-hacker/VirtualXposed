package com.lody.virtual.client.local;

import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.env.RuntimeEnv;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.service.IProcessManager;
import com.lody.virtual.service.interfaces.IProcessObserver;

import java.util.List;

/**
 * @author Lody
 *
 */
public class LocalProcessManager {

	private static IProcessManager service;

	private static IProcessManager getService() {
		if (service == null) {
			synchronized (LocalProcessManager.class) {
				if (service == null) {
					IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.PROCESS_MANAGER);
					service = IProcessManager.Stub.asInterface(binder);
				}
			}
		}
		if (service == null) {
			throw new RuntimeException("Unable to attach ProcessManager");
		}
		return service;
	}

	public static void killAllApps() {
		try {
			getService().killAllApps();
		} catch (RemoteException e) {
			// Ignore
		}
	}

	public static void killAppByPkg(String pkg) {
		try {
			getService().killAppByPkg(pkg);
		} catch (RemoteException e) {
			// Ignore
		}
	}

	public static boolean isAppProcess(String processName) {
		try {
			return getService().isAppProcess(processName);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public static void dump() {
		try {
			getService().dump();
		} catch (RemoteException e) {
			// Ignore
		}
	}

	public static void killApplicationProcess(String procName, int uid) {
		try {
			getService().killApplicationProcess(procName, uid);
		} catch (RemoteException e) {
			// Ignore
		}
	}


	public static void onAppProcessCreate(IBinder appThread, String pkg, String processName) {
		try {
			getService().onAppProcessCreate(appThread, pkg, processName);
		} catch (RemoteException e) {
			// Ignore
		}
	}


	public static boolean isAppPid(int pid) {
		try {
			return getService().isAppPid(pid);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public static List<String> getProcessPkgList(int pid) {
		try {
			return getService().getProcessPkgList(pid);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public static String getAppProcessName(int pid) {
		try {
			return getService().getAppProcessName(pid);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public static void registerProcessObserver(IProcessObserver observer) {
		try {
			getService().registerProcessObserver(observer);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void unregisterProcessObserver(IProcessObserver observer) {
		try {
			getService().unregisterProcessObserver(observer);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static String getInitialPackage(int pid){
		try {
			return getService().getInitialPackage(pid);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}
}
