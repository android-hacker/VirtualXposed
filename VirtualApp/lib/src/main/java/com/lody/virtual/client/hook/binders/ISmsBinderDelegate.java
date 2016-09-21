package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.telephony.ISms;

/**
 * @author Lody
 */

public class ISmsBinderDelegate extends HookBinderDelegate {
    @Override
    protected IInterface createInterface() {
        IBinder binder = ServiceManager.getService.call("isms");
        if (binder != null) {
            return ISms.Stub.asInterface.call(binder);
        }
        return null;
    }
}
