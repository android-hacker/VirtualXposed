package com.lody.virtual.service;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.helper.proto.VComponentInfo;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.Reflect;

import java.util.List;

/**
 * @author Lody
 *
 * 存在BUG,先不使用。
 *
 */
public class VServiceServiceImpl_uncompleted extends IServiceManager.Stub {

	private static final String TAG = VServiceServiceImpl_uncompleted.class.getSimpleName();

	private static final VServiceServiceImpl_uncompleted sService = new VServiceServiceImpl_uncompleted();
	/**
	 * Type for IActivityManager.serviceDoneExecuting: anonymous operation
	 */
	private static final int SERVICE_DONE_EXECUTING_ANON = 0;
	/**
	 * Type for IActivityManager.serviceDoneExecuting: done with an onStart call
	 */
	private static final int SERVICE_DONE_EXECUTING_START = 1;
	/**
	 * Type for IActivityManager.serviceDoneExecuting: done stopping
	 * (destroying) service
	 */
	private static final int SERVICE_DONE_EXECUTING_STOP = 2;

	public static VServiceServiceImpl_uncompleted getService() {
		return sService;
	}

	private static VPackageServiceImpl getPM() {
		return VPackageServiceImpl.getService();
	}

	private static boolean taskRemoved(ServiceInfo targetInfo) {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR || targetInfo.applicationInfo != null
				&& targetInfo.applicationInfo.targetSdkVersion < Build.VERSION_CODES.ECLAIR;
	}

