package com.lody.virtual.client.hook.secondary;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Pair;

import java.io.FileDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * @author Lody
 */

public abstract class StubBinder implements IBinder {
	private ClassLoader mClassLoader;
	private IBinder mBase;
	private IInterface mInterface;

	public StubBinder(ClassLoader classLoader, IBinder base) {
		this.mClassLoader = classLoader;
		this.mBase = base;
	}

	@Override
	public String getInterfaceDescriptor() throws RemoteException {
		return mBase.getInterfaceDescriptor();
	}

	@Override
	public boolean pingBinder() {
		return mBase.pingBinder();
	}

	@Override
	public boolean isBinderAlive() {
		return mBase.isBinderAlive();
	}

	@Override
	public IInterface queryLocalInterface(String descriptor) {
		if (mInterface == null) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			if (stackTrace != null && stackTrace.length > 1) {
				Pair<Class<?>, IInterface> res = getStubInterface(mBase, stackTrace);
				if (res == null) {
					return null;
				}
				InvocationHandler handler = createHandler(res.first, res.second);
				mInterface = (IInterface) Proxy.newProxyInstance(mClassLoader, new Class[]{res.first}, handler);
			}
		}
		return mInterface;

	}

	public abstract InvocationHandler createHandler(Class<?> interfaceClass, IInterface iInterface);

	/**
	 * Anti Proguard
	 *
	 * Search the AidlClass.Stub.asInterface(IBinder) method.
	 *
	 */
	private Pair<Class<?>, IInterface> getStubInterface(IBinder binder, StackTraceElement[] stackTraceElements) {
		for (int i = 1; i < stackTraceElements.length; i++) {
			StackTraceElement stackTraceElement = stackTraceElements[i];
			if (!stackTraceElement.isNativeMethod()) {
				try {
					Method method = mClassLoader.loadClass(stackTraceElement.getClassName())
							.getDeclaredMethod(stackTraceElement.getMethodName(), IBinder.class);
					if ((method.getModifiers() & Modifier.STATIC) != 0) {
						method.setAccessible(true);
						Class<?> returnType = method.getReturnType();
						if (returnType.isInterface() && IInterface.class.isAssignableFrom(returnType)) {
							IInterface iInterface = (IInterface) method.invoke(null, binder);
							if (iInterface != null) {
								return new Pair<Class<?>, IInterface>(returnType, iInterface);
							}
						}
					}
				} catch (Exception e) {
					// go to the next cycle
				}
			}
		}
		return null;
	}

	@Override
	public void dump(FileDescriptor fd, String[] args) throws RemoteException {
		mBase.dump(fd, args);
	}

	@Override
	public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
		mBase.dumpAsync(fd, args);
	}

	@Override
	public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
		return mBase.transact(code, data, reply, flags);
	}

	@Override
	public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
		mBase.linkToDeath(recipient, flags);
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
		return mBase.unlinkToDeath(recipient, flags);
	}
}
