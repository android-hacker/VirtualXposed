// IAppManager.aidl
package com.lody.virtual.server;

import com.lody.virtual.server.interfaces.IAppObserver;
import com.lody.virtual.server.interfaces.IAppRequestListener;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.remote.InstallResult;

interface IAppManager {

    void preloadAllApps();
    InstalledAppInfo getInstalledAppInfo(String pkg);

    InstallResult installApp(String apkPath, int flags);
    boolean uninstallApp(String pkg);
    List<InstalledAppInfo> getInstalledApps();
    int getInstalledAppCount();
    boolean isAppInstalled(String pkg);


    void registerObserver(IAppObserver observer);
    void unregisterObserver(IAppObserver observer);

    void setAppRequestListener(IAppRequestListener listener);
    void clearAppRequestListener();
    IAppRequestListener getAppRequestListener();

}
