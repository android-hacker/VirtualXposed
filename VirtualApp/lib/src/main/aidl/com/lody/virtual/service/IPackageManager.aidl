// IPackageManager.aidl
package com.lody.virtual.service;

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

import com.lody.virtual.helper.proto.ReceiverInfo;
import com.lody.virtual.helper.proto.VParceledListSlice;

interface IPackageManager {

        List<String> getSharedLibraries(String pkgName);

        int checkPermission(String permName, String pkgName);

        PackageInfo getPackageInfo(String packageName, int flags);

        ActivityInfo getActivityInfo(in ComponentName componentName, int flags);

        boolean activitySupportsIntent(in ComponentName component, in Intent intent,
                                           										  in String resolvedType);
         ActivityInfo getReceiverInfo(in ComponentName componentName, int flags);

         ServiceInfo getServiceInfo(in ComponentName componentName, int flags);

         ProviderInfo getProviderInfo(in ComponentName componentName, int flags);

         ResolveInfo resolveIntent(in Intent intent, in String resolvedType, int flags);

         List<ResolveInfo> queryIntentActivities(in Intent intent,in  String resolvedType, int flags);

         List<ResolveInfo> queryIntentReceivers(in Intent intent, String resolvedType, int flags);

         ResolveInfo resolveService(in Intent intent, String resolvedType, int flags);

         List<ResolveInfo> queryIntentServices(in Intent intent, String resolvedType, int flags);

         List<ResolveInfo> queryIntentContentProviders(in Intent intent, String resolvedType, int flags);

         VParceledListSlice getInstalledPackages(int flags);

         VParceledListSlice getInstalledApplications(int flags);

         PermissionInfo getPermissionInfo(in String name, int flags);

         List<PermissionInfo> queryPermissionsByGroup(in String group, int flags);

         PermissionGroupInfo getPermissionGroupInfo(in String name, int flags);

         List<PermissionGroupInfo> getAllPermissionGroups(int flags);

         ProviderInfo resolveContentProvider(in String name, int flags);

         ApplicationInfo getApplicationInfo(in String packageName, int flags);

         VParceledListSlice queryContentProviders(in String processName, int flags);

         List<ReceiverInfo> queryReceivers(in String processName, int flags);

         List<String> querySharedPackages(in String packageName);

         List<IntentFilter> getReceiverIntentFilter(in ActivityInfo info);
}
