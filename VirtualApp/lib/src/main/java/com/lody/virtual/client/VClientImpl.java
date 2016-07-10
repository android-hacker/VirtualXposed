package com.lody.virtual.client;

import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;

import java.util.Map;

/**
 * @author Lody
 */

public class VClientImpl extends IVClient.Stub {

	private static final IVClient gClient = new VClientImpl();
	private Map<IBinder, Object> activities;

	public static IVClient getClient() {
		return gClient;
	}

	@Override
	public IBinder getAppThread() throws RemoteException {
		return VirtualCore.mainThread().getApplicationThread();
	}

}
