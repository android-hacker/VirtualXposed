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

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.server.IPackageInstaller;
import com.lody.virtual.server.IPackageManager;

import java.io.File;
import java.util.List;

/**
 * @author Lody
 */
public class VPackageManager {

    private static final VPackageManager sMgr = new VPackageManager();
    private IPackageManager mRemote;

    public static VPackageManager get() {
        return sMgr;
    }

    public IPackageManager getInterface() {
        if (mRemote == null ||
                (!mRemote.asBinder().pingBinder() && !VirtualCore.get().isVAppProcess())) {
            synchronized (VPackageManager.class) {
                Object remote = getRemoteInterface();
                mRemote = LocalProxyUtils.genProxy(IPackageManager.class, remote);
            }
        }
        return mRemote;
    }

    private Object getRemoteInterface() {
        final IBinder pmBinder = ServiceManagerNative.getService(ServiceManagerNative.PACKAGE);
        return IPackageManager.Stub.asInterface(pmBinder);
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
            ApplicationInfo info = getInterface().getApplicationInfo(packageName, flags, userId);
            if (info == null) {
                return null;
            }
            final int P = 28;
            String APACHE_LEGACY = "/system/framework/org.apache.http.legacy.boot.jar";
            if (!new File(APACHE_LEGACY).exists()) {
                APACHE_LEGACY = "/system/framework/org.apache.http.legacy.jar";
            }
            List<String> sharedLibraries = getInterface().getSharedLibraries(packageName);
            boolean forceAdd = sharedLibraries.contains("org.apache.http.legacy");

            // https://cs.android.com/android/platform/superproject/+/master:frameworks/base/services/core/java/com/android/server/pm/parsing/library/OrgApacheHttpLegacyUpdater.java;l=36?q=OrgApacheHttpLegacyUpdater&ss=android%2Fplatform%2Fsuperproject:frameworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2Fparsing%2Flibrary%2F
            if (android.os.Build.VERSION.SDK_INT >= P && info.targetSdkVersion < P || forceAdd) {
                String[] newSharedLibraryFiles;
                if (info.sharedLibraryFiles == null) {
                    newSharedLibraryFiles = new String[]{APACHE_LEGACY};
                } else {
                    int newLength = info.sharedLibraryFiles.length + 1;
                    newSharedLibraryFiles = new String[newLength];
                    System.arraycopy(info.sharedLibraryFiles, 0, newSharedLibraryFiles, 0, newLength - 1);
                    newSharedLibraryFiles[newLength - 1] = APACHE_LEGACY;
                }
                info.sharedLibraryFiles = newSharedLibraryFiles;
            }
            return info;
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

    public String getNameForUid(int uid) {
        try {
            return getInterface().getNameForUid(uid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }


    public IPackageInstaller getPackageInstaller() {
        try {
            return getInterface().getPackageInstaller();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }
}
