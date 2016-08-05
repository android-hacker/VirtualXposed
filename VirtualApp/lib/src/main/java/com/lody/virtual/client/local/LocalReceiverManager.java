package com.lody.virtual.client.local;

import android.app.ActivityThread;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.service.IReceiverManager;

/**
 * @author Lody
 */

public class LocalReceiverManager {

    private static IReceiverManager mRemote;

    private static IReceiverManager getInterface() {
        if (mRemote == null) {
            synchronized (LocalReceiverManager.class) {
                if (mRemote == null) {
                    IBinder receiverServiceBinder = ServiceManagerNative.getService(ServiceManagerNative.RECEIVER_MANAGER);
                    if (receiverServiceBinder == null)
                        return null;
                    mRemote = IReceiverManager.Stub.asInterface(receiverServiceBinder);
                }
            }
        }
        return mRemote;
    }


    public static Intent addIntentReceiver(IBinder caller, IIntentReceiver intentReceiver, IntentFilter filter,
                                           String permission, int userId) {

        IReceiverManager receiverService = getInterface();
        if (receiverService == null)
            return null;

        try {
            return receiverService.registerReceiver(caller, intentReceiver.asBinder(), filter, permission, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int sendBroadcast(Intent intent) {
        IReceiverManager receiverService = getInterface();
        if (receiverService == null)
            return -1;

        try {
            return receiverService.broadcastIntent(ActivityThread.currentActivityThread().getApplicationThread(),
                    intent,
                    intent.resolveTypeIfNeeded(
                            ActivityThread.currentActivityThread().getApplication().getContentResolver()),
                    null, 0, null, null, null, false, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void unregisterReceiver(IIntentReceiver intentReceiver) {
        IReceiverManager receiverManager = getInterface();
        if (receiverManager == null)
            return;
        try {
            receiverManager.unregisterReceiver(intentReceiver.asBinder());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
