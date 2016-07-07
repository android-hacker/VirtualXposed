// IAppManager.aidl
package com.lody.virtual.service;

import com.lody.virtual.service.interfaces.IAppObserver;
import com.lody.virtual.helper.proto.Problem;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.InstallResult;

import android.content.pm.PackageInfo;

interface IAppManager {

    void preloadAllApps();
    AppInfo findAppInfo(String pkg);

    InstallResult installApp(String apkPath, int flags);
    boolean uninstallApp(String pkg);
    List<AppInfo> getAllApps();
    int getAppCount();
    boolean isAppInstalled(String pkg);


    void registerObserver(IAppObserver observer);
    void unregisterObserver(IAppObserver observer);

}
