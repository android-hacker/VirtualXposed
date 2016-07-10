package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.accounts.IAccountManager;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookAccountBinder extends HookBinder<IAccountManager> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.ACCOUNT_SERVICE);
	}

	@Override
	protected IAccountManager createInterface(IBinder baseBinder) {
		return IAccountManager.Stub.asInterface(baseBinder);
	}
}
