package com.lody.virtual.server.secondary;

import android.os.Binder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;

/**
 * @author Lody
 */
public class FakeIdentityBinder extends Binder {

    private Binder mBase;

    public FakeIdentityBinder(Binder binder) {
        this.mBase = binder;
    }

    public final void attachInterface(IInterface owner, String descriptor) {
        mBase.attachInterface(owner, descriptor);
    }

    public final String getInterfaceDescriptor() {
        return mBase.getInterfaceDescriptor();
    }

    public final boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            Binder.restoreCallingIdentity(getFakeIdentity());
            return mBase.transact(code, data, reply, flags);
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    /**
     * See: http://androidxref.com/6.0.1_r10/xref/frameworks/native/libs/binder/IPCThreadState.cpp#356
     */
    protected long getFakeIdentity() {
        return (long) getFakeUid() << 32 | (long) getFakePid();
    }

    protected int getFakeUid() {
        return Process.myUid();
    }

    protected int getFakePid() {
        return Process.myPid();
    }

    public final IInterface queryLocalInterface(String descriptor) {
        return mBase.queryLocalInterface(descriptor);
    }
}
