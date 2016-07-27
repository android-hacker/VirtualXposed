package com.lody.virtual.service.am;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.IApplicationThreadCompat;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.service.IServiceManager;
import com.lody.virtual.service.pm.VPackageService;
import com.lody.virtual.service.process.ProcessRecord;
import com.lody.virtual.service.process.VProcessService;

import java.util.ArrayList;
import java.util.List;

import static android.app.ActivityThread.SERVICE_DONE_EXECUTING_STOP;

/**
 * @author Lody
 *
 */
public class VServiceService extends IServiceManager.Stub {

	private static final String TAG = VServiceService.class.getSimpleName();

	private static final VServiceService sService = new VServiceService();

	private ArrayList<ServiceRecord> mHistory = new ArrayList<>();


	private void addRecord(ServiceRecord r) {
		mHistory.add(r);
	}

	private ServiceRecord findRecord(ServiceInfo serviceInfo) {
		for (ServiceRecord r : mHistory) {
			if (ComponentUtils.isSameComponent(serviceInfo, r.serviceInfo)) {
				return r;
			}
		}
		return null;
	}

	private ServiceRecord findRecord(IServiceConnection connection) {
		for (ServiceRecord r : mHistory) {
			if (r.containConnection(connection)) {
				return r;
			}
		}
		return null;
	}

	private ServiceRecord findRecord(IBinder token) {
		for (ServiceRecord r : mHistory) {
			if (r.token == token) {
				return r;
			}
		}
		return null;
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
			ServiceInfo serviceInfo = VirtualCore.getCore().resolveServiceInfo(service);
			if (serviceInfo != null) {
				service.setComponent(new ComponentName(serviceInfo.packageName, serviceInfo.name));
				return serviceInfo;
			}
		}
		return null;
	}



	@Override
	public ComponentName startService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		return startServiceCommon(caller, service, resolvedType, true);
	}

	private ComponentName startServiceCommon(IBinder caller, Intent service, String resolvedType, boolean scheduleServiceArgs) {
		ServiceInfo serviceInfo = getServiceInfo(service);
		if (serviceInfo == null) {
			return null;
		}
		String processName = ComponentUtils.getProcessName(serviceInfo);
		ProviderInfo env = VActivityService.getService().fetchRunningServiceRuntime(serviceInfo);
		if (env == null) {
			env = VActivityService.getService().fetchServiceRuntime(serviceInfo);
			if (env == null) {
				return null;
			} else {
				VProcessService.getService().launchComponentProcess(serviceInfo, env);
			}
		}
		ProcessRecord processRecord = VProcessService.getService().findProcess(processName);
		if (processRecord == null) {
			return null;
		}
		IApplicationThread appThread = processRecord.appThread;
		ServiceRecord r = findRecord(serviceInfo);
		if (r == null) {
			r = new ServiceRecord();
			r.startId = 0;
			r.targetAppThread = appThread;
			r.token = new Binder();
			r.serviceInfo = serviceInfo;
			IApplicationThreadCompat.scheduleCreateService(appThread, r.token, r.serviceInfo, 0);
			addRecord(r);
		} else {
			if (scheduleServiceArgs) {
				r.startId++;
				boolean taskRemoved = serviceInfo.applicationInfo != null
						&& serviceInfo.applicationInfo.targetSdkVersion < Build.VERSION_CODES.ECLAIR;
				IApplicationThreadCompat.scheduleServiceArgs(appThread, r.token, taskRemoved, r.startId, 0, service);
			}
		}
		return new ComponentName(serviceInfo.packageName, serviceInfo.name);
	}

	@Override
	public int stopService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		ServiceInfo serviceInfo = getServiceInfo(service);
		if (serviceInfo == null) {
			return 0;
		}
		ServiceRecord r = findRecord(serviceInfo);
		if (r == null) {
			return 0;
		}
		if (!r.hasSomeBound()) {
			IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
		}
		return 1;
	}

	@Override
	public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
		ServiceRecord r = findRecord(token);
		if (r == null) {
			return false;
		}
		if (r.startId == startId) {
			IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
			return true;
		}
		return false;
	}

	@Override
	public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, boolean keepNotification) throws RemoteException {

	}

	@Override
	public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags) throws RemoteException {
		ServiceInfo serviceInfo = getServiceInfo(service);
		if (serviceInfo == null) {
			return 0;
		}
		ServiceRecord r = findRecord(serviceInfo);
		if (r == null) {
			if ((flags & Context.BIND_AUTO_CREATE) != 0) {
				startServiceCommon(caller, service, resolvedType, false);
				r = findRecord(serviceInfo);
			}
		}
		if (r == null) {
			return 0;
		}
		if (r.binder != null && r.binder.isBinderAlive()) {
			if (r.doRebind) {
				IApplicationThreadCompat.scheduleBindService(r.targetAppThread, r.token, service, true, 0);
			}
			ComponentName componentName = new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name);
			try {
				connection.connected(componentName, r.binder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			IApplicationThreadCompat.scheduleBindService(r.targetAppThread, r.token, service, r.doRebind, 0);
		}
		r.addToBoundIntent(service, connection);

		return 1;
	}

	@Override
	public boolean unbindService(IServiceConnection connection) throws RemoteException {
		ServiceRecord r = findRecord(connection);
		if (r == null) {
			return false;
		}
		Intent intent = r.removedConnection(connection);
		IApplicationThreadCompat.scheduleUnbindService(r.targetAppThread, r.token, intent);
		if (r.startId <= 0 && r.getAllConnections().isEmpty()) {
			IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
		}
		return true;
	}

	@Override
	public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {
		ServiceRecord r = findRecord(token);
		if (r != null) {
			r.doRebind = doRebind;
		}
	}

	@Override
	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
		ServiceRecord r = findRecord(token);
		if (r == null) {
			return;
		}
		if (SERVICE_DONE_EXECUTING_STOP == type) {
			mHistory.remove(r);
		}
	}

	@Override
	public IBinder peekService(Intent service, String resolvedType) throws RemoteException {
		ServiceInfo serviceInfo = getServiceInfo(service);
		if (serviceInfo == null) {
			return null;
		}
		ServiceRecord r = findRecord(serviceInfo);
		if (r != null) {
			return r.token;
		}
		return null;
	}

	@Override
	public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
		ServiceRecord r = findRecord(token);
		if (r == null) {
			return;
		}
		List<IServiceConnection> allConnections = r.getAllConnections();
		if (allConnections.isEmpty()) {
			return;
		}
		for (IServiceConnection connection : allConnections) {
			if (connection.asBinder().isBinderAlive()) {
				ComponentName componentName = new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name);
				try {
					connection.connected(componentName, service);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				allConnections.remove(connection);
			}
		}
	}


	public void processDied(ProcessRecord record) {
		for (ServiceRecord r : mHistory) {
			if (ComponentUtils.getProcessName(r.serviceInfo).equals(record.appProcessName)) {
				for (IServiceConnection connection : r.getAllConnections()) {
					try {
						unbindService(connection);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				mHistory.remove(r);
			}
		}
	}
}
