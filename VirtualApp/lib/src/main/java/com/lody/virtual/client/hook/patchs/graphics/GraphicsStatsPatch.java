package com.lody.virtual.client.hook.patchs.graphics;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.GraphicsStatsBinderDelegate;

import mirror.android.os.ServiceManager;


/**
 * @author Lody
 */
@Patch({RequestBufferForProcess.class})
public class GraphicsStatsPatch extends PatchDelegate<GraphicsStatsBinderDelegate> {

	@Override
	protected GraphicsStatsBinderDelegate createHookDelegate() {
		return new GraphicsStatsBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService("graphicsstats");
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call("graphicsstats") != getHookDelegate();
	}
}
