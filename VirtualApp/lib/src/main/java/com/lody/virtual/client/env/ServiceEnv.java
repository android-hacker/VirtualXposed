package com.lody.virtual.client.env;

import android.app.ActivityManagerNative;
import android.app.Application;
import android.app.IServiceConnection;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.modifiers.ContextModifier;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.ComponentUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 *
 *
 * @see android.app.ActivityThread
 * @see android.app.IActivityManager
 *
 *   VirtualApp的Service运行时环境。
 *
 */
public class ServiceEnv {

	private static final ServiceEnv sEnv = new ServiceEnv();

	private final Map<String, RunningServiceRecord> mServices = new ConcurrentHashMap<>();
	private final Map<IBinder, RunningServiceRecord> mConns = new ConcurrentHashMap<>();

	private ServiceEnv() {
	}

	public static ServiceEnv getEnv() {
		return sEnv;
	}

	private static void runOnUiThread(Runnable r) {
		RuntimeEnv.getUIHandler().post(r);
	}

	public void handleStartService(final AppInfo appInfo, final Intent intent, final ServiceInfo serviceInfo) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				startServiceInner(appInfo, intent, serviceInfo);
			}
		});
	}

	public void handleStopService(final AppInfo appInfo, final ServiceInfo serviceInfo) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				stopServiceTokenInner(appInfo, serviceInfo, -1);
			}
		});

	}

	public void handleStopServiceToken(final AppInfo appInfo, final ServiceInfo serviceInfo, final int startId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				stopServiceTokenInner(appInfo, serviceInfo, startId);
			}
		});

	}

	public void handleBindService(final AppInfo appInfo, final Intent intent, final ServiceInfo serviceInfo,
			final IServiceConnection serviceConnection) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bindServiceInner(appInfo, intent, serviceInfo, serviceConnection);
			}
		});
	}

	public boolean handleUnbindService(final IServiceConnection connection) {
		return unbindServiceInner(connection, true);
	}

	private boolean unbindServiceInner(final IServiceConnection connection, boolean stopIfEmpty) {
		IBinder connBinder = connection.asBinder();
		boolean result = false;
		if (connBinder != null) {
			final RunningServiceRecord record = mConns.get(connBinder);
			if (record != null) {
				record.connections.remove(connBinder);
				mConns.remove(connBinder);
				if (record.connections.isEmpty()) {
					Intent intent = new Intent();
					intent.setClassName(record.serviceInfo.packageName, record.serviceInfo.name);
					result = record.service.onUnbind(intent);
				}
				if (record.connections.isEmpty() && record.startCount == 0) {
					mServices.remove(record.serviceInfo.name);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							record.service.onDestroy();
						}
					});
				}
			}
		}
		return result;
	}

	private void bindServiceInner(AppInfo appInfo, Intent intent, ServiceInfo serviceInfo,
			IServiceConnection connection) {
		RunningServiceRecord record = getService(appInfo, serviceInfo, true);
		if (record != null) {
			// proxy of connection
			connection = new ServiceConnectionImpl(appInfo.getApplication(), connection);

			IBinder connBinder = connection.asBinder();
			if (connBinder != null) {
				intent.setExtrasClassLoader(record.service.getClassLoader());
				if (mConns.get(connBinder) != record && !record.connections.contains(connBinder)) {
					if (record.binder == null) {
						record.binder = record.service.onBind(intent);
						try {
							connection.connected(intent.getComponent(), record.binder);
						} catch (RemoteException e) {
							throw new RuntimeException(
									"Unable to bind service " + record.serviceInfo.name + " : " + e.getMessage());
						}
						mConns.put(connBinder, record);
						record.connections.add(connBinder);
					}
				}
			}
		}
	}

	private void stopServiceTokenInner(AppInfo appInfo, ServiceInfo serviceInfo, int startId) {
		final RunningServiceRecord record = getService(appInfo, serviceInfo, false);
		if (record != null) {
			if (startId == record.startId || startId == -1) {
				record.startCount = 0;
				if (record.connections.isEmpty()) {
					mServices.remove(record.serviceInfo.name);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							record.service.onDestroy();
						}
					});
				}
			}
		}
	}

	private void startServiceInner(AppInfo appInfo, Intent intent, ServiceInfo serviceInfo) {
		RunningServiceRecord record = getService(appInfo, serviceInfo, true);
		if (record != null) {
			intent.setExtrasClassLoader(record.service.getClassLoader());
			record.service.onStartCommand(intent, 0, record.startId++);
			record.startCount++;
		}
	}

	public RunningServiceRecord getService(AppInfo appInfo, ServiceInfo serviceInfo, boolean autoCreate) {
		RunningServiceRecord record = mServices.get(serviceInfo.name);
		if (record == null && autoCreate) {
			try {
				record = createServiceRecord(appInfo, serviceInfo);
				mServices.put(serviceInfo.name, record);
			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException("Unable to instance service " + serviceInfo.name + " : " + e.getMessage());
			}
		}
		return record;
	}

	private RunningServiceRecord createServiceRecord(AppInfo appInfo, ServiceInfo serviceInfo) throws Throwable {
		AppSandBox.install(ComponentUtils.getProcessName(serviceInfo), serviceInfo.packageName);
		RunningServiceRecord record = new RunningServiceRecord();
		record.appInfo = appInfo;
		record.serviceInfo = serviceInfo;
		record.token = new ServiceFakeBinder();
		String className = serviceInfo.name;

		Application application = appInfo.getApplication();
		ClassLoader classLoader = appInfo.getClassLoader();
		// 创建ContextImpl
		Service service = (Service) classLoader.loadClass(className).newInstance();
		record.service = service;
		Context base = application.createPackageContext(appInfo.packageName, Context.CONTEXT_INCLUDE_CODE);
		// Fuck AppOps
		ContextModifier.modifyContext(base);
		ContextModifier.setOuterContext(base, service);

		// Call attach
		service.attach(base, VirtualCore.mainThread(), className, record.token, application,
				ActivityManagerNative.getDefault());
		// Call systemReady
		service.onCreate();

		return record;
	}

	private static final class ServiceFakeBinder extends Binder {
		// Empty
	}

	private static final class RunningServiceRecord {
		AppInfo appInfo;
		IBinder binder;
		ServiceFakeBinder token;
		Set<IBinder> connections = new HashSet<>();
		ServiceInfo serviceInfo;
		int startCount = 0;
		int startId = 1;
		Service service;

		private RunningServiceRecord() {
		}
	}


}
