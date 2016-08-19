// IVClient.aidl
package com.lody.virtual.client;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;

interface IVClient {
    IBinder acquireProviderClient(in ProviderInfo info);
    IBinder getAppThread();
    IBinder getToken();
    void bindApplication(in String processName, in ApplicationInfo info, in List<String> sharedPackages, in List<ProviderInfo> providerInfos, in List<String> usesLibraries, int vuid);
}