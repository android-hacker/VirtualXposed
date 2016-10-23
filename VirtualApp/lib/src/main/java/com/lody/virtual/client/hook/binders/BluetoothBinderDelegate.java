package com.lody.virtual.client.hook.binders;


import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.bluetooth.IBluetooth;
import mirror.android.os.ServiceManager;

public class BluetoothBinderDelegate extends HookBinderDelegate {
    public static final String SERVICE_NAME = Build.VERSION.SDK_INT >= 18 ?
            Context.BLUETOOTH_SERVICE :
            "bluetooth";

    @Override
    protected IInterface createInterface() {
        IBinder binder = ServiceManager.getService.call(SERVICE_NAME);
        return IBluetooth.Stub.asInterface.call(binder);
    }
}
