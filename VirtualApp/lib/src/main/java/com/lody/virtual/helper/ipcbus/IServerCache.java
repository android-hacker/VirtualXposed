package com.lody.virtual.helper.ipcbus;

import android.os.IBinder;

/**
 * @author Lody
 */
public interface IServerCache {
    void join(String serverName, IBinder binder);
    IBinder query(String serverName);
}
