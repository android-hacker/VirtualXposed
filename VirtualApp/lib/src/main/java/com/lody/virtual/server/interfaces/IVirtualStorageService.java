package com.lody.virtual.server.interfaces;

import android.os.RemoteException;

/**
 * @author Lody
 */
public interface IVirtualStorageService {

    void setVirtualStorage(String packageName, int userId, String vsPath) throws RemoteException;

    String getVirtualStorage(String packageName, int userId) throws RemoteException;

    void setVirtualStorageState(String packageName, int userId, boolean enable) throws RemoteException;

    boolean isVirtualStorageEnable(String packageName, int userId) throws RemoteException;
}
