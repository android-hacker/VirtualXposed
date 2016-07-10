package com.lody.virtual.client.hook.patchs.vibrator;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookVibratorBinder;

import android.content.Context;
import android.os.IVibratorService;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IVibratorService
 * @see android.os.Vibrator
 */
@Patch({Hook_Vibrate.class, Hook_VibratePattern.class})
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
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.VIBRATOR_SERVICE);
	}

}
