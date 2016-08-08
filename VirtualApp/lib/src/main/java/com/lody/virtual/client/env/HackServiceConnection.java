package com.lody.virtual.client.env;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.binders.StubBinder;
import com.lody.virtual.helper.utils.VLog;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/**
 * @author Lody
 */

public class HackServiceConnection extends IServiceConnection.Stub {

	private static final String TAG = HackServiceConnection.class.getSimpleName();

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
									dumpCallingInfo(false, method, args);
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
									dumpCallingInfo(true, method, args);
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
								if (args != null && args.length > 0) {
									dumpCallingInfo(false, method, args);
								}
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
								dumpCallingInfo(false, method, args);
								if (args != null && args.length > 0) {
									String name = args[args.length - 1].getClass().getName();
									if ("com.google.android.gms.common.internal.GetServiceRequest".equals(name)
											|| "com.google.android.gms.common.internal.ValidateAccountRequest"
													.equals(name)) {
										args[args.length - 1] = modifyObject(args[args.length - 1]);
									}
									String hostPkg = VirtualCore.getCore().getHostPkg();
									String pkg = context.getPackageName();
									int i = 0;
									while (i < args.length) {
										if ((args[i] instanceof String) && hostPkg.equals(args[i])) {
											args[i] = pkg;
										}
										i++;
									}
								}
								dumpCallingInfo(true, method, args);
								return method.invoke(base, args);
							}
						};
					}

					private Object modifyObject(Object object) {
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

	private Context mContext;
	private IServiceConnection mConnection;

	public HackServiceConnection(Context context, IServiceConnection connection) {
		this.mContext = context;
		this.mConnection = connection;
	}

	private static void dumpCallingInfo(boolean hooked, Method method, Object[] args) {
		StringBuilder stringBuilder = new StringBuilder(20);
		stringBuilder.append(hooked ? "after-" : "before-");
		stringBuilder.append("call ");
		stringBuilder.append(method.getDeclaringClass().getName());
		stringBuilder.append(".");
		stringBuilder.append(method.getName());
		stringBuilder.append("(");
		if (args != null) {
			for (Object arg : args) {
				if (arg == null) {
					stringBuilder.append("null, ");
				} else {
					stringBuilder.append(arg.getClass().getSimpleName());
					stringBuilder.append("(");
					stringBuilder.append(arg.toString());
					stringBuilder.append("), ");
				}
			}
		}
		stringBuilder.append(")");
		VLog.d("Hook-SecondaryService", stringBuilder.toString());
	}

	@Override
	public void connected(ComponentName component, IBinder binder) throws RemoteException {
		try {
			String description = binder.getInterfaceDescriptor();
			ServiceFetcher fetcher = sHookSecondaryServiceMap.get(description);
			if (fetcher != null) {
				IBinder res = fetcher.getService(mContext, mContext.getClassLoader(), binder);
				if (res != null) {
					binder = res;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		mConnection.connected(component, binder);
	}

	@Override
	public IBinder asBinder() {
		return mConnection.asBinder();
	}

	private interface ServiceFetcher {
		IBinder getService(Context context, ClassLoader classLoader, IBinder binder);
	}
}
