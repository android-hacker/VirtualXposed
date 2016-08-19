package com.lody.virtual.client.hook.patchs.display;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookDisplayManagerBinder;
import com.lody.virtual.helper.utils.Reflect;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 *         API 17后加入.
 * @see IDisplayManager
 * @see android.hardware.display.DisplayManagerGlobal
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@Patch({CreateVirtualDisplay.class})
public class DisplayManagerPatch extends PatchObject<IDisplayManager, HookDisplayManagerBinder> {
	@Override
	protected HookDisplayManagerBinder initHookObject() {
		return new HookDisplayManagerBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.DISPLAY_SERVICE);
		DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
		try {
			Reflect.on(dm).set("mDm", getHookObject().getProxyObject());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.DISPLAY_SERVICE);
	}
}
