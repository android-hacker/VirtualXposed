// IPackageManager.aidl
package com.lody.virtual.server;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;
import android.content.IntentFilter;
import android.content.pm.PermissionInfo;
import android.content.pm.PermissionGroupInfo;

import com.lody.virtual.remote.ReceiverInfo;
import com.lody.virtual.remote.VParceledListSlice;

import com.lody.virtual.server.IPackageInstaller;

interface IPackageManager {

        int getPackageUid(String packageName, int userId);

        String[] getPackagesForUid(int vuid);

        List<String> getSharedLibraries(String pkgName);

        int checkPermission(String permName, String pkgName, int userId);

        PackageInfo getPackageInfo(String packageName, int flags, int userId);

        ActivityInfo getActivityInfo(in ComponentName componentName, int flags, int userId);

        boolean activitySupportsIntent(in ComponentName component, in Intent intent,
                                           										  in String resolvedType);
         ActivityInfo getReceiverInfo(in ComponentName componentName, int flags, int userId);

         ServiceInfo getServiceInfo(in ComponentName componentName, int flags, int userId);

         ProviderInfo getProviderInfo(in ComponentName componentName, int flags, int userId);

         ResolveInfo resolveIntent(in Intent intent, in String resolvedType, int flags, int userId);

         List<ResolveInfo> queryIntentActivities(in Intent intent,in  String resolvedType, int flags, int userId);

         List<ResolveInfo> queryIntentReceivers(in Intent intent, String resolvedType, int flags, int userId);

         ResolveInfo resolveService(in Intent intent, String resolvedType, int flags, int userId);

         List<ResolveInfo> queryIntentServices(in Intent intent, String resolvedType, int flags, int userId);

         List<ResolveInfo> queryIntentContentProviders(in Intent intent, String resolvedType, int flags, int userId);

         VParceledListSlice getInstalledPackages(int flags, int userId);

         VParceledListSlice getInstalledApplications(int flags, int userId);

         PermissionInfo getPermissionInfo(in String name, int flags);

         List<PermissionInfo> queryPermissionsByGroup(in String group, int flags);

         PermissionGroupInfo getPermissionGroupInfo(in String name, int flags);

         List<PermissionGroupInfo> getAllPermissionGroups(int flags);

         ProviderInfo resolveContentProvider(in String name, int flags, int userId);

         ApplicationInfo getApplicationInfo(in String packageName, int flags, int userId);

         VParceledListSlice queryContentProviders(in String processName, int vuid, int flags);

         List<String> querySharedPackages(in String packageName);

         String getNameForUid(int uid);

         IPackageInstaller getPackageInstaller();
}
