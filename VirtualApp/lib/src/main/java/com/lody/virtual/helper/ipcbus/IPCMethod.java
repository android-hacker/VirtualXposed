package com.lody.virtual.helper.ipcbus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Lody
 */
public class IPCMethod {

    private int code;
    private Method method;
    private String interfaceName;
    private MethodParamConverter[] converters;
    private MethodParamConverter resultConverter;


    public IPCMethod(int code, Method method, String interfaceName) {
        this.code = code;
        this.method = method;
        this.interfaceName = interfaceName;
        Class<?>[] parameterTypes = method.getParameterTypes();
        converters = new MethodParamConverter[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (isAidlParam(parameterTypes[i])) {
                converters[i] = new AidlParamConverter(parameterTypes[i]);
            }
        }
        Class<?> returnType = method.getReturnType();
        if (isAidlParam(returnType)) {
            resultConverter = new AidlParamConverter(returnType);
        }
    }

    private boolean isAidlParam(Class<?> type) {
        return type.isInterface() && IInterface.class.isAssignableFrom(type);
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
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (converters[i] != null) {
                    parameters[i] = converters[i].convert(parameters[i]);
                }
            }
        }
        try {
            Object res = method.invoke(server, parameters);
            reply.writeNoException();
            reply.writeValue(res);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            reply.writeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            reply.writeException(e);
        }
    }

    private static Method findAsInterfaceMethod(Class<?> type) {
        for (Class<?> innerClass : type.getDeclaredClasses()) {
            // public static class Stub extends Binder implements IType
            if (Modifier.isStatic(innerClass.getModifiers())
                    && Binder.class.isAssignableFrom(innerClass)
                    && type.isAssignableFrom(innerClass)) {
                // public static IType asInterface(android.os.IBinder obj)
                for (Method method : innerClass.getDeclaredMethods()) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        Class<?>[] types = method.getParameterTypes();
                        if (types.length == 1 && types[0] == IBinder.class) {
                            return method;
                        }
                    }
                }
            }
        }
        throw new IllegalStateException("Can not found the " + type.getName() + "$Stub.asInterface method.");
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
            if (resultConverter != null) {
                result = resultConverter.convert(result);
            }
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

    public interface MethodParamConverter {
        Object convert(Object param);
    }

    private class AidlParamConverter implements MethodParamConverter {

        private Method asInterfaceMethod;
        private Class<?> type;

        AidlParamConverter(Class<?> type) {
            this.type = type;
        }

        @Override
        public Object convert(Object param) {
            if (param != null) {
                if (asInterfaceMethod == null) {
                    synchronized (this) {
                        if (asInterfaceMethod == null) {
                            asInterfaceMethod = findAsInterfaceMethod(type);
                        }
                    }
                }
                try {
                    return asInterfaceMethod.invoke(null, param);
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
            return null;
        }
    }

}
