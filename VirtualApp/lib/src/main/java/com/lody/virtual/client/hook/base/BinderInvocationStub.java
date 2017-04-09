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

import mirror.RefStaticMethod;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
@SuppressWarnings("unchecked")
public class BinderInvocationStub extends MethodInvocationStub<IInterface> implements IBinder {

    private static final String TAG = BinderInvocationStub.class.getSimpleName();
    private IBinder mBaseBinder;

    public BinderInvocationStub(RefStaticMethod<IInterface> asInterfaceMethod, IBinder binder) {
        this(asInterface(asInterfaceMethod, binder));
    }

    public BinderInvocationStub(Class<?> stubClass, IBinder binder) {
        this(asInterface(stubClass, binder));
    }


    public BinderInvocationStub(IInterface mBaseInterface) {
        super(mBaseInterface);
        mBaseBinder = getBaseInterface() != null ? getBaseInterface().asBinder() : null;
        addMethodProxy(new AsBinder());
    }

    private static IInterface asInterface(RefStaticMethod<IInterface> asInterfaceMethod, IBinder binder) {
        if (asInterfaceMethod == null || binder == null) {
            return null;
        }
        return asInterfaceMethod.call(binder);
    }

    private static IInterface asInterface(Class<?> stubClass, IBinder binder) {
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

    private final class AsBinder extends MethodProxy {

        @Override
        public String getMethodName() {
            return "asBinder";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return BinderInvocationStub.this;
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
