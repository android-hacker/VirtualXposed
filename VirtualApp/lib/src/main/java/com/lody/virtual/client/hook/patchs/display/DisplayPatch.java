package com.lody.virtual.client.hook.patchs.display;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;

import mirror.android.hardware.display.DisplayManagerGlobal;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@Patch({CreateVirtualDisplay.class})
public class DisplayPatch extends PatchDelegate<HookDelegate<IInterface>> {
	public DisplayPatch() {
		super(new HookDelegate<IInterface>(
				DisplayManagerGlobal.mDm.get(DisplayManagerGlobal.getInstance.call())));
	}

	@Override
	public void inject() throws Throwable {
		Object dmg = DisplayManagerGlobal.getInstance.call();
		DisplayManagerGlobal.mDm.set(dmg, getHookDelegate().getProxyInterface());
	}

	@Override
	public boolean isEnvBad() {
		Object dmg = DisplayManagerGlobal.getInstance.call();
		IInterface mDm = DisplayManagerGlobal.mDm.get(dmg);
		return mDm != getHookDelegate().getProxyInterface();
	}
}
