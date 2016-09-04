package com.lody.virtual.client.hook.secondary;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
								if (args != null && args.length > 0) {
									for (Object arg : args) {
										if (arg instanceof Bundle) {
											Bundle bundle = (Bundle) arg;
											if (bundle.containsKey("androidPackageName")) {
												bundle.putString("androidPackageName", context.getPackageName());
											}
											if (bundle.containsKey("clientPackageName")) {
												bundle.putString("clientPackageName", context.getPackageName());
											}
										}
									}
								}
								return method.invoke(base, args);
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
								return method.invoke(base, args);
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
								if (args != null && args.length > 0) {
									String name = args[args.length - 1].getClass().getName();
									if ("com.google.android.gms.common.internal.GetServiceRequest".equals(name)
											|| "com.google.android.gms.common.internal.ValidateAccountRequest"
													.equals(name)) {
										args[args.length - 1] = replaceObjectPkgFields(args[args.length - 1]);
									}
									String hostPkg = VirtualCore.get().getHostPkg();
									String pkg = context.getPackageName();
									int i = 0;
									while (i < args.length) {
										if ((args[i] instanceof String) && hostPkg.equals(args[i])) {
											args[i] = pkg;
										}
										i++;
									}
								}
								return method.invoke(base, args);
							}
						};
					}

					private Object replaceObjectPkgFields(Object object) {
						for (Field field : object.getClass().getDeclaredFields()) {
							field.setAccessible(true);
							if ((field.getModifiers() & Modifier.STATIC) == 0) {
								try {
									if (field.get(object) instanceof String) {
										field.set(object, context.getPackageName());
										break;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						return object;
					}
				};
			}
		});
	}


	public static IBinder getProxyService(Context context, ComponentName component, IBinder binder) {
		if (context == null) {
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
