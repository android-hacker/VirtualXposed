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
/* package */ class Hook_CreateVirtualDisplay extends Hook<DisplayManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_CreateVirtualDisplay(DisplayManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "createVirtualDisplay";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
