// IServiceManager.aidl
package com.lody.virtual.service;

import android.content.Intent;
import android.app.Notification;
import android.app.IServiceConnection;
import android.content.ComponentName;


interface IServiceManager {

    ComponentName startService(in IBinder caller,in Intent service, String resolvedType);

    int stopService(in IBinder caller, in Intent service, String resolvedType);

    boolean stopServiceToken(in ComponentName className, in IBinder token, int startId);

    void setServiceForeground(in ComponentName className, in IBinder token, int id,
                            in Notification notification, boolean keepNotification);

    int bindService(in IBinder caller, in IBinder token, in Intent service,
                    String resolvedType, in IServiceConnection connection, int flags);

    boolean unbindService(in IServiceConnection connection);

    void unbindFinished(in IBinder token, in Intent service, in boolean doRebind);

    void serviceDoneExecuting(in IBinder token, in int type, in int startId, in int res);

    IBinder peekService(in Intent service, String resolvedType);

    void publishService(in IBinder token, in Intent intent, in IBinder service);

}
