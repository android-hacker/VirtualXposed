// IAppManager.aidl
package com.lody.virtual.server;

import com.lody.virtual.server.interfaces.IPackageObserver;
import com.lody.virtual.server.interfaces.IAppRequestListener;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.remote.InstallResult;

interface IAppManager {
    int[] getPackageInstalledUsers(String packageName);
    void scanApps();
    void addVisibleOutsidePackage(String pkg);
    void removeVisibleOutsidePackage(String pkg);
    boolean isOutsidePackageVisible(String pkg);
    InstalledAppInfo getInstalledAppInfo(String pkg, int flags);
    InstallResult installPackage(String path, int flags);
    boolean isPackageLaunched(int userId, String packageName);
    void setPackageHidden(int userId, String packageName, boolean hidden);
    boolean installPackageAsUser(int userId, String packageName);
    boolean uninstallPackageAsUser(String packageName, int userId);
    boolean uninstallPackage(String packageName);
    boolean clearPackageAsUser(int userId, String packageName);
    boolean clearPackage(String packageName);
    List<InstalledAppInfo> getInstalledApps(int flags);
    List<InstalledAppInfo> getInstalledAppsAsUser(int userId, int flags);
    int getInstalledAppCount();
    boolean isAppInstalled(String packageName);
    boolean isAppInstalledAsUser(int userId, String packageName);

    void registerObserver(IPackageObserver observer);
    void unregisterObserver(IPackageObserver observer);

    void setAppRequestListener(IAppRequestListener listener);
    void clearAppRequestListener();
    IAppRequestListener getAppRequestListener();

}
