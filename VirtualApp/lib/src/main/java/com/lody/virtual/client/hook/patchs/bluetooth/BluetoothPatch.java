package com.lody.virtual.client.hook.patchs.bluetooth;


import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.BackupBinderDelegate;
import com.lody.virtual.client.hook.binders.BluetoothBinderDelegate;

import mirror.android.os.ServiceManager;

public class BluetoothPatch extends PatchDelegate<BluetoothBinderDelegate> {

    @Override
    protected BluetoothBinderDelegate createHookDelegate() {
        return new BluetoothBinderDelegate();
    }

    @Override
    public void inject() throws Throwable {
        getHookDelegate().replaceService(BluetoothBinderDelegate.SERVICE_NAME);
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();

    }

    @Override
    public boolean isEnvBad() {
        return getHookDelegate() != ServiceManager.getService.call(BluetoothBinderDelegate.SERVICE_NAME);
    }
}
