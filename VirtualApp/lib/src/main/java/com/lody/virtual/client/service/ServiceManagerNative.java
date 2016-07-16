package com.lody.virtual.client.service;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.utils.XLog;
import com.lody.virtual.service.interfaces.IServiceFetcher;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Lody
 *
 */
public class ServiceManagerNative {

	public static final String PACKAGE_MANAGER = "x-package-manager";
	public static final String ACTIVITY_MANAGER = "x-activity-manager";
	public static final String PLUGIN_MANAGER = "x-plugin-manager";
	public static final String PROCESS_MANAGER = "x-process-manager";
	public static final String SERVICE_MANAGER = "x-service-manager";
	public static final String CONTENT_MANAGER = "x-content—manager";
	private static final String TAG = ServiceManagerNative.class.getSimpleName();
	private static final String SERVICE_CP_AUTH = "virtual.service.BinderProvider";

	public static IServiceFetcher getServiceFetcher() {
		Context context = VirtualCore.getCore().getContext();
		Bundle response = new ProviderCaller.Builder(context, SERVICE_CP_AUTH).methodName("@").call();
		if (response != null) {
			IBinder binder = BundleCompat.getBinder(response, ExtraConstants.EXTRA_BINDER);
			return IServiceFetcher.Stub.asInterface(binder);
		}
		return null;
	}

	public static IBinder getService(String name) {
		IServiceFetcher fetcher = getServiceFetcher();
		if (fetcher != null) {
			try {
				return fetcher.getService(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		XLog.e(TAG, "GetService(%s) return null.", name);
		return null;
	}

	public static void addService(String name, IBinder service) {
		IServiceFetcher fetcher = getServiceFetcher();
		if (fetcher != null) {
			try {
				fetcher.addService(name, service);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	public static void removeService(String name) {
		IServiceFetcher fetcher = getServiceFetcher();
		if (fetcher != null) {
			try {
				fetcher.removeService(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public static void startup(Context context) {
		new ProviderCaller.Builder(context, SERVICE_CP_AUTH).methodName("startup").call();
	}

}
