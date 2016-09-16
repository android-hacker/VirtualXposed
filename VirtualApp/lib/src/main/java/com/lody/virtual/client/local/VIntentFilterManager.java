package com.lody.virtual.client.local;

import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.service.interfaces.IIntentFilterObserver;

import android.os.IBinder;

/**
 * @author Lody
 */

public class VIntentFilterManager {
	private static IIntentFilterObserver mRemote;

	public static IIntentFilterObserver getInterface() {
		if (mRemote == null) {
			synchronized (VIntentFilterManager.class) {
				if (mRemote == null) {
					IBinder remote = ServiceManagerNative.getService(ServiceManagerNative.INTENT_FILTER);
					mRemote = IIntentFilterObserver.Stub.asInterface(remote);
				}
			}
		}
		return mRemote;
	}
}
