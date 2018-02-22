package com.lody.virtual.client.ipc;


import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.server.IVirtualStorageService;

/**
 * @author Lody
 */

public class VirtualStorageManager {

    private static final VirtualStorageManager sInstance = new VirtualStorageManager();
    private IVirtualStorageService mRemote;


    public static VirtualStorageManager get() {
        return sInstance;
    }


    public IVirtualStorageService getRemote() {
        if (mRemote == null ||
                (!mRemote.asBinder().pingBinder() && !VirtualCore.get().isVAppProcess())) {
            synchronized (this) {
                Object remote = getRemoteInterface();
                mRemote = LocalProxyUtils.genProxy(IVirtualStorageService.class, remote);
            }
        }
        return mRemote;
    }

    private Object getRemoteInterface() {
        final IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.VS);
        return IVirtualStorageService.Stub.asInterface(binder);
    }

    public void setVirtualStorage(String packageName, int userId, String vsPath) {
        try {
            getRemote().setVirtualStorage(packageName, userId, vsPath);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public String getVirtualStorage(String packageName, int userId) {
        try {
            return getRemote().getVirtualStorage(packageName, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void setVirtualStorageState(String packageName, int userId, boolean enable) {
        try {
            getRemote().setVirtualStorageState(packageName, userId, enable);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public boolean isVirtualStorageEnable(String packageName, int userId) {
        try {
            return getRemote().isVirtualStorageEnable(packageName, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }
}
