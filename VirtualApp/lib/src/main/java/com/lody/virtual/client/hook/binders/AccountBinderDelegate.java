package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.accounts.IAccountManager;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class AccountBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.ACCOUNT_SERVICE);
		return IAccountManager.Stub.asInterface.call(binder);
	}
}
