package com.lody.virtual.client.hook.patchs.vibrator;

import android.content.Context;
import android.os.IVibratorService;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.binders.HookVibratorBinder;

/**
 * @author Lody
 *
 *
 * @see IVibratorService
 * @see android.os.Vibrator
 */
public class VibratorPatch extends PatchObject<IVibratorService, HookVibratorBinder> {

	@Override
	protected HookVibratorBinder initHookObject() {
		return new HookVibratorBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.VIBRATOR_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceCallingPkgHook("vibrate")).replaceUid(0);
		addHook(new ReplaceCallingPkgHook("vibratePattern")).replaceUid(0);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.VIBRATOR_SERVICE);
	}

}
