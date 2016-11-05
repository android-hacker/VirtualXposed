package com.lody.virtual.client.ipc;

import android.os.IBinder;

import com.lody.virtual.service.interfaces.IIntentFilterObserver;

/**
 * @author Lody
 */

public class VIntentFilterManager {
	private static IIntentFilterObserver mRemote;

	public static IIntentFilterObserver getInterface() {
		if (mRemote == null) {
			synchronized (VIntentFilterManager.class) {
				if (mRemote == null) {
					IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.INTENT_FILTER);
					IIntentFilterObserver remote = IIntentFilterObserver.Stub.asInterface(binder);
					mRemote = LocalProxyUtils.genProxy(IIntentFilterObserver.class, remote);
				}
			}
		}
		return mRemote;
	}
}
