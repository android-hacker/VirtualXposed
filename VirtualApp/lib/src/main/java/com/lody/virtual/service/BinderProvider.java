package com.lody.virtual.service;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.client.stub.KeepService;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.component.BaseContentProvider;
import com.lody.virtual.helper.utils.XLog;
import com.lody.virtual.service.interfaces.IServiceFetcher;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 *
 */
public final class BinderProvider extends BaseContentProvider {

	private final Map<String, IBinder> mServices = new HashMap<String, IBinder>();

	private final ServiceFetcher mServiceFetcher = new ServiceFetcher();

	@Override
	public boolean onCreate() {
		Context context = getContext();
		KeepService.startup(context);
		if (!VirtualCore.getCore().isStartup()) {
			return true;
		}
		AppFileSystem.getDefault();
		VAppServiceImpl.getService().onCreate();
		addService(ServiceManagerNative.PLUGIN_MANAGER, VAppServiceImpl.getService());
		addService(ServiceManagerNative.PROCESS_MANAGER, VProcessServiceImpl.getService());
		VPackageServiceImpl.getService().onCreate(context);
		addService(ServiceManagerNative.PACKAGE_MANAGER, VPackageServiceImpl.getService());
		VActivityServiceImpl.getService().onCreate(context);
		addService(ServiceManagerNative.ACTIVITY_MANAGER, VActivityServiceImpl.getService());
		addService(ServiceManagerNative.SERVICE_MANAGER, VServiceServiceImpl.getService());
		addService(ServiceManagerNative.CONTENT_MANAGER, VContentServiceImpl.getService());
		return true;
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		if (method.equals("startup")) {
			// 确保ServiceContentProvider所在进程创建，因为一切插件服务都依赖这个桥梁。
			return null;
		} else {
			Bundle bundle = new Bundle();
			BundleCompat.putBinder(bundle, ExtraConstants.EXTRA_BINDER, mServiceFetcher);
			return bundle;
		}
	}

	private void addService(String name, IBinder service) {
		mServices.put(name, service);
	}

	private class ServiceFetcher extends IServiceFetcher.Stub {
		@Override
		public IBinder getService(String name) throws RemoteException {
			if (name != null) {
				return mServices.get(name);
			}
			return null;
		}

		@Override
		public void addService(String name, IBinder service) throws RemoteException {
			if (name != null && service != null) {
				addService(name, service);
			}
		}

		@Override
		public void removeService(String name) throws RemoteException {
			if (name != null) {
				mServices.remove(name);
			}
		}
	}

}