	@Override
	public ComponentName startService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		return handleStartService(caller, service, resolvedType, true);
	}

	private ComponentName handleStartService(IBinder caller, Intent service, String resolvedType,
			boolean shouldScheduleServiceArgs) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ResolveInfo resolveInfo = getPM().resolveService(service, resolvedType, 0);
		if (resolveInfo == null || resolveInfo.serviceInfo == null) {
			// 没有查询到Service信息
			return null;
		}
		ServiceInfo serviceInfo = resolveInfo.serviceInfo;
		String processName = ComponentUtils.getProcessName(serviceInfo);
		// 查询进程记录
		ProcessRecord processRecord = processService.findRecord(processName);
		if (processRecord == null) {
			// 目标进程还未启动, 先启动目标进程
			processService.installComponent(VComponentInfo.wrap(serviceInfo));
			processRecord = processService.findRecord(processName);
			if (processRecord == null) {
				// 尝试启动进程了,但是进程还是没有启动,可能因为某些原因挂了
				return null;
			}
		}
		IApplicationThread appThread = processRecord.appThread;
		ServiceRecord record = processRecord.findServiceRecord(serviceInfo.name);
		if (record == null) {
			record = new ServiceRecord();
			record.serviceInfo = serviceInfo;
			record.startId = 0;
			record.token = new Binder();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				appThread.scheduleCreateService(record.token, record.serviceInfo,
						CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, 0);
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				Reflect.on(appThread).call("scheduleCreateService", record.token, record.serviceInfo,
						CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
			} else {
				Reflect.on(appThread).call("scheduleCreateService", record.token, record.serviceInfo);
			}
			processRecord.addServiceRecord(record);
		}
		if (shouldScheduleServiceArgs) {
			record.startId++;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				appThread.scheduleServiceArgs(record.token, taskRemoved(serviceInfo), record.startId, 0, service);
			} else {
				Reflect.on(appThread).call("scheduleServiceArgs", record.token, record.startId, 0, service);
			}
		}
		return new ComponentName(serviceInfo.packageName, serviceInfo.name);
	}

	@Override
	public int stopService(IBinder caller, Intent service, String resolvedType) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ResolveInfo resolveInfo = getPM().resolveService(service, resolvedType, 0);
		if (resolveInfo == null || resolveInfo.serviceInfo == null) {
			// 没有查询到Service信息
			return 0;
		}
		ServiceInfo serviceInfo = resolveInfo.serviceInfo;
		String processName = ComponentUtils.getProcessName(serviceInfo);
		// 查询进程记录
		ProcessRecord processRecord = processService.findRecord(processName);
		if (processRecord == null) {
			return 0;
		}
		ServiceRecord record = processRecord.findServiceRecord(serviceInfo.name);
		if (record == null) {
			return 0;
		}
		if (!record.hasSomeBound()) {
			processRecord.appThread.scheduleStopService(record.token);
		}
		return 1;
	}

	@Override
	public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ServiceRecord r = processService.queryServiceRecord(token);
		if (r == null) {
			return false;
		}
		if (r.startId == startId) {
			String processName = ComponentUtils.getProcessName(r.serviceInfo);
			ProcessRecord processRecord = processService.findRecord(processName);
			processRecord.appThread.scheduleStopService(token);
		}
		return false;
	}

	@Override
	public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification,
			boolean keepNotification) throws RemoteException {
		// 暂时先不实现了,不碍事
	}

	@Override
	public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType,
			IServiceConnection connection, int flags) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ResolveInfo resolveInfo = getPM().resolveService(service, resolvedType, 0);
		if (resolveInfo == null || resolveInfo.serviceInfo == null) {
			// 没有查询到Service信息
			return 0;
		}
		ServiceInfo serviceInfo = resolveInfo.serviceInfo;
		String processName = ComponentUtils.getProcessName(serviceInfo);
		// 查询进程记录
		ProcessRecord processRecord = processService.findRecord(processName);
		ServiceRecord record = null;
		if (processRecord != null) {
			record = processRecord.findServiceRecord(serviceInfo.name);
		}
		if (record == null) {
			if ((flags & Context.BIND_AUTO_CREATE) != 0) {
				handleStartService(caller, service, resolvedType, false);
				processRecord = processService.findRecord(processName);
				if (processRecord == null) {
					return 0;
				}
			} else {
				return 0;
			}
		}
		if (record == null) {
			record = processRecord.findServiceRecord(serviceInfo.name);
		}
		if (record == null) {
			return 0;
		}
		IApplicationThread appThread = processRecord.appThread;
		// 此时目标Service已经onCreate()调用完成
		if (record.binder != null && record.binder.isBinderAlive()) {
			if (record.doRebind) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					appThread.scheduleBindService(record.token, service, true, 0);
				} else {
					Reflect.on(appThread).call("scheduleBindService", record.token, service, record.doRebind);
				}
			}
			ComponentName cn = new ComponentName(record.serviceInfo.packageName, record.serviceInfo.name);
			try {
				connection.connected(cn, record.binder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				appThread.scheduleBindService(record.token, service, true, 0);
			} else {
				Reflect.on(appThread).call("scheduleBindService", record.token, service, record.doRebind);
			}
		}
		record.addToBoundIntent(service, connection);
		return 1;
	}

	@Override
	public boolean unbindService(IServiceConnection connection) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ServiceRecord r = processService.findServiceRecord(connection);
		if (r == null) {
			return false;
		}
		Intent intent = r.removedConnection(connection);
		String processName = ComponentUtils.getProcessName(r.serviceInfo);
		ProcessRecord processRecord = processService.findRecord(processName);
		if (processRecord == null) {
			return false;
		}
		IApplicationThread appThread = processRecord.appThread;
		appThread.scheduleUnbindService(r.token, intent);
		if (r.startId <= 0 && !r.hasConnection()) {
			appThread.scheduleStopService(r.token);
		}
		return true;
	}

	@Override
	public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {
		ServiceRecord record = VProcessServiceImpl.getService().queryServiceRecord(token);
		if (record != null) {
			record.doRebind = doRebind;
		}
	}

	@Override
	public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ServiceRecord record = processService.queryServiceRecord(token);
		if (record == null) {
			return;
		}

		if (type == SERVICE_DONE_EXECUTING_ANON) {
			// Nothing to do
		} else if (type == SERVICE_DONE_EXECUTING_START) {
			switch (res) {
				case Service.START_STICKY_COMPATIBILITY :
				case Service.START_STICKY : {
					break;
				}
				case Service.START_NOT_STICKY : {
					break;
				}
				case Service.START_REDELIVER_INTENT : {
					break;
				}
				case Service.START_TASK_REMOVED_COMPLETE : {
					break;
				}
				default :
					throw new IllegalArgumentException("Unknown service start result: " + res);
			}
		} else if (type == SERVICE_DONE_EXECUTING_STOP) {
			String processName = ComponentUtils.getProcessName(record.serviceInfo);
			ProcessRecord processRecord = processService.findRecord(processName);
			if (processRecord != null) {
				processRecord.removeServiceRecord(record.serviceInfo);
			}
		}
	}

	@Override
	public IBinder peekService(Intent service, String resolvedType) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ResolveInfo resolveInfo = getPM().resolveService(service, resolvedType, 0);
		if (resolveInfo == null || resolveInfo.serviceInfo == null) {
			// 没有查询到Service信息
			return null;
		}
		ServiceInfo serviceInfo = resolveInfo.serviceInfo;
		String processName = ComponentUtils.getProcessName(serviceInfo);
		// 查询进程记录
		ProcessRecord processRecord = processService.findRecord(processName);
		if (processRecord == null) {
			return null;
		}
		ServiceRecord record = processRecord.findServiceRecord(serviceInfo.name);
		if (record == null) {
			return null;
		}
		return record.binder;
	}

	@Override
	public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
		VProcessServiceImpl processService = VProcessServiceImpl.getService();
		ServiceRecord r = processService.queryServiceRecord(token);
		if (r == null) {
			return;
		}
		r.binder = service;
		List<IServiceConnection> allConnections = r.getAllConnections();
		if (allConnections.size() > 0) {
			for (IServiceConnection conn : allConnections) {
				if (conn.asBinder().isBinderAlive()) {
					ComponentName cn = new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name);
					try {
						conn.connected(cn, service);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// Remove the died connection
					r.removedConnection(conn);
				}
			}
		}
	}

}
