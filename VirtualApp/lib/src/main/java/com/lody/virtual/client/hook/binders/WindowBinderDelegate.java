package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.android.view.IWindowManager;

/**
 * @author Lody
 *
 */
public class WindowBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.WINDOW_SERVICE);
		return IWindowManager.Stub.asInterface.call(binder);
	}
}
