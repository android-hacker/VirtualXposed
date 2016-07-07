package com.lody.virtual.client.hook.patchs.graphics;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookGraphicsStatsBinder;

import android.os.ServiceManager;
import android.view.IGraphicsStats;

/**
 * @author Lody
 *
 *
 *
 * @see IGraphicsStats
 */
@Patch({Hook_RequestBufferForProcess.class})
public class GraphicsStatsPatch extends PatchObject<IGraphicsStats, HookGraphicsStatsBinder> {

	@Override
	protected HookGraphicsStatsBinder initHookObject() {
		return new HookGraphicsStatsBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(HookGraphicsStatsBinder.SERVICE_NAME);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(HookGraphicsStatsBinder.SERVICE_NAME) != getHookObject();
	}
}
