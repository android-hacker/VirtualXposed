package com.lody.virtual.client.ipc;

import android.content.ComponentName;
import android.content.Intent;
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

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.proto.ReceiverInfo;
import com.lody.virtual.service.IPackageManager;

import java.util.List;

/**
 * @author Lody
 *
 */
public class VPackageManager {

	private static final VPackageManager sMgr = new VPackageManager();
	private IPackageManager mRemote;

	public static VPackageManager get() {
		return sMgr;
	}

	public synchronized IPackageManager getInterface() {
		if (mRemote == null) {
			synchronized (VPackageManager.class) {
				if (mRemote == null) {
					final IBinder pmBinder = ServiceManagerNative.getService(ServiceManagerNative.PACKAGE);
					Object remote = IPackageManager.Stub.asInterface(pmBinder);
					mRemote = LocalProxyUtils.genProxy(IPackageManager.class, remote);
				}
			}
		}
		return mRemote;
	}

	public int checkPermission(String permName, String pkgName, int userId) {
		try {
			return getInterface().checkPermission(permName, pkgName, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) {
		try {
			return getInterface().resolveService(intent, resolvedType, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) {
		try {
			return getInterface().getPermissionGroupInfo(name, flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
		try {
			// noinspection unchecked
			return getInterface().getInstalledApplications(flags, userId).getList();
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
		try {
			return getInterface().getPackageInfo(packageName, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
		try {
			return getInterface().resolveIntent(intent, resolvedType, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) {
		try {
			return getInterface().queryIntentContentProviders(intent, resolvedType, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ActivityInfo getReceiverInfo(ComponentName componentName, int flags, int userId) {
		try {
			return getInterface().getReceiverInfo(componentName, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<PackageInfo> getInstalledPackages(int flags, int userId) {
		try {
			return getInterface().getInstalledPackages(flags, userId).getList();
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) {
		try {
			return getInterface().queryPermissionsByGroup(group, flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public PermissionInfo getPermissionInfo(String name, int flags) {
		try {
			return getInterface().getPermissionInfo(name, flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ActivityInfo getActivityInfo(ComponentName componentName, int flags, int userId) {
		try {
			return getInterface().getActivityInfo(componentName, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) {
		try {
			return getInterface().queryIntentReceivers(intent, resolvedType, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
		try {
			return getInterface().getAllPermissionGroups(flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
		try {
			return getInterface().queryIntentActivities(intent, resolvedType, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId) {
		try {
			return getInterface().queryIntentServices(intent, resolvedType, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
		try {
			return getInterface().getApplicationInfo(packageName, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
		try {
			return getInterface().resolveContentProvider(name, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ServiceInfo getServiceInfo(ComponentName componentName, int flags, int userId) {
		try {
			return getInterface().getServiceInfo(componentName, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public ProviderInfo getProviderInfo(ComponentName componentName, int flags, int userId) {
		try {
			return getInterface().getProviderInfo(componentName, flags, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean activitySupportsIntent(ComponentName component, Intent intent, String resolvedType) {
		try {
			return getInterface().activitySupportsIntent(component, intent, resolvedType);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
		try {
			// noinspection unchecked
			return getInterface().queryContentProviders(processName, uid, flags).getList();
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<ReceiverInfo> queryReceivers(String processName, int uid, int flags) {
		try {
			// noinspection unchecked
			return getInterface().queryReceivers(processName, uid, flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public List<String> querySharedPackages(String packageName) {
		try {
			return getInterface().querySharedPackages(packageName);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public String[] getPackagesForUid(int uid) {
		try {
			return getInterface().getPackagesForUid(uid);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public int getPackageUid(String packageName, int userId) {
		try {
			return getInterface().getPackageUid(packageName, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

}
