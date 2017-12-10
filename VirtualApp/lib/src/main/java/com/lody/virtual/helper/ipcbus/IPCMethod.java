package com.lody.virtual.helper.ipcbus;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Lody
 */
public class IPCMethod {

    private int code;
    private Method method;
    private String interfaceName;

    public IPCMethod(int code, Method method, String interfaceName) {
        this.code = code;
        this.method = method;
        this.interfaceName = interfaceName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public Method getMethod() {
        return method;
    }

    public void handleTransact(Object server, Parcel data, Parcel reply) {
        data.enforceInterface(interfaceName);
        Object[] parameters = data.readArray(getClass().getClassLoader());
        Object res = null;
        try {
            res = method.invoke(server, parameters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        reply.writeNoException();
        reply.writeValue(res);
    }

    public Object callRemote(IBinder server, Object[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Object result;
        try {
            data.writeInterfaceToken(interfaceName);
            data.writeArray(args);
            server.transact(code, data, reply, 0);
            reply.readException();
            result = readValue(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
        return result;
    }

    private Object readValue(Parcel replay) {
        Object result = replay.readValue(getClass().getClassLoader());
        if (result instanceof Parcelable[]) {
            Parcelable[] parcelables = (Parcelable[]) result;
            Object[] results = (Object[]) Array.newInstance(method.getReturnType().getComponentType(), parcelables.length);
            System.arraycopy(parcelables, 0, results, 0, results.length);
            return results;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPCMethod ipcMethod = (IPCMethod) o;

        return method != null ? method.equals(ipcMethod.method) : ipcMethod.method == null;
    }

}
