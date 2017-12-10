package com.lody.virtual.server.interfaces;

import android.os.RemoteException;

import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;

import java.util.List;

/**
 * @author Lody
 */
public interface IAppManager {

    int[] getPackageInstalledUsers(String packageName) throws RemoteException;

    void scanApps() throws RemoteException;

    void addVisibleOutsidePackage(String pkg) throws RemoteException;

    void removeVisibleOutsidePackage(String pkg) throws RemoteException;

    boolean isOutsidePackageVisible(String pkg) throws RemoteException;

    InstalledAppInfo getInstalledAppInfo(String pkg, int flags) throws RemoteException;

    InstallResult installPackage(String path, int flags) throws RemoteException;

    boolean isPackageLaunched(int userId, String packageName) throws RemoteException;

    void setPackageHidden(int userId, String packageName, boolean hidden) throws RemoteException;

    boolean installPackageAsUser(int userId, String packageName) throws RemoteException;

    boolean uninstallPackageAsUser(String packageName, int userId) throws RemoteException;

    boolean uninstallPackage(String packageName) throws RemoteException;

    List<InstalledAppInfo> getInstalledApps(int flags) throws RemoteException;

    List<InstalledAppInfo> getInstalledAppsAsUser(int userId, int flags) throws RemoteException;

    int getInstalledAppCount() throws RemoteException;

    boolean isAppInstalled(String packageName) throws RemoteException;

    boolean isAppInstalledAsUser(int userId, String packageName) throws RemoteException;

    void registerObserver(IPackageObserver observer) throws RemoteException;

    void unregisterObserver(IPackageObserver observer) throws RemoteException;

    void setAppRequestListener(IAppRequestListener listener) throws RemoteException;

    void clearAppRequestListener() throws RemoteException;

    IAppRequestListener getAppRequestListener() throws RemoteException;
}
