package com.lody.virtual.server.interfaces;

import android.content.*;
import android.content.pm.*;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.remote.VParceledListSlice;

import java.util.List;

/**
 * @author Lody
 */
public interface IPackageManager {

    int getPackageUid(String packageName, int userId) throws RemoteException;

    String[] getPackagesForUid(int vuid) throws RemoteException;

    List<String> getSharedLibraries(String pkgName) throws RemoteException;

    int checkPermission(String permName, String pkgName, int userId) throws RemoteException;

    PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException;

    ActivityInfo getActivityInfo(ComponentName componentName, int flags, int userId) throws RemoteException;

    boolean activitySupportsIntent(ComponentName component, Intent intent, String resolvedType) throws RemoteException;

    ActivityInfo getReceiverInfo(ComponentName componentName, int flags, int userId) throws RemoteException;

    ServiceInfo getServiceInfo(ComponentName componentName, int flags, int userId) throws RemoteException;

    ProviderInfo getProviderInfo(ComponentName componentName, int flags, int userId) throws RemoteException;

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException;

    List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException;

    List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) throws RemoteException;

    ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) throws RemoteException;

    List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId) throws RemoteException;

    List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) throws RemoteException;

    VParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) throws RemoteException;

    VParceledListSlice<ApplicationInfo> getInstalledApplications(int flags, int userId) throws RemoteException;

    PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException;

    List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws RemoteException;

    PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException;

    List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException;

    ProviderInfo resolveContentProvider(String name, int flags, int userId) throws RemoteException;

    ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException;

    VParceledListSlice queryContentProviders(String processName, int vuid, int flags) throws RemoteException;

    List<String> querySharedPackages(String packageName) throws RemoteException;

    String getNameForUid(int uid) throws RemoteException;

    IBinder getPackageInstaller() throws RemoteException;
}
