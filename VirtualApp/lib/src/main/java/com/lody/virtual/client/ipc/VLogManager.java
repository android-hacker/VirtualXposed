package com.lody.virtual.client.ipc;

import android.os.RemoteException;

import com.lody.virtual.server.ILogManager;

/**
 * @author Lody
 */

public class VLogManager {

    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_WARNING = 2;
    public static final int LEVEL_ERROR = 3;

    private static final VLogManager sInstance = new VLogManager();

    private ILogManager mRemote;

    public static VLogManager get() {
        return sInstance;
    }

    public ILogManager getRemote() {
        if (mRemote == null) {
            synchronized (this) {
                if (mRemote == null) {
                    Object remote = getRemoteInterface();
                    mRemote = LocalProxyUtils.genProxy(ILogManager.class, remote);
                }
            }
        }
        return mRemote;
    }

    private Object getRemoteInterface() {
        return ILogManager.Stub
                .asInterface(ServiceManagerNative.getService(ServiceManagerNative.LOG_REPORT));
    }

    public void d(String tag, String msg) {
        reportLog(LEVEL_DEBUG, tag, msg);
    }

    public void w(String tag, String msg) {
        reportLog(LEVEL_WARNING, tag, msg);
    }

    public void e(String tag, String msg) {
        reportLog(LEVEL_ERROR, tag, msg);
    }

    public void reportLog(int level, String tag, String msg) {
        try {
            getRemote().reportLog(level, tag, msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
