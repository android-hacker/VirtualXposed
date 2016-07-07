package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.IUserManager;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookUserBinder extends HookBinder<IUserManager> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService("user");
	}

	@Override
	protected IUserManager createInterface(IBinder baseBinder) {
		return IUserManager.Stub.asInterface(baseBinder);
	}
}
