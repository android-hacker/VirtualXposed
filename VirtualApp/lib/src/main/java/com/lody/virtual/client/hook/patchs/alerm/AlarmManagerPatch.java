package com.lody.virtual.client.hook.patchs.alerm;

import android.content.Context;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.AlarmBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@Patch({Set.class})
public class AlarmManagerPatch extends PatchDelegate<AlarmBinderDelegate> {

	@Override
	protected AlarmBinderDelegate createHookDelegate() {
		return new AlarmBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.ALARM_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(Context.ALARM_SERVICE);
	}
}
