package com.lody.virtual.client.hook.patchs.vibrator;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.binders.VibratorBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see android.os.Vibrator
 */
public class VibratorPatch extends PatchDelegate<VibratorBinderDelegate> {

	@Override
	protected VibratorBinderDelegate createHookDelegate() {
		return new VibratorBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.VIBRATOR_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceCallingPkgHook("vibrate"));
		addHook(new ReplaceCallingPkgHook("vibratePattern"));
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(Context.VIBRATOR_SERVICE);
	}

}
