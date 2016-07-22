// IServiceEnvironment.aidl
package com.lody.virtual.service.interfaces;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.app.IServiceConnection;

interface IServiceEnvironment {

    void handleStartService(in Intent intent, in ServiceInfo serviceInfo);

    int handleStopService(in ServiceInfo serviceInfo);

    boolean handleStopServiceToken(in IBinder token, in ServiceInfo serviceInfo, int startId);

    boolean handleUnbindService(in ServiceInfo serviceInfo, in IServiceConnection connection);

    int handleBindService(in IBinder token, in Intent service, in ServiceInfo serviceInfo, in IServiceConnection connection);

    IBinder handlePeekService(in ServiceInfo service);
}
