// IAppManager.aidl
package com.lody.virtual.server;

import com.lody.virtual.server.interfaces.IAppObserver;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.proto.InstallResult;

interface IAppManager {

    void preloadAllApps();
    AppSetting findAppInfo(String pkg);

    InstallResult installApp(String apkPath, int flags);
    boolean uninstallApp(String pkg);
    List<AppSetting> getAllApps();
    int getAppCount();
    boolean isAppInstalled(String pkg);


    void registerObserver(IAppObserver observer);
    void unregisterObserver(IAppObserver observer);

}
