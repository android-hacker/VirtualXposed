package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.os.IVibratorService;

/**
 * @author Lody
 *
 */
public class VibratorBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.VIBRATOR_SERVICE);
		return IVibratorService.Stub.asInterface.call(binder);
	}
}
