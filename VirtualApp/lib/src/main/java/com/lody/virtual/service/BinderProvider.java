package com.lody.virtual.service;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.client.stub.KeepService;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.component.BaseContentProvider;
import com.lody.virtual.service.accounts.VAccountManagerService;
import com.lody.virtual.service.am.VActivityManagerService;
import com.lody.virtual.service.filter.IntentFilterService;
import com.lc.interceptor.service.VInterceptorService;
import com.lody.virtual.service.interfaces.IServiceFetcher;
import com.lody.virtual.service.pm.VAppManagerService;
import com.lody.virtual.service.pm.VPackageManagerService;
import com.lody.virtual.service.pm.VUserManagerService;

/**
 * @author Lody
 */
public final class BinderProvider extends BaseContentProvider {

	private final ServiceFetcher mServiceFetcher = new ServiceFetcher();

	@Override
	public boolean onCreate() {
		Context context = getContext();
		KeepService.startup(context);
		if (!VirtualCore.get().isStartup()) {
			return true;
		}
		VPackageManagerService.systemReady();
		addService(ServiceManagerNative.PACKAGE_MANAGER, VPackageManagerService.get());
		VActivityManagerService.systemReady(context);
		addService(ServiceManagerNative.ACTIVITY_MANAGER, VActivityManagerService.get());
		addService(ServiceManagerNative.USER_MANAGER, VUserManagerService.get());
		VAppManagerService.systemReady();
		addService(ServiceManagerNative.APP_MANAGER, VAppManagerService.get());
		VAccountManagerService.systemReady();
		addService(ServiceManagerNative.ACCOUNT_MANAGER, VAccountManagerService.get());
		addService(ServiceManagerNative.INTENT_FILTER_MANAGER, IntentFilterService.get());
        addService(ServiceManagerNative.INTERCEPTOR_SERVICE, VInterceptorService.get());
		return true;
	}

	private void addService(String name, IBinder service) {
		ServiceCache.addService(name, service);
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		Bundle bundle = new Bundle();
		BundleCompat.putBinder(bundle, "_VA_|_binder_", mServiceFetcher);
		return bundle;
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
