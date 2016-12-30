package com.lody.virtual.client.hook.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;

import java.io.FileDescriptor;
import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
@SuppressWarnings("unchecked")
public class HookBinderDelegate extends HookDelegate<IInterface> implements IBinder {

    private static final String TAG = HookBinderDelegate.class.getSimpleName();
    private IBinder mBaseBinder;

    public HookBinderDelegate(Class<?> stubClass, IBinder binder) {
        this(createStub(stubClass, binder));
    }

    public HookBinderDelegate(IInterface mBaseInterface) {
        super(mBaseInterface);
        mBaseBinder = getBaseInterface() != null ? getBaseInterface().asBinder() : null;
        addHook(new AsBinder());
    }

    private static IInterface createStub(Class<?> stubClass, IBinder binder) {
        try {
            if (stubClass == null || binder == null) {
                return null;
            }
            Method asInterface = stubClass.getMethod("asInterface", IBinder.class);
            return (IInterface) asInterface.invoke(null, binder);
        } catch (Exception e) {
            Log.d(TAG, "Could not create stub " + stubClass.getName() + ". Cause: " + e);
            return null;
        }
    }

    public void replaceService(String name) {
        if (mBaseBinder != null) {
            ServiceManager.sCache.get().put(name, this);
        }
    }

    private final class AsBinder extends Hook {

        @Override
        public String getName() {
            return "asBinder";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return HookBinderDelegate.this;
        }
    }


    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return mBaseBinder.getInterfaceDescriptor();
    }

    public Context getContext() {
        return VirtualCore.get().getContext();
    }

    @Override
    public boolean pingBinder() {
        return mBaseBinder.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return mBaseBinder.isBinderAlive();
    }

    @Override
    public IInterface queryLocalInterface(String descriptor) {
        return getProxyInterface();
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        mBaseBinder.dump(fd, args);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
        mBaseBinder.dumpAsync(fd, args);
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return mBaseBinder.transact(code, data, reply, flags);
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
        mBaseBinder.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return mBaseBinder.unlinkToDeath(recipient, flags);
    }

    public IBinder getBaseBinder() {
        return mBaseBinder;
    }

}
