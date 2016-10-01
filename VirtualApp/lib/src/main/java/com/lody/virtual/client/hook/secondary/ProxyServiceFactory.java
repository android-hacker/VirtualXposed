package com.lody.virtual.client.hook.secondary;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

public class ProxyServiceFactory {

	private static final String TAG = ProxyServiceFactory.class.getSimpleName();

	private static Map<String, ServiceFetcher> sHookSecondaryServiceMap = new HashMap<>();

	static {
		sHookSecondaryServiceMap.put("com.google.android.auth.IAuthManagerService", new ServiceFetcher() {
			@Override
			public IBinder getService(final Context context, ClassLoader classLoader, IBinder binder) {
				return new StubBinder(classLoader, binder) {
					@Override
					public InvocationHandler createHandler(Class<?> interfaceClass, final IInterface base) {
						return new InvocationHandler() {
							@Override
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								try {
									return method.invoke(base, args);
								} catch (InvocationTargetException e) {
									if (e.getCause() != null) {
										throw e.getCause();
									}
									throw e;
								}
							}
						};
					}
				};
			}
		});

		sHookSecondaryServiceMap.put("com.android.vending.billing.IInAppBillingService", new ServiceFetcher() {
			@Override
			public IBinder getService(final Context context, ClassLoader classLoader, IBinder binder) {
				return new StubBinder(classLoader, binder) {
					@Override
					public InvocationHandler createHandler(Class<?> interfaceClass, final IInterface base) {
						return new InvocationHandler() {
							@Override
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								try {
									return method.invoke(base, args);
								} catch (InvocationTargetException e) {
									if (e.getCause() != null) {
										throw e.getCause();
									}
									throw e;
								}
							}
						};
					}
				};
			}
		});

		sHookSecondaryServiceMap.put("com.google.android.gms.common.internal.IGmsServiceBroker", new ServiceFetcher() {
			@Override
			public IBinder getService(final Context context, ClassLoader classLoader, IBinder binder) {
				return new StubBinder(classLoader, binder) {

					@Override
					public InvocationHandler createHandler(Class<?> interfaceClass, final IInterface base) {
						return new InvocationHandler() {
							@Override
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								try {
									return method.invoke(base, args);
								} catch (InvocationTargetException e) {
									if (e.getCause() != null) {
										throw e.getCause();
									}
									throw e;
								}
							}
						};
					}

				};
			}
		});
	}


	public static IBinder getProxyService(Context context, ComponentName component, IBinder binder) {
		if (context == null || binder == null) {
			return null;
		}
		try {
			String description = binder.getInterfaceDescriptor();
			ServiceFetcher fetcher = sHookSecondaryServiceMap.get(description);
			if (fetcher != null) {
				IBinder res = fetcher.getService(context, context.getClassLoader(), binder);
				if (res != null) {
					return res;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}




	private interface ServiceFetcher {
		IBinder getService(Context context, ClassLoader classLoader, IBinder binder);
	}
}
