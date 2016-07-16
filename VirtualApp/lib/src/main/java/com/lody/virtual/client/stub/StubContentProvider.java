package com.lody.virtual.client.stub;

import android.app.IServiceConnection;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.ServiceEnv;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.component.BaseContentProvider;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.service.interfaces.IServiceEnvironment;

/**
 * @author Lody
 *
 */
public abstract class StubContentProvider extends BaseContentProvider {

	private final ServiceEnvBinder mServiceEnvBinder = new ServiceEnvBinder();

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		if (method.equals(MethodConstants.INIT_PROCESS)) {
			VirtualCore core = VirtualCore.getCore();
			try {
				while (core.getApplication() == null) {
					Thread.sleep(50);
				}
				while (AppSandBox.isInstalling()) {
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			Bundle bundle = new Bundle();
			BundleCompat.putBinder(bundle, ExtraConstants.EXTRA_BINDER, mServiceEnvBinder);
			return bundle;
		}
	}

	private static class ServiceEnvBinder extends IServiceEnvironment.Stub {

		@Override
		public void handleStartService(Intent intent, ServiceInfo serviceInfo) throws RemoteException {
			AppInfo appInfo = VirtualCore.getCore().findApp(serviceInfo.packageName);
			if (appInfo != null) {
				ServiceEnv.getEnv().handleStartService(appInfo, intent, serviceInfo);
			}
		}

		@Override
		public int handleStopService(ServiceInfo serviceInfo) throws RemoteException {
			if (serviceInfo != null) {
				AppInfo appInfo = VirtualCore.getCore().findApp(serviceInfo.packageName);
				if (appInfo != null) {
					ServiceEnv.getEnv().handleStopService(appInfo, serviceInfo);
					return 1;
				}
			}
			return 0;
		}

		@Override
		public boolean handleStopServiceToken(IBinder token, ServiceInfo serviceInfo, int startId)
				throws RemoteException {
			if (serviceInfo != null) {
				AppInfo appInfo = VirtualCore.getCore().findApp(serviceInfo.packageName);
				if (appInfo != null) {
					ServiceEnv.getEnv().handleStopServiceToken(appInfo, serviceInfo, startId);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean handleUnbindService(ServiceInfo serviceInfo, IServiceConnection connection)
				throws RemoteException {
			if (serviceInfo != null && connection != null) {
				ServiceEnv.getEnv().handleUnbindService(connection);
				return true;
			}
			return false;
		}

		@Override
		public int handleBindService(IBinder token, Intent service, ServiceInfo serviceInfo,
				IServiceConnection connection) throws RemoteException {
			AppInfo appInfo = VirtualCore.getCore().findApp(serviceInfo.packageName);
			if (appInfo != null) {
				ServiceEnv.getEnv().handleBindService(appInfo, service, serviceInfo, connection);
				return 1;
			}
			return 0;
		}
	}

	public static class C0 extends StubContentProvider {
	}

	public static class C1 extends StubContentProvider {
	}

	public static class C2 extends StubContentProvider {
	}

	public static class C3 extends StubContentProvider {
	}

	public static class C4 extends StubContentProvider {
	}

	public static class C5 extends StubContentProvider {
	}

	public static class C6 extends StubContentProvider {
	}

	public static class C7 extends StubContentProvider {
	}

	public static class C8 extends StubContentProvider {
	}

	public static class C9 extends StubContentProvider {
	}

	public static class C10 extends StubContentProvider {
	}

	public static class C11 extends StubContentProvider {
	}

	public static class C12 extends StubContentProvider {
	}

	public static class C13 extends StubContentProvider {
	}

	public static class C14 extends StubContentProvider {
	}

	public static class C15 extends StubContentProvider {
	}

	public static class C16 extends StubContentProvider {
	}

	public static class C17 extends StubContentProvider {
	}

	public static class C18 extends StubContentProvider {
	}

	public static class C19 extends StubContentProvider {
	}

	public static class C20 extends StubContentProvider {
	}

}
