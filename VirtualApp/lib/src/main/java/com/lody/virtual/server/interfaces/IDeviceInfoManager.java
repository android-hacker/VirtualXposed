package com.lody.virtual.server.interfaces;

import android.os.RemoteException;

import com.lody.virtual.remote.VDeviceInfo;

/**
 * @author Lody
 */
public interface IDeviceInfoManager {

    VDeviceInfo getDeviceInfo(int userId) throws RemoteException;

    void updateDeviceInfo(int userId, VDeviceInfo info) throws RemoteException;

}
