// IAppManager.aidl
package com.lody.virtual.service;

import com.lody.virtual.service.interfaces.IAppObserver;
import com.lody.virtual.helper.proto.AppSettings;
import com.lody.virtual.helper.proto.InstallResult;

interface IAppManager {

    void preloadAllApps();
    AppSettings findAppInfo(String pkg);

    InstallResult installApp(String apkPath, int flags);
    boolean uninstallApp(String pkg);
    List<AppSettings> getAllApps();
    int getAppCount();
    boolean isAppInstalled(String pkg);


    void registerObserver(IAppObserver observer);
    void unregisterObserver(IAppObserver observer);

}
