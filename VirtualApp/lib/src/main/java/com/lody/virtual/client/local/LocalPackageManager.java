package com.lody.virtual.client.local;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.env.RuntimeEnv;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.proto.ReceiverInfo;
import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.service.IPackageManager;

import java.util.List;

/**
 * @author Lody
 *
 */
public class LocalPackageManager {

	private static final LocalPackageManager sMgr = new LocalPackageManager();
	private IPackageManager mRemote;

	public static LocalPackageManager getInstance() {
		return sMgr;
	}

	public synchronized IPackageManager getInterface() {
		if (mRemote == null) {
			synchronized (LocalPackageManager.class) {
				if (mRemote == null) {
					final IBinder pmBinder = ServiceManagerNative.getService(ServiceManagerNative.PACKAGE_MANAGER);
					mRemote = IPackageManager.Stub.asInterface(pmBinder);
				}
			}
		}
		return mRemote;
	}

	public int checkPermission(String permName, String pkgName) {
		try {
			return getInterface().checkPermission(permName, pkgName);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ResolveInfo resolveService(Intent intent, String resolvedType, int flags) {
		try {
			return getInterface().resolveService(intent, resolvedType, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) {
		try {
			return getInterface().getPermissionGroupInfo(name, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<ApplicationInfo> getInstalledApplications(int flags) {
		try {
			//noinspection unchecked
			return getInterface().getInstalledApplications(flags).getList();
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public PackageInfo getPackageInfo(String packageName, int flags) {
		try {
			return getInterface().getPackageInfo(packageName, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags) {
		try {
			return getInterface().resolveIntent(intent, resolvedType, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) {
		try {
			return getInterface().queryIntentContentProviders(intent, resolvedType, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ActivityInfo getReceiverInfo(ComponentName componentName, int flags) {
		try {
			return getInterface().getReceiverInfo(componentName, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public VParceledListSlice getInstalledPackages(int flags) {
		try {
			return getInterface().getInstalledPackages(flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) {
		try {
			return getInterface().queryPermissionsByGroup(group, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public PermissionInfo getPermissionInfo(String name, int flags) {
		try {
			return getInterface().getPermissionInfo(name, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ActivityInfo getActivityInfo(ComponentName componentName, int flags) {
		try {
			return getInterface().getActivityInfo(componentName, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) {
		try {
			return getInterface().queryIntentReceivers(intent, resolvedType, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
		try {
			return getInterface().getAllPermissionGroups(flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) {
		try {
			return getInterface().queryIntentActivities(intent, resolvedType, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) {
		try {
			return getInterface().queryIntentServices(intent, resolvedType, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ApplicationInfo getApplicationInfo(String packageName, int flags) {
		try {
			return getInterface().getApplicationInfo(packageName, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) {
		try {
			return getInterface().getReceiverIntentFilter(info);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ProviderInfo resolveContentProvider(String name, int flags) {
		try {
			return getInterface().resolveContentProvider(name, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ServiceInfo getServiceInfo(ComponentName componentName, int flags) {
		try {
			return getInterface().getServiceInfo(componentName, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public ProviderInfo getProviderInfo(ComponentName componentName, int flags) {
		try {
			return getInterface().getProviderInfo(componentName, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public boolean activitySupportsIntent(ComponentName component, Intent intent, String resolvedType) {
		try {
			return getInterface().activitySupportsIntent(component, intent, resolvedType);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<ProviderInfo> queryContentProviders(String processName, int flags) {
		try {
			//noinspection unchecked
			return getInterface().queryContentProviders(processName, flags).getList();
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<ReceiverInfo> queryReceivers(String processName, int flags) {
		try {
			//noinspection unchecked
			return getInterface().queryReceivers(processName, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public List<String> querySharedPackages(String packageName) {
		try {
			return getInterface().querySharedPackages(packageName);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}
}
