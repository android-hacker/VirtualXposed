package com.lody.virtual.service.filter;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.service.interfaces.IIntentFilterObserver;

/**
 * Class: Created by andy on 16-8-2. TODO:
 */
public class IntentFilterService extends IIntentFilterObserver.Stub {
	public static IntentFilterService sService = new IntentFilterService();
	public IIntentFilterObserver callBack;

	public static IntentFilterService getService() {
		return sService;
	}

	public static IIntentFilterObserver getService(IBinder binder) {
		if (binder == null)
			return null;
		else
			return IIntentFilterObserver.Stub.asInterface(binder);
	}

	@Override
	public Intent filter(Intent intent) throws RemoteException {
		return this.callBack != null ? this.callBack.filter(intent) : intent;
	}

	@Override
	public IBinder getCallBack() throws RemoteException {
		if (this.callBack != null)
			return this.callBack.asBinder();
		return null;
	}

	@Override
	public void setCallBack(IBinder callBack) throws RemoteException {
		if (callBack != null)
			this.callBack = IIntentFilterObserver.Stub.asInterface(callBack);
	}
}
