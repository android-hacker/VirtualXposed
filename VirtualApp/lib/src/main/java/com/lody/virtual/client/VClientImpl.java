package com.lody.virtual.client;

import com.lody.virtual.client.core.VirtualCore;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Lody
 */

public class VClientImpl extends IVClient.Stub {

	private static final IVClient gClient = new VClientImpl();

	public static IVClient getClient() {
		return gClient;
	}

	@Override
	public IBinder getAppThread() throws RemoteException {
		return VirtualCore.mainThread().getApplicationThread();
	}

}
