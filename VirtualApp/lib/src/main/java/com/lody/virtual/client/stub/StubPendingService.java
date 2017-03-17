package com.lody.virtual.client.stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * @author Lody
 */

public class StubPendingService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // _VA_|_from_inner_ marked
        startService(intent);
        stopSelf();
        return START_NOT_STICKY;
    }
}
