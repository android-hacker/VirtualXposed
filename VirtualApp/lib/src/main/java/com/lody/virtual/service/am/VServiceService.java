package com.lody.virtual.service.am;

import android.app.ActivityManagerNative;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.service.ProviderCaller;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.utils.XLog;
import com.lody.virtual.service.IServiceManager;
import com.lody.virtual.service.VPackageService;
import com.lody.virtual.service.interfaces.IServiceEnvironment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 *
 */
public class VServiceService extends IServiceManager.Stub {

	private static final String TAG = VServiceService.class.getSimpleName();

	private static final VServiceService sService = new VServiceService();

	private Map<IBinder, ServiceRecord> serviceConnectionMap = new ConcurrentHashMap<IBinder, ServiceRecord>();

	private class ServiceRecord {
		IBinder connection;
		ServiceInfo serviceInfo;
		int pid;

		ServiceRecord(IBinder connection, ServiceInfo serviceInfo, int pid) {
			this.connection = connection;
			this.serviceInfo = serviceInfo;
			this.pid = pid;
		}
	}


	public static VServiceService getService() {
		return sService;
	}

	private static ServiceInfo getServiceInfo(ComponentName service) {
		if (service != null) {
			try {
				return VPackageService.getService().getServiceInfo(service, 0);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static ServiceInfo getServiceInfo(Intent service) {
		if (service != null) {
			return getServiceInfo(service.getComponent());
		}
		return null;
	}

	private IServiceEnvironment getServiceEnvironment(ServiceInfo serviceInfo, ProviderInfo serviceEnv) {
		Context context = VirtualCore.getCore().getContext();
		Bundle bundle = new ProviderCaller.Builder(context, serviceEnv.authority)
				.methodName(MethodConstants.GET_SERVICE_RUNTIME)
				.call();
		if (bundle != null) {
			IBinder binder = BundleCompat.getBinder(bundle, ExtraConstants.EXTRA_BINDER);
			IServiceEnvironment env = IServiceEnvironment.Stub.asInterface(binder);
			if (env == null) {
				XLog.e(TAG, "Unable to fetch ServiceEnvironment for client(%s)", serviceEnv.authority);
			}
			return env;
		}
		return null;
	}

	public ComponentName startService(IBinder caller, Intent service, String resolvedType) {
		ServiceInfo serviceInfo = getServiceInfo(service);
		if (serviceInfo == null) {
			return service.getComponent();
		}
		ProviderInfo serviceEnv = VActivityService.getService().fetchServiceRuntime(serviceInfo);
		if (serviceEnv != null) {
			IServiceEnvironment environment = getServiceEnvironment(serviceInfo, serviceEnv);
			if (environment == null) {
				return service.getComponent();
			}
			try {
				environment.handleStartService(service, serviceInfo);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}
		return service.getComponent();
	}

	public int stopService(IBinder caller, Intent service, String resolvedType) {
		ServiceInfo serviceInfo = getServiceInfo(service);
		if (serviceInfo == null) {
			return 0;
		}
		ProviderInfo serviceEnv = VActivityService.getService().fetchRunningServiceRuntime(serviceInfo);
		if (serviceEnv == null) {
			return 0;
		}
		IServiceEnvironment environment = getServiceEnvironment(serviceInfo, serviceEnv);
		if (environment == null) {
			return 0;
		}
		try {
			environment.handleStopService(serviceInfo);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean stopServiceToken(ComponentName componentName, IBinder token, int startId) {
		if (componentName != null && token != null) {
			ServiceInfo serviceInfo = getServiceInfo(componentName);
			if (serviceInfo == null) {
				return false;
			}
			ProviderInfo serviceEnv = VActivityService.getService().fetchRunningServiceRuntime(serviceInfo);
			if (serviceEnv == null) {
				return false;
			}
			IServiceEnvironment environment = getServiceEnvironment(serviceInfo, serviceEnv);
			if (environment == null) {
				return false;
			}
			try {
				environment.handleStopServiceToken(token, serviceInfo, startId);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public IBinder peekService(Intent service, String resolvedType) {
		try {
			return ActivityManagerNative.getDefault().peekService(service, null,
					VirtualCore.getCore().getHostPkg());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {

	}

	public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType,
			IServiceConnection connection, int flags) {

		if (service == null || connection == null) {
			return 0;
		}
		ServiceInfo serviceInfo = getServiceInfo(service);
		if (serviceInfo == null) {
			return 0;
		}
		ProviderInfo serviceEnv = VActivityService.getService().fetchServiceRuntime(serviceInfo);
		if (serviceEnv != null) {
			IServiceEnvironment environment = getServiceEnvironment(serviceInfo, serviceEnv);
			if (environment == null) {
				return 0;
			}
			int result = 0;
			try {
				result = environment.handleBindService(token, service, serviceInfo, connection);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (result != 0) {
				// bind Service Success
				IBinder connectionBinder = connection.asBinder();
				serviceConnectionMap.put(connectionBinder,
						new ServiceRecord(connectionBinder, serviceInfo, Binder.getCallingPid()));
			}
			return result;
		}
		return 0;
	}

	public void setServiceForeground(ComponentName componentName, IBinder token, int id, Notification notification,
			boolean keepNotification) {
		if (componentName != null && VirtualCore.getCore().isAppInstalled(componentName.getPackageName())) {
			try {
				ActivityManagerNative.getDefault().setServiceForeground(componentName, token, id, notification,
						keepNotification);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public boolean unbindService(IServiceConnection connection) {
		if (connection == null) {
			return false;
		}
		IBinder connBinder = connection.asBinder();
		ServiceRecord r = serviceConnectionMap.get(connBinder);
		if (r == null) {
			return false;
		}
		ServiceInfo serviceInfo = r.serviceInfo;
		ProviderInfo serviceEnv = VActivityService.getService().fetchServiceRuntime(serviceInfo);
		if (serviceEnv == null) {
			return false;
		}
		IServiceEnvironment serviceEnvironment = getServiceEnvironment(serviceInfo, serviceEnv);
		if (serviceEnvironment == null) {
			return false;
		}
		try {
			return serviceEnvironment.handleUnbindService(serviceInfo, connection);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {

	}

	@Override
	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {

	}

	public void processDied(int pid) {
		for (ServiceRecord r : serviceConnectionMap.values()) {
			if (r.pid == pid) {
				serviceConnectionMap.remove(r.connection);
			}
		}
	}
}
