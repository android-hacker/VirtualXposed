package com.lody.virtual.service.am;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.IApplicationThreadCompat;
import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IServiceManager;
import com.lody.virtual.service.process.ProcessRecord;
import com.lody.virtual.service.process.VProcessService;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static android.app.ActivityThread.SERVICE_DONE_EXECUTING_STOP;

/**
 * @author Lody
 *
 */
public class VServiceService extends IServiceManager.Stub {

	private static final String TAG = VServiceService.class.getSimpleName();
	private static final VServiceService sService = new VServiceService();
	private final List<ServiceRecord> mHistory = new ArrayList<ServiceRecord>();

	public static VServiceService getService() {
		return sService;
	}

	private static ServiceInfo resolveServiceInfo(Intent service) {
		if (service != null) {
			ServiceInfo serviceInfo = VirtualCore.getCore().resolveServiceInfo(service);
			if (serviceInfo != null) {
				return serviceInfo;
			}
		}
		return null;
	}

	private void addRecord(ServiceRecord r) {
		mHistory.add(r);
	}

	private ServiceRecord findRecord(ServiceInfo serviceInfo) {
		synchronized (mHistory) {
			for (ServiceRecord r : mHistory) {
				if (ComponentUtils.isSameComponent(serviceInfo, r.serviceInfo)) {
					return r;
				}
			}
			return null;
		}
	}

	private ServiceRecord findRecord(IServiceConnection connection) {
		synchronized (mHistory) {
			for (ServiceRecord r : mHistory) {
				if (r.containConnection(connection)) {
					return r;
				}
			}
			return null;
		}
	}

	private ServiceRecord findRecord(IBinder token) {
		synchronized (mHistory) {
			for (ServiceRecord r : mHistory) {
				if (r.token == token) {
					return r;
				}
			}
			return null;
		}
	}

	@Override
	public ComponentName startService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		synchronized (this) {
			return startServiceCommon(caller, service, resolvedType, true);
		}
	}

	private ComponentName startServiceCommon(IBinder caller, Intent service, String resolvedType,
			boolean scheduleServiceArgs) {
		ServiceInfo serviceInfo = resolveServiceInfo(service);
		if (serviceInfo == null) {
			return null;
		}
		ProcessRecord processRecord = VProcessService.getService()
				.startProcess(ComponentUtils.getProcessName(serviceInfo), serviceInfo.applicationInfo);
		if (processRecord == null) {
			VLog.e(TAG, "Unable to start new Process for : " + ComponentUtils.toComponentName(serviceInfo));
			return null;
		}
		IApplicationThread appThread = processRecord.thread;
		ServiceRecord r = findRecord(serviceInfo);
		if (r == null) {
			r = new ServiceRecord();
			r.pid = processRecord.pid;
			r.startId = 0;
			r.activeSince = SystemClock.elapsedRealtime();
			r.targetAppThread = appThread;
			r.token = new Binder();
			r.serviceInfo = serviceInfo;
			IApplicationThreadCompat.scheduleCreateService(appThread, r.token, r.serviceInfo, 0);
			addRecord(r);
		}
		r.lastActivityTime = SystemClock.uptimeMillis();
		if (scheduleServiceArgs) {
			r.startId++;
			boolean taskRemoved = serviceInfo.applicationInfo != null
					&& serviceInfo.applicationInfo.targetSdkVersion < Build.VERSION_CODES.ECLAIR;
			IApplicationThreadCompat.scheduleServiceArgs(appThread, r.token, taskRemoved, r.startId, 0, service);
		}
		return new ComponentName(serviceInfo.packageName, serviceInfo.name);
	}

	@Override
	public int stopService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		synchronized (this) {
			ServiceInfo serviceInfo = resolveServiceInfo(service);
			if (serviceInfo == null) {
				return 0;
			}
			ServiceRecord r = findRecord(serviceInfo);
			if (r == null) {
				return 0;
			}
			if (!r.hasSomeBound()) {
				IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					mHistory.remove(r);
				}
			}
			return 1;
		}
	}

	@Override
	public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
		synchronized (this) {
			ServiceRecord r = findRecord(token);
			if (r == null) {
				return false;
			}
			if (r.startId == startId) {
				IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					mHistory.remove(r);
				}
				return true;
			}
			return false;
		}
	}

	@Override
	public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification,
			boolean keepNotification) throws RemoteException {

	}

	@Override
	public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType,
			IServiceConnection connection, int flags) throws RemoteException {
		synchronized (this) {
			ServiceInfo serviceInfo = resolveServiceInfo(service);
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
			r.lastActivityTime = SystemClock.uptimeMillis();
			r.addToBoundIntent(service, connection);

			return 1;
		}
	}

	@Override
	public boolean unbindService(IServiceConnection connection) throws RemoteException {
		synchronized (this) {
			ServiceRecord r = findRecord(connection);
			if (r == null) {
				return false;
			}
			Intent intent = r.removedConnection(connection);
			IApplicationThreadCompat.scheduleUnbindService(r.targetAppThread, r.token, intent);
			if (r.startId <= 0 && r.getAllConnections().isEmpty()) {
				IApplicationThreadCompat.scheduleStopService(r.targetAppThread, r.token);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					mHistory.remove(r);
				}
			}
			return true;
		}
	}

	@Override
	public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {
		synchronized (this) {
			ServiceRecord r = findRecord(token);
			if (r != null) {
				r.doRebind = doRebind;
			}
		}
	}

	@Override
	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
		synchronized (this) {
			ServiceRecord r = findRecord(token);
			if (r == null) {
				return;
			}
			if (SERVICE_DONE_EXECUTING_STOP == type) {
				mHistory.remove(r);
			}
		}
	}

	@Override
	public IBinder peekService(Intent service, String resolvedType) throws RemoteException {
		synchronized (this) {
			ServiceInfo serviceInfo = resolveServiceInfo(service);
			if (serviceInfo == null) {
				return null;
			}
			ServiceRecord r = findRecord(serviceInfo);
			if (r != null) {
				return r.token;
			}
			return null;
		}
	}

	@Override
	public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
		synchronized (this) {
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
	}

	public void processDied(ProcessRecord record) {
		synchronized (mHistory) {
			ListIterator<ServiceRecord> iterator = mHistory.listIterator();
			while (iterator.hasNext()) {
				ServiceRecord r = iterator.next();
				if (ComponentUtils.getProcessName(r.serviceInfo).equals(record.processName)) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	public VParceledListSlice<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) {
		synchronized (mHistory) {
			int myUid = Process.myUid();
			List<ActivityManager.RunningServiceInfo> services = new ArrayList<>(mHistory.size());
			for (ServiceRecord r : mHistory) {
				ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
				info.uid = myUid;
				info.pid = r.pid;
				ProcessRecord processRecord = VProcessService.getService().findProcess(r.pid);
				if (processRecord != null) {
					info.process = processRecord.processName;
					info.clientPackage = processRecord.info.packageName;
				}
				info.activeSince = r.activeSince;
				info.lastActivityTime = r.lastActivityTime;
				info.clientCount = r.getClientCount();
				info.service = ComponentUtils.toComponentName(r.serviceInfo);
				info.started = r.startId > 0;
			}
			return new VParceledListSlice<>(services);
		}
	}
}
