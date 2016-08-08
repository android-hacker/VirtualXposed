package com.lody.virtual.service;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.client.stub.KeepService;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.MethodConstants;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.component.BaseContentProvider;
import com.lody.virtual.service.account.VAccountManagerService;
import com.lody.virtual.service.am.VActivityManagerService;
import com.lody.virtual.service.filter.IntentFilterService;
import com.lody.virtual.service.interfaces.IServiceFetcher;
import com.lody.virtual.service.pm.VAppManagerService;
import com.lody.virtual.service.pm.VPackageManagerService;

/**
 * @author Lody
 */
public final class BinderProvider extends BaseContentProvider {

	private final ServiceFetcher mServiceFetcher = new ServiceFetcher();

	@Override
	public boolean onCreate() {
		Context context = getContext();
		KeepService.startup(context);
		if (!VirtualCore.getCore().isStartup()) {
			return true;
		}
		AppFileSystem.getDefault();
		VPackageManagerService.systemReady();
		addService(ServiceManagerNative.PACKAGE_MANAGER, VPackageManagerService.getService());
		VAppManagerService.systemReady();
		addService(ServiceManagerNative.APP_MANAGER, VAppManagerService.getService());
		VActivityManagerService.systemReady(context);
		addService(ServiceManagerNative.ACTIVITY_MANAGER, VActivityManagerService.getService());
		VAccountManagerService.systemReady(context);
		addService(ServiceManagerNative.ACCOUNT_MANAGER, VAccountManagerService.getSingleton());
		addService(ServiceManagerNative.INTENT_FILTER_MANAGER, IntentFilterService.getService());
		return true;
	}

	private void addService(String name, IBinder service) {
		ServiceCache.addService(name, service);
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		if (method.equals(MethodConstants.INIT_SERVICE)) {
			// Ensure the server process created.
			return null;
		} else {
			Bundle bundle = new Bundle();
			BundleCompat.putBinder(bundle, ExtraConstants.EXTRA_BINDER, mServiceFetcher);
			return bundle;
		}
	}

	private class ServiceFetcher extends IServiceFetcher.Stub {
		@Override
		public IBinder getService(String name) throws RemoteException {
			if (name != null) {
				return ServiceCache.getService(name);
			}
			return null;
		}

		@Override
		public void addService(String name, IBinder service) throws RemoteException {
			if (name != null && service != null) {
				ServiceCache.addService(name, service);
			}
		}

		@Override
		public void removeService(String name) throws RemoteException {
			if (name != null) {
				ServiceCache.removeService(name);
			}
		}
	}

}
