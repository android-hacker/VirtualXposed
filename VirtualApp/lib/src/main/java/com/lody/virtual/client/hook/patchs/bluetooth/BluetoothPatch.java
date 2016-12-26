package com.lody.virtual.client.hook.patchs.bluetooth;

import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;

import mirror.android.bluetooth.IBluetooth;

/**
 * @see android.bluetooth.BluetoothManager
 */
public class BluetoothPatch extends PatchBinderDelegate {
    public static final String SERVICE_NAME = Build.VERSION.SDK_INT >= 17 ?
            "bluetooth_manager" :
            "bluetooth";

    public BluetoothPatch() {
        super(IBluetooth.Stub.TYPE, SERVICE_NAME);
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new GetAddress());
    }
}
