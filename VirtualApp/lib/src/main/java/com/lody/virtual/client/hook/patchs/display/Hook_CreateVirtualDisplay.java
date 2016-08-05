package com.lody.virtual.client.hook.patchs.display;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import android.hardware.display.IVirtualDisplayCallback;
import android.media.projection.IMediaProjection;
import android.view.Surface;

/**
 * @author Lody
 *
 * @see android.hardware.display.IDisplayManager#createVirtualDisplay(IVirtualDisplayCallback,
 *      IMediaProjection, String, String, int, int, int, Surface, int)
 *
 */
/* package */ class Hook_CreateVirtualDisplay extends Hook {

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeHook(who, method, args);
	}

	@Override
	public String getName() {
		return "createVirtualDisplay";
	}

}
