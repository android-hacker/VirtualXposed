// IReceiverManager.aidl
package com.lody.virtual.service;

// Declare any non-default types here with import statements

interface IReceiverManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    Intent registerReceiver(IBinder caller,
                IBinder receiver, in IntentFilter filter, String permission, int userId);

    int broadcastIntent(IBinder caller,
                        in Intent intent, String resolvedType,  IBinder resultTo,
                        int resultCode, String resultData, in Bundle map,
                        String requiredPermission, boolean serialized,
                        boolean sticky);

    void unregisterReceiver(IBinder receiver);
}
