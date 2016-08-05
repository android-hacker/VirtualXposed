package com.lody.virtual.client.local;

import java.util.List;

import com.lody.virtual.client.env.RuntimeEnv;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.service.IServiceManager;

import android.app.ActivityManager;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Lody
 *
 */
public class LocalServiceManager {

	private static final LocalServiceManager sService = new LocalServiceManager();
	private IServiceManager service;

	public static LocalServiceManager getInstance() {
		return sService;
	}

	public IServiceManager getService() {
		if (service == null) {
			synchronized (LocalServiceManager.class) {
				if (service == null) {
					IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.SERVICE_MANAGER);
					service = IServiceManager.Stub.asInterface(binder);
				}
			}
		}
		return service;
	}

	public ComponentName startService(IBinder caller, Intent service, String resolvedType) {
		try {
			return getService().startService(caller, service, resolvedType);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public int stopService(IBinder caller, Intent service, String resolvedType) {
		try {
			return getService().stopService(caller, service, resolvedType);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public boolean stopServiceToken(ComponentName className, IBinder token, int startId) {
		try {
			return getService().stopServiceToken(className, token, startId);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification,
			boolean keepNotification) {
		try {
			getService().setServiceForeground(className, token, id, notification, keepNotification);
		} catch (RemoteException e) {
			RuntimeEnv.crash(e);
		}
	}

	public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType,
			IServiceConnection connection, int flags) {
		try {
			return getService().bindService(caller, token, service, resolvedType, connection, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public boolean unbindService(IServiceConnection connection) {
		try {
			return getService().unbindService(connection);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public IBinder peekService(Intent service, String resolvedType) {
		try {
			return getService().peekService(service, resolvedType);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public void publishService(IBinder token, Intent intent, IBinder service) {
		try {
			getService().publishService(token, intent, service);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void unbindFinished(IBinder token, Intent service, boolean doRebind) {
		try {
			getService().unbindFinished(token, service, doRebind);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
		try {
			getService().serviceDoneExecuting(token, type, startId, res);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) {
		try {
			// noinspection unchecked
			return getService().getServices(maxNum, flags).getList();
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}
}
