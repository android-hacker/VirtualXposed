package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.app.IAlarmManager;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookAlarmBinder extends HookBinder<IAlarmManager> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.ALARM_SERVICE);
	}

	@Override
	protected IAlarmManager createInterface(IBinder baseBinder) {
		return IAlarmManager.Stub.asInterface(baseBinder);
	}
}
