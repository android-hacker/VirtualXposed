package com.lody.virtual.client.ipc;

import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.remote.vloc.VCell;
import com.lody.virtual.remote.vloc.VLocation;
import com.lody.virtual.server.IVirtualLocationManager;

import java.util.List;

/**
 * @author Lody
 */

public class VirtualLocationManager {

    private static final VirtualLocationManager sInstance = new VirtualLocationManager();
    private IVirtualLocationManager mRemote;

    public static final int MODE_CLOSE = 0;
    public static final int MODE_USE_GLOBAL = 1;
    public static final int MODE_USE_SELF = 2;


    public static VirtualLocationManager get() {
        return sInstance;
    }


    public IVirtualLocationManager getRemote() {
        if (mRemote == null ||
                (!mRemote.asBinder().pingBinder() && !VirtualCore.get().isVAppProcess())) {
            synchronized (this) {
                Object remote = getRemoteInterface();
                mRemote = LocalProxyUtils.genProxy(IVirtualLocationManager.class, remote);
            }
        }
        return mRemote;
    }

    private Object getRemoteInterface() {
        final IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.VIRTUAL_LOC);
        return IVirtualLocationManager.Stub.asInterface(binder);
    }

    public int getMode(int userId, String pkg) {
        try {
            return getRemote().getMode(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int getMode() {
        return getMode(MethodProxy.getAppUserId(), MethodProxy.getAppPkg());
    }

    public void setMode(int userId, String pkg, int mode) {
        try {
            getRemote().setMode(userId, pkg, mode);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setCell(int userId, String pkg, VCell cell) {
        try {
            getRemote().setCell(userId, pkg, cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setAllCell(int userId, String pkg, List<VCell> cell) {
        try {
            getRemote().setAllCell(userId, pkg, cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setNeighboringCell(int userId, String pkg, List<VCell> cell) {
        try {
            getRemote().setNeighboringCell(userId, pkg, cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public VCell getCell(int userId, String pkg) {
        try {
            return getRemote().getCell(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<VCell> getAllCell(int userId, String pkg) {
        try {
            return getRemote().getAllCell(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<VCell> getNeighboringCell(int userId, String pkg) {
        try {
            return getRemote().getNeighboringCell(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }


    public void setGlobalCell(VCell cell) {
        try {
            getRemote().setGlobalCell(cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setGlobalAllCell(List<VCell> cell) {
        try {
            getRemote().setGlobalAllCell(cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setGlobalNeighboringCell(List<VCell> cell) {
        try {
            getRemote().setGlobalNeighboringCell(cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setLocation(int userId, String pkg, VLocation loc) {
        try {
            getRemote().setLocation(userId, pkg, loc);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public VLocation getLocation(int userId, String pkg) {
        try {
            return getRemote().getLocation(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public VLocation getLocation() {
        return getLocation(MethodProxy.getAppUserId(), MethodProxy.getAppPkg());
    }

    public void setGlobalLocation(VLocation loc) {
        try {
            getRemote().setGlobalLocation(loc);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public VLocation getGlobalLocation() {
        try {
            return getRemote().getGlobalLocation();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }
}
