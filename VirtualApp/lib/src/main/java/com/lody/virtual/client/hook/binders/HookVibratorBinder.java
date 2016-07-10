package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.os.IBinder;
import android.os.IVibratorService;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookVibratorBinder extends HookBinder<IVibratorService> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.VIBRATOR_SERVICE);
	}

	@Override
	protected IVibratorService createInterface(IBinder baseBinder) {
		return IVibratorService.Stub.asInterface(baseBinder);
	}
}
