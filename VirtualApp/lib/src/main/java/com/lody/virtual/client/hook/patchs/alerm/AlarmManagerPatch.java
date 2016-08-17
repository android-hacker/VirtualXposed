package com.lody.virtual.client.hook.patchs.alerm;

import android.app.IAlarmManager;
import android.content.Context;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.HookAlarmBinder;

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
	protected void applyHooks() {
		super.applyHooks();
		addHook(new StaticHook("getNextAlarmClock")).replaceLastUserId();
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.ALARM_SERVICE);
	}
}
