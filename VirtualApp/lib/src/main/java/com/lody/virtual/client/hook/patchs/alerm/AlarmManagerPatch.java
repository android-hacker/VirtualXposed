package com.lody.virtual.client.hook.patchs.alerm;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookAlarmBinder;

import android.app.IAlarmManager;
import android.content.Context;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@Patch({Hook_Set.class})
public class AlarmManagerPatch extends PatchObject<IAlarmManager, HookAlarmBinder> {

	@Override
	protected HookAlarmBinder initHookObject() {
		return new HookAlarmBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.ALARM_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.ALARM_SERVICE);
	}
}
