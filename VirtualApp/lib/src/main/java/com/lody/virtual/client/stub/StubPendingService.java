package com.lody.virtual.client.stub;

import android.content.Intent;
import android.os.IBinder;

import com.lody.virtual.helper.component.BaseService;

/**
 * @author Lody
 */

public class StubPendingService extends BaseService {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent realIntent = intent.getParcelableExtra("_VA_|_intent_");
        if (realIntent != null) {
            startService(realIntent);
        }
        stopSelf();
        return START_NOT_STICKY;
    }
}
