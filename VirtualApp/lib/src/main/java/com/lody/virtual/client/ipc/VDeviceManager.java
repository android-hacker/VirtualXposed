package com.lody.virtual.client.ipc;

import android.os.RemoteException;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.ipcbus.IPCSingleton;
import com.lody.virtual.remote.VDeviceInfo;
import com.lody.virtual.server.interfaces.IDeviceInfoManager;

/**
 * @author Lody
 */

public class VDeviceManager {

    private static final VDeviceManager sInstance = new VDeviceManager();
    private IPCSingleton<IDeviceInfoManager> singleton = new IPCSingleton<>(IDeviceInfoManager.class);


    public static VDeviceManager get() {
        return sInstance;
    }


    public IDeviceInfoManager getService() {
        return singleton.get();
    }

    public VDeviceInfo getDeviceInfo(int userId) {
        try {
            return getService().getDeviceInfo(userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }
}