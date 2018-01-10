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
import android.os.RemoteException;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.ipcbus.IPCSingleton;
import com.lody.virtual.server.IPackageInstaller;
import com.lody.virtual.server.interfaces.IPackageManager;

import java.util.List;

/**
 * @author Lody
 */
public class VPackageManager {

    private static final VPackageManager sMgr = new VPackageManager();
    private IPCSingleton<IPackageManager> singleton = new IPCSingleton<>(IPackageManager.class);

    public static VPackageManager get() {
        return sMgr;
    }

    public IPackageManager getService() {
        return singleton.get();
    }

    public int checkPermission(String permName, String pkgName, int userId) {
        try {
            return getService().checkPermission(permName, pkgName, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().resolveService(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) {
        try {
            return getService().getPermissionGroupInfo(name, flags);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        try {
            // noinspection unchecked
            return getService().getInstalledApplications(flags, userId).getList();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        try {
            return getService().getPackageInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().resolveIntent(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().queryIntentContentProviders(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ActivityInfo getReceiverInfo(ComponentName componentName, int flags, int userId) {
        try {
            return getService().getReceiverInfo(componentName, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        try {
            return getService().getInstalledPackages(flags, userId).getList();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) {
        try {
            return getService().queryPermissionsByGroup(group, flags);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public PermissionInfo getPermissionInfo(String name, int flags) {
        try {
            return getService().getPermissionInfo(name, flags);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ActivityInfo getActivityInfo(ComponentName componentName, int flags, int userId) {
        try {
            return getService().getActivityInfo(componentName, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().queryIntentReceivers(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        try {
            return getService().getAllPermissionGroups(flags);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().queryIntentActivities(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().queryIntentServices(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        try {
            return getService().getApplicationInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
        try {
            return getService().resolveContentProvider(name, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ServiceInfo getServiceInfo(ComponentName componentName, int flags, int userId) {
        try {
            return getService().getServiceInfo(componentName, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ProviderInfo getProviderInfo(ComponentName componentName, int flags, int userId) {
        try {
            return getService().getProviderInfo(componentName, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public boolean activitySupportsIntent(ComponentName component, Intent intent, String resolvedType) {
        try {
            return getService().activitySupportsIntent(component, intent, resolvedType);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        try {
            // noinspection unchecked
            return getService().queryContentProviders(processName, uid, flags).getList();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<String> querySharedPackages(String packageName) {
        try {
            return getService().querySharedPackages(packageName);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String[] getPackagesForUid(int uid) {
        try {
            return getService().getPackagesForUid(uid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int getPackageUid(String packageName, int userId) {
        try {
            return getService().getPackageUid(packageName, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String getNameForUid(int uid) {
        try {
            return getService().getNameForUid(uid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }


    public IPackageInstaller getPackageInstaller() {
        try {
            return IPackageInstaller.Stub.asInterface(getService().getPackageInstaller());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }
}
